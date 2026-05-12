package com.group5.afes.services;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.group5.afes.entity.Room;
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

            for (String actionVariant : buildActionVariants(action)) {
                String payload = buildControlPayload(actionVariant, room);
                MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
                message.setQos(1);
                message.setRetained(false);

                client.publish(controlTopic, message);
                System.out.println("[CONTROL] Published to " + controlTopic + ": " + payload);
            }
        } finally {
            if (client.isConnected()) {
                client.disconnect();
            }
            client.close();
        }
    }

    private String buildControlPayload(String action, Room room) {
        String safeAction = action != null ? action : "";
        if (room == null) {
            return "{\"action\":\"" + safeAction + "\"}";
        }
        return "{\"action\":\"" + safeAction + "\",\"roomId\":" + room.getId() + "}";
    }

    private java.util.List<String> buildActionVariants(String action) {
        String safeAction = action != null ? action : "";
        if ("reset_system".equals(safeAction)) {
            return java.util.List.of("reset_system", "rest_system");
        }
        if ("rest_system".equals(safeAction)) {
            return java.util.List.of("rest_system", "reset_system");
        }
        return java.util.List.of(safeAction);
    }
}
