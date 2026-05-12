package com.group5.afes.controller;

import com.group5.afes.dto.RoomDTO;
import com.group5.afes.dto.RoomSummaryDTO;
import com.group5.afes.dto.RoomOverviewDTO;
import com.group5.afes.dto.UserDTO;
import com.group5.afes.dto.Mq2Data;
import com.group5.afes.entity.Room;
import com.group5.afes.entity.User;
import com.group5.afes.entity.SensorData;
import com.group5.afes.repository.RoomRepository;
import com.group5.afes.repository.SensorDataRepository;
import com.group5.afes.repository.UserRepository;
import com.group5.afes.services.ControlPublisherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/rooms")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoomController {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final SensorDataRepository sensorDataRepository;
    private final ControlPublisherService controlPublisherService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping
    public ResponseEntity<List<RoomDTO>> getRooms() {
        List<RoomDTO> rooms = roomRepository.findAll().stream()
                .map(room -> RoomDTO.from(room, userRepository.countByRoom_Id(room.getId())))
                .toList();
        return ResponseEntity.ok(rooms);
    }

    /**
     * Resolve the sensor type from a SensorData record, using the same logic as
     * the frontend Dashboard.jsx resolveSensorType() function.
     */
    private String resolveSensorType(SensorData item) {
        if (item == null) return "";
        String name = (item.getSensorName() != null ? item.getSensorName() : "").toLowerCase();
        if (name.contains("temp")) return "temperature";
        if (name.contains("smoke")) return "smoke";
        if (name.contains("flame")) return "flame";

        String topic = (item.getTopic() != null ? item.getTopic() : "").toLowerCase();
        if (topic.contains("dht20") || topic.contains("temp")) return "temperature";
        if (topic.contains("smoke")) return "smoke";
        if (topic.contains("flame")) return "flame";
        return "";
    }

    /**
     * Build a RoomSummaryDTO by scanning the sensor history for a room,
     * using the exact same sensor-type resolution logic as the user Dashboard.
     * This guarantees admin and user see the same data.
     */
    private RoomSummaryDTO buildRoomSummary(Room room) {
        Integer roomId = room.getId();
        List<SensorData> records = sensorDataRepository.findByRoom_IdOrderByTimestampDesc(roomId);

        SensorData latestTemp = null;
        SensorData latestSmoke = null;
        SensorData latestFlame = null;

        for (SensorData record : records) {
            String type = resolveSensorType(record);
            if ("temperature".equals(type) && latestTemp == null) latestTemp = record;
            else if ("smoke".equals(type) && latestSmoke == null) latestSmoke = record;
            else if ("flame".equals(type) && latestFlame == null) latestFlame = record;
            if (latestTemp != null && latestSmoke != null && latestFlame != null) break;
        }

        LocalDateTime updatedAt = maxTimestamp(
                latestTemp != null ? latestTemp.getTimestamp() : null,
                latestFlame != null ? latestFlame.getTimestamp() : null,
                latestSmoke != null ? latestSmoke.getTimestamp() : null
        );

        return RoomSummaryDTO.builder()
                .id(roomId)
                .code(room.getCode())
                .name(room.getName())
                .monitoringEnabled(room.getMonitoringEnabled())
                .userCount(userRepository.countByRoom_Id(roomId))
                .temperature(latestTemp != null ? latestTemp.getMainValue() : null)
                .smokeTotal(latestSmoke != null ? latestSmoke.getMainValue() : null)
                .flame(latestFlame != null ? latestFlame.getMainValue() : null)
                .updatedAt(updatedAt)
                .build();
    }

    /** Compact per-room view for overview lists. */
    @GetMapping("/summary")
    public ResponseEntity<List<RoomSummaryDTO>> getRoomSummaries() {
        List<RoomSummaryDTO> summaries = roomRepository.findAll().stream()
                .map(this::buildRoomSummary)
                .toList();
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/{roomId}/overview")
    public ResponseEntity<RoomOverviewDTO> getRoomOverview(@PathVariable Integer roomId) {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found"));

        RoomSummaryDTO summary = buildRoomSummary(room);
        long recordCount = sensorDataRepository.countByRoom_Id(roomId);

        return ResponseEntity.ok(RoomOverviewDTO.builder()
            .summary(summary)
            .sensorRecordCount(recordCount)
            .build());
    }

    private LocalDateTime maxTimestamp(LocalDateTime... values) {
        LocalDateTime max = null;
        if (values == null) return null;
        for (LocalDateTime v : values) {
            if (v == null) continue;
            if (max == null || v.isAfter(max)) max = v;
        }
        return max;
    }

    /* Kept for backward compatibility, but buildRoomSummary now uses mainValue directly */
    private float parseMq2Total(SensorData data) {
        if (data == null) return 0f;
        float fallbackSmoke = data.getMainValue() != null ? data.getMainValue() : 0f;
        String details = data.getDetails();
        if (details == null || details.isBlank()) return fallbackSmoke;
        try {
            Mq2Data parsed = objectMapper.readValue(details, Mq2Data.class);
            return parsed.getCo() + parsed.getLpg() + parsed.getSmoke();
        } catch (Exception ex) {
            return fallbackSmoke;
        }
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDTO> getRoom(@PathVariable Integer roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        return ResponseEntity.ok(RoomDTO.from(room, userRepository.countByRoom_Id(room.getId())));
    }

    @GetMapping("/{roomId}/users")
    public ResponseEntity<List<UserDTO>> getUsersInRoom(@PathVariable Integer roomId) {
        if (!roomRepository.existsById(roomId)) {
            return ResponseEntity.notFound().build();
        }

        List<UserDTO> users = userRepository.findAll().stream()
                .filter(user -> user.getRoom() != null && roomId.equals(user.getRoom().getId()))
                .map(UserDTO::from)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{roomId}/history")
    public ResponseEntity<?> getRoomSensorHistory(@PathVariable Integer roomId) {
        if (!roomRepository.existsById(roomId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sensorDataRepository.findByRoom_IdOrderByTimestampDesc(roomId));
    }

    @PutMapping("/{roomId}/monitoring")
    @Transactional
    public ResponseEntity<RoomDTO> updateMonitoring(@PathVariable Integer roomId, @RequestBody Map<String, Boolean> payload) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        room.setMonitoringEnabled(payload.getOrDefault("enabled", Boolean.TRUE));
        Room saved = roomRepository.save(room);
        return ResponseEntity.ok(RoomDTO.from(saved, userRepository.countByRoom_Id(saved.getId())));
    }

    @PutMapping("/{roomId}/users/{userId}")
    @Transactional
    public ResponseEntity<UserDTO> assignUserToRoom(@PathVariable Integer roomId, @PathVariable Integer userId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRoom(room);
        return ResponseEntity.ok(UserDTO.from(userRepository.save(user)));
    }

    @PostMapping("/{roomId}/control")
    public ResponseEntity<?> controlRoom(@PathVariable Integer roomId, @RequestBody Map<String, String> payload) throws Exception {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        String action = payload.getOrDefault("action", "").trim();
        if (action.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing action"));
        }
        controlPublisherService.publishAction(action, room);
        return ResponseEntity.ok(Map.of("ok", true, "roomId", roomId, "action", action));
    }

    /** Broadcast to all controllers: activates all sirens (reuses the existing test_alarm action). */
    @PostMapping("/broadcast-alarm")
    public ResponseEntity<?> broadcastAlarm() {
        try {
            controlPublisherService.publishAction("test_alarm");
            return ResponseEntity.ok(Map.of("ok", true, "action", "test_alarm"));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("ok", false, "action", "test_alarm", "error", ex.getMessage()));
        }
    }
}