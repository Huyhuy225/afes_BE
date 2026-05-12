package com.group5.afes.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group5.afes.entity.SensorData;
import com.group5.afes.entity.Room;
import com.group5.afes.repository.SensorDataRepository;
import com.group5.afes.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import com.group5.afes.security.AESUtils;

@Service
public class MqttReceiver implements MessageHandler {

    @Autowired
    private SensorDataRepository sensorDataRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Value("${sensor.default-room-code:ROOM-101}")
    private String defaultRoomCode;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String payload = "";
        String topic = "";
        try {
            topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

            Object rawPayload = message.getPayload();
            if (rawPayload instanceof byte[]) {
                payload = new String((byte[]) rawPayload, StandardCharsets.UTF_8);
            } else {
                payload = rawPayload.toString();
            }

            System.out.println("📩 [Received Data] Topic: " + topic + " | Payload: " + payload);

            // Nếu payload rỗng thì bỏ qua
            if (payload == null || payload.trim().isEmpty())
                return;

            // Thử parse JSON
            JsonNode rootNode = null;
            try {
                rootNode = objectMapper.readTree(payload);
            } catch (Exception e) {
                // Không phải JSON, xử lý như dữ liệu thô (plain text/number)
                handleRawValue(topic, payload, null);
                return;
            }

            // Nếu là JSON, kiểm tra xem có trường mã hóa không
            if (rootNode.has("smoke_enc_value") || rootNode.has("flame_enc_value") || rootNode.has("temp_enc_value")) {
                handleEncryptedData(rootNode, topic);
            } else {
                // JSON nhưng không mã hóa (ví dụ: {"value": 30.5}) hoặc JSON thô từ cảm biến
                handleRawJson(rootNode, topic);
            }

        } catch (Exception e) {
            System.err.println("❌ [ERROR] Lỗi xử lý MQTT message: " + e.getMessage());
            System.err.println("👉 Topic: " + topic + " | Payload: " + payload);
        }
    }

    private void handleRawJson(JsonNode rootNode, String topic) {
        // Hỗ trợ format {"value": 123} hoặc {"temp": 123, "humi": 45}
        if (rootNode.has("value")) {
            handleRawValue(topic, rootNode.get("value").asText(), rootNode);
        } else if (rootNode.isNumber()) {
            handleRawValue(topic, rootNode.asText(), rootNode);
        } else {
            // Duyệt qua tất cả các field nếu là object phức tạp
            rootNode.properties().forEach(entry -> {
                if (entry.getValue().isNumber()) {
                    handleRawValue(topic + "/" + entry.getKey(), entry.getValue().asText(), rootNode);
                }
            });
        }
    }

    private void handleRawValue(String topic, String value, JsonNode rootNode) {
        try {
            // Xác định loại cảm biến dựa trên topic
            String sensorName = "Unknown Sensor";
            if (topic.contains("temp") || topic.contains("dht20"))
                sensorName = "Temp Sensor";
            else if (topic.contains("smoke"))
                sensorName = "Smoke Sensor";
            else if (topic.contains("flame"))
                sensorName = "Flame Sensor";

            System.out.println(String.format("📊 [RAW DATA] %s: %s (Topic: %s)", sensorName, value, topic));

            Room room = resolveRoom(rootNode != null ? rootNode : objectMapper.createObjectNode());
            saveData(topic, sensorName, value, room);
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Lỗi lưu dữ liệu thô: " + e.getMessage());
        }
    }

    private void handleEncryptedData(JsonNode rootNode, String topic) {
        try {
            String smokeVal = AESUtils.decryptData(rootNode.path("smoke_enc_value").asText(null));
            String flameVal = AESUtils.decryptData(rootNode.path("flame_enc_value").asText(null));
            String tempVal = AESUtils.decryptData(rootNode.path("temp_enc_value").asText(null));

            if (smokeVal != null || flameVal != null || tempVal != null) {
                System.out.println(
                        String.format("✅ [DECRYPTED] Smoke=%s, Flame=%s, Temp=%s", smokeVal, flameVal, tempVal));

                Room room = resolveRoom(rootNode);

                if (smokeVal != null)
                    saveData(topic, "Smoke Sensor", smokeVal, room);
                if (flameVal != null)
                    saveData(topic, "Flame Sensor", flameVal, room);
                if (tempVal != null)
                    saveData(topic, "Temp Sensor", tempVal, room);

                System.out.println("🚀 [DATABASE] Đã đồng bộ dữ liệu mã hóa thành công!");
            } else {
                System.err.println("⚠️ [WARNING] Giải mã thất bại!");
            }
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Lỗi xử lý dữ liệu mã hóa: " + e.getMessage());
        }
    }

    private void saveData(String topic, String sensorName, String value, Room room) {
        try {
            SensorData data = new SensorData();
            data.setTopic(topic);
            data.setSensorName(sensorName);
            data.setMainValue(Float.parseFloat(value));
            data.setDetails(String.format("{\"value\": %s}", value));
            data.setRoom(room);
            data.setTimestamp(LocalDateTime.now());
            sensorDataRepository.save(data);
        } catch (NumberFormatException e) {
            System.err.println("⚠️ [SKIP] Giá trị không phải số: " + value);
        }
    }

    private Room resolveRoom(JsonNode rootNode) {
        if (rootNode != null && rootNode.hasNonNull("roomCode")) {
            return roomRepository.findByCode(rootNode.get("roomCode").asText()).orElseGet(this::findDefaultRoom);
        }
        return findDefaultRoom();
    }

    private Room findDefaultRoom() {
        return roomRepository.findByCode(defaultRoomCode).orElse(null);
    }
}