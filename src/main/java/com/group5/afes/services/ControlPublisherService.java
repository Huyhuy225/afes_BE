package com.group5.afes.services;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.group5.afes.entity.Room;
import com.group5.afes.security.AESUtils;
import java.nio.charset.StandardCharsets;

@Service
public class ControlPublisherService {

    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.username}")
    private String username;

    @Value("${mqtt.password}")
    private String password;

    @Value("${mqtt.control.topic:yolo_uno/control}")
    private String controlTopic;

    public void publishAction(String action) throws Exception {
        publishAction(action, null);
    }

    public void publishAction(String action, Room room) throws Exception {
        String clientId = "afes-control-" + System.currentTimeMillis();
        MqttClient client = new MqttClient(brokerUrl, clientId);
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(false);
            options.setCleanSession(true);
            options.setUserName(username);
            options.setPassword(password.toCharArray());

            client.connect(options);
            // json create
            // Extract numeric device ID from room code (e.g. "ROOM-101" → 101)
            // Hardware uses DEVICE_ID = "101", not the database primary key
            String actionJson;
            if (room == null) {
                actionJson = "{\"action\":\"" + action + "\"}";
            } else {
                String deviceId = room.getCode().replaceAll("[^0-9]", "");
                actionJson = "{\"action\":\"" + action + "\",\"roomId\":" + deviceId + "}";
            }
            // json encrypt
            String encrypted = AESUtils.encryptData(actionJson);
            String payload = "{\"cmd_enc_value\":\"" + encrypted + "\"}";

            MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
            message.setQos(1);
            message.setRetained(false);

            client.publish(controlTopic, message);
            System.out.println("[CONTROL] Published encrypted to " + controlTopic);
            System.out.println("[CONTROL] Original: " + actionJson);
        } finally {
            if (client.isConnected()) {
                client.disconnect();
            }
            client.close();
        }
    }
}
