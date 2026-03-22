package com.group5.afes.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group5.afes.entity.SensorData;
import com.group5.afes.repository.SensorDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class MqttReceiver implements MessageHandler {

    @Autowired
    private SensorDataRepository sensorDataRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Định nghĩa topic y hệt bên Node.js
    private static final String TOPIC_FLAME = "yolo_uno/sensors/flame";
    private static final String TOPIC_SMOKE = "yolo_uno/sensors/smoke";
    private static final String TOPIC_DHT20 = "yolo_uno/sensors/dht20";

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        try {
            // Lấy tên Topic từ Header của gói tin MQTT
            String topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();
            String payload = message.getPayload().toString();

            System.out.println("📩 [Nhận dữ liệu] Topic: " + topic);

            // Chuyển chuỗi JSON thành Node động (giống hệt object trong JS)
            JsonNode rootNode = objectMapper.readTree(payload);

            if (TOPIC_SMOKE.equals(topic)) {
                String[] sensors = {"mq2_1", "mq2_2"};
                for (String sName : sensors) {
                    if (rootNode.has(sName)) {
                        JsonNode vals = rootNode.get(sName);

                        SensorData data = new SensorData();
                        data.setTopic(topic);
                        data.setSensorName(sName);
                        // Lấy giá trị SMOKE
                        data.setMainValue((float) vals.get("SMOKE").asDouble());
                        // Lưu toàn bộ object con {"CO":..., "LPG":..., "SMOKE":...} vào details
                        data.setDetails(vals.toString());
                        data.setTimestamp(LocalDateTime.now());

                        sensorDataRepository.save(data);
                    }
                }
                System.out.println("🚀 [DATABASE] Đã lưu 2 bản ghi SMOKE thành công!");

            } else if (TOPIC_FLAME.equals(topic)) {
                if (rootNode.has("flame_data")) {
                    JsonNode f = rootNode.get("flame_data");

                    SensorData data = new SensorData();
                    data.setTopic(topic);
                    data.setSensorName("Flame_sensor");

                    float f1 = (float) f.get("FLAME1").asDouble();
                    float f2 = (float) f.get("FLAME2").asDouble();
                    // Lấy giá trị lớn nhất để biểu diễn mức cháy mạnh nhất.
                    data.setMainValue(Math.max(f1, f2));
                    data.setDetails(f.toString());
                    data.setTimestamp(LocalDateTime.now());

                    sensorDataRepository.save(data);
                    System.out.println("🚀 [DATABASE] Đã lưu bản ghi FLAME thành công!");
                }
            } else if (TOPIC_DHT20.equals(topic)) {
                if (rootNode.has("dht20_data")) {
                    JsonNode d = rootNode.get("dht20_data");

                    SensorData data = new SensorData();
                    data.setTopic(topic);
                    data.setSensorName("DHT20");
                    data.setMainValue((float) d.get("TEMP").asDouble());
                    data.setDetails(d.toString());
                    data.setTimestamp(LocalDateTime.now());

                    sensorDataRepository.save(data);
                    System.out.println("🚀 [DATABASE] Đã lưu bản ghi DHT20 thành công!");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Lỗi parse JSON hoặc lưu DB: " + e.getMessage());
        }
    }
}