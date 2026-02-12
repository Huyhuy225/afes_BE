package com.group5.afes.controller;

import com.group5.afes.entity.SensorData;
import com.group5.afes.repository.SensorDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sensors")
@CrossOrigin(origins = "*") // Cho phép các thiết bị/web khác truy cập
public class SensorDataController {

    @Autowired
    private SensorDataRepository repository;

    // API nhận dữ liệu từ thiết bị IoT (POST) [cite: 30, 31]
    @PostMapping("/log")
    public SensorData receiveSensorData(@RequestBody SensorData data) {
        return repository.save(data); // Lưu trực tiếp vào Azure MySQL
    }

    // API lấy lịch sử hiển thị lên Dashboard (GET) [cite: 37, 40]
    @GetMapping("/history")
    public List<SensorData> getHistory() {
        return repository.findAll();
    }
}