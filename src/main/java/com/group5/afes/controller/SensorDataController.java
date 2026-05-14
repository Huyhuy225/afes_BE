package com.group5.afes.controller;

import com.group5.afes.entity.SensorData;
import com.group5.afes.repository.SensorDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sensors")
public class SensorDataController {

    @Autowired
    private SensorDataRepository repository;

    // API nhận dữ liệu từ thiết bị IoT (POST) [cite: 30, 31]
    @PostMapping("/log")
    @PreAuthorize("permitAll()")  // Allow IoT devices to post data
    public SensorData receiveSensorData(@RequestBody SensorData data) {
        return repository.save(data); // Lưu trực tiếp vào Azure MySQL
    }

    // API lấy lịch sử hiển thị lên Dashboard (GET)
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<SensorData> getHistory() {
        return repository.findAll();
    }

    @GetMapping("/history/room/{roomId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<SensorData> getHistoryByRoom(@PathVariable Integer roomId) {
        return repository.findByRoom_IdOrderByTimestampDesc(roomId);
    }



    @GetMapping("/history/latest")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public SensorData getLatest() {
        // Trả về bản ghi mới nhất dựa trên ID hoặc Timestamp
        return repository.findTopByOrderByIdDesc();
    }

    @GetMapping("/history/latest/room/{roomId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public SensorData getLatestByRoom(@PathVariable Integer roomId) {
        return repository.findTopByRoom_IdOrderByIdDesc(roomId);
    }
}