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
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

@Service
public class MqttReceiver implements MessageHandler {

    @Autowired
    private SensorDataRepository sensorDataRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Value("${sensor.default-room-code:ROOM-101}")
    private String defaultRoomCode;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Khóa và IV khớp hoàn toàn với file JS
    private static final byte[] AES_KEY = {
            (byte) 0x2B, (byte) 0x7E, (byte) 0x15, (byte) 0x16, (byte) 0x28, (byte) 0xAE, (byte) 0xD2, (byte) 0xA6,
            (byte) 0xAB, (byte) 0xF7, (byte) 0x15, (byte) 0x88, (byte) 0x09, (byte) 0xCF, (byte) 0x4F, (byte) 0x3C
    };

    private static final byte[] AES_IV = {
            (byte) 0x28, (byte) 0x34, (byte) 0xA5, (byte) 0xAF, (byte) 0xBE, (byte) 0xB8, (byte) 0x14, (byte) 0xF5,
            (byte) 0x08, (byte) 0xE1, (byte) 0x24, (byte) 0xD2, (byte) 0xB3, (byte) 0xAB, (byte) 0xDB, (byte) 0xCE
    };

    private static final String TOPIC_ENCODED = "yolo_uno/sensors/all";

    private String decryptData(String ciphertext) {
        try {
            if (ciphertext == null || ciphertext.isEmpty()) return null;

            // Sử dụng NoPadding để khớp với setAutoPadding(false) bên Node.js
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(AES_KEY, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(AES_IV);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] decodedBytes = Base64.getDecoder().decode(ciphertext);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);

            // Xử lý chuỗi sau giải mã: xóa ký tự null và khoảng trắng
            String decrypted = new String(decryptedBytes, StandardCharsets.UTF_8);
            return decrypted.replace("\0", "").trim();
        } catch (Exception e) {
            System.err.println("❌ [DECRYPT ERROR] " + e.getMessage());
            return null;
        }
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        try {
            String topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();
            String payload = message.getPayload().toString();

            System.out.println("📩 [Received Data] Topic: " + topic);
            JsonNode rootNode = objectMapper.readTree(payload);

            // Logic chính: Nếu nhận được từ topic mã hóa hoặc payload có chứa các trường mã hóa
            if (TOPIC_ENCODED.equals(topic) || rootNode.has("smoke_enc_value")) {
                handleEncryptedData(rootNode, topic);
            } else {
                // Giữ lại logic xử lý dữ liệu thô cũ nếu cần (tùy chọn)
                System.out.println("⚠️ Dữ liệu không ở dạng mã hóa hoặc sai Topic");
            }

        } catch (Exception e) {
            System.err.println("❌ [ERROR] Lỗi parse JSON hoặc lưu DB: " + e.getMessage());
        }
    }

    private void handleEncryptedData(JsonNode rootNode, String topic) {
        try {
            String smokeVal = decryptData(rootNode.path("smoke_enc_value").asText(null));
            String flameVal = decryptData(rootNode.path("flame_enc_value").asText(null));
            String tempVal = decryptData(rootNode.path("temp_enc_value").asText(null));

            if (smokeVal != null && flameVal != null && tempVal != null) {
                System.out.println(String.format("✅ [DECRYPTED] Smoke=%s, Flame=%s, Temp=%s", smokeVal, flameVal, tempVal));

                Room room = resolveRoom(rootNode);

                // Lưu các bản ghi (khớp với logic SensorData.create bên JS)
                saveData(topic, "Smoke Sensor", smokeVal, room);
                saveData(topic, "Flame Sensor", flameVal, room);
                saveData(topic, "Temp Sensor", tempVal, room);

                System.out.println("🚀 [DATABASE] Đã đồng bộ 3 bản ghi thành công!");
            } else {
                System.err.println("⚠️ [WARNING] Giải mã thất bại!");
            }
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Lỗi xử lý dữ liệu: " + e.getMessage());
        }
    }

    private void saveData(String topic, String sensorName, String value, Room room) {
        SensorData data = new SensorData();
        data.setTopic(topic);
        data.setSensorName(sensorName);
        data.setMainValue(Float.parseFloat(value));
        data.setDetails(String.format("{\"value\": %s}", value));
        data.setRoom(room);
        data.setTimestamp(LocalDateTime.now());
        sensorDataRepository.save(data);
    }

    private Room resolveRoom(JsonNode rootNode) {
        if (rootNode.hasNonNull("roomCode")) {
            return roomRepository.findByCode(rootNode.get("roomCode").asText()).orElseGet(this::findDefaultRoom);
        }
        return findDefaultRoom();
    }

    private Room findDefaultRoom() {
        return roomRepository.findByCode(defaultRoomCode).orElse(null);
    }
}