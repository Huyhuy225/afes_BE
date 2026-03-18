package com.group5.afes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group5.afes.dto.SensorDataDTO;
import com.group5.afes.entity.SensorData;
import com.group5.afes.repository.SensorDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class MqttReceiver implements MessageHandler {

    @Autowired
    private SensorDataRepository sensorDataRepository;

    // XÓA DÒNG @Autowired ở đây
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        try {
            String payload = message.getPayload().toString();

            // Sử dụng objectMapper đã khởi tạo ở trên
            SensorDataDTO dto = objectMapper.readValue(payload, SensorDataDTO.class);

            SensorData sensorData = new SensorData();
            sensorData.setTemperature(dto.getTemperature());
            sensorData.setGasLevel(dto.getSmoke());
            sensorData.setTimestamp(LocalDateTime.now());

            sensorDataRepository.save(sensorData);

            System.out.println("🚀 [DATABASE] LUU THANH CONG: " + payload);

        } catch (Exception e) {
            System.err.println("❌ [ERROR] MQTT Data Error: " + e.getMessage());
        }
    }
}