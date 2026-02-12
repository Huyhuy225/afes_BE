package com.group5.afes.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "history_log")
public class SensorData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private float temperature;    // Dữ liệu từ DHT20 [cite: 19]
    private float gasLevel;       // Dữ liệu từ MQ-2 [cite: 18]
    private boolean fireDetected; // Kết quả từ thuật toán fusion [cite: 56]
    private LocalDateTime timestamp = LocalDateTime.now();
}