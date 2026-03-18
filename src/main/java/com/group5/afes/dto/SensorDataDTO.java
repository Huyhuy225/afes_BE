package com.group5.afes.dto;

import lombok.Data;

@Data
public class SensorDataDTO {
    private float temperature; // Đổi Double thành float
    private float humidity;    // Đổi Double thành float
    private float smoke;       // Đổi Double thành float
}