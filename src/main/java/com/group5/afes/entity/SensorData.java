package com.group5.afes.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_data")
@Data
public class SensorData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String topic;
    @Column(name = "sensor_name")
    private String sensorName;
    @Column(name = "main_value")
    private Float mainValue;
    @Column(columnDefinition = "JSON")
    private String details;
    private String status;
    private LocalDateTime timestamp;
}