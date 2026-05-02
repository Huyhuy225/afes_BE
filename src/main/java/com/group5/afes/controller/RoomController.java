package com.group5.afes.controller;

import com.group5.afes.dto.RoomDTO;
import com.group5.afes.dto.UserDTO;
import com.group5.afes.entity.Room;
import com.group5.afes.entity.User;
import com.group5.afes.repository.RoomRepository;
import com.group5.afes.repository.SensorDataRepository;
import com.group5.afes.repository.UserRepository;
import com.group5.afes.services.ControlPublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/rooms")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoomController {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final SensorDataRepository sensorDataRepository;
    private final ControlPublisherService controlPublisherService;

    @GetMapping
    public ResponseEntity<List<RoomDTO>> getRooms() {
        List<RoomDTO> rooms = roomRepository.findAll().stream()
                .map(room -> RoomDTO.from(room, userRepository.countByRoom_Id(room.getId())))
                .toList();
        return ResponseEntity.ok(rooms);
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
}