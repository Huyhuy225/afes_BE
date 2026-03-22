package com.group5.afes.services;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
        String clientId = "afes-control-" + System.currentTimeMillis();
        MqttClient client = new MqttClient(brokerUrl, clientId);
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(false);
            options.setCleanSession(true);
            options.setUserName(username);
            options.setPassword(password.toCharArray());

            client.connect(options);

            String payload = "{\"action\":\"" + action + "\"}";
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);
            message.setRetained(false);

            client.publish(controlTopic, message);
            System.out.println("[CONTROL] Published to " + controlTopic + ": " + payload);
        } finally {
            if (client.isConnected()) {
                client.disconnect();
            }
            client.close();
        }
    }
}
