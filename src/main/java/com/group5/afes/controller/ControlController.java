package com.group5.afes.controller;

import com.group5.afes.entity.Room;
import com.group5.afes.entity.User;
import com.group5.afes.repository.RoomRepository;
import com.group5.afes.repository.UserRepository;
import com.group5.afes.services.ControlPublisherService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/control")
public class ControlController {

    private final ControlPublisherService controlPublisherService;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public ControlController(ControlPublisherService controlPublisherService,
                             UserRepository userRepository,
                             RoomRepository roomRepository) {
        this.controlPublisherService = controlPublisherService;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    /**
     * Resolve the Room for the currently authenticated user.
     * If the user has a room assigned, commands will target only that room's hardware.
     * If no room is assigned, the command will be a broadcast (no roomId filter).
     */
    private Room resolveCurrentUserRoom() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByUsername(auth.getName())
                .map(User::getRoom)
                .orElse(null);
    }

    private ResponseEntity<?> publishForCurrentUser(String action) {
        try {
            Room room = resolveCurrentUserRoom();
            controlPublisherService.publishAction(action, room);
            return ResponseEntity.ok(Map.of("ok", true, "action", action));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("ok", false, "action", action, "error", ex.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")  // Only admin can control
    public ResponseEntity<?> control(@RequestBody Map<String, String> payload) {
        String action = payload.getOrDefault("action", "").trim();
        if (action.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing action"));
        }
        return publishForCurrentUser(action);
    }

    @PostMapping("/pump")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> pump(@RequestBody Map<String, String> payload) {
        String state = payload.getOrDefault("state", "").trim().toLowerCase();
        if ("on".equals(state)) {
            return publishForCurrentUser("pump_on");
        }
        if ("off".equals(state)) {
            return publishForCurrentUser("pump_off");
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid state, use on/off"));
    }

    @PostMapping("/test-alarm")
    public ResponseEntity<?> testAlarm() {
        return publishForCurrentUser("test_alarm");
    }

    @PostMapping("/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reset() {
        return publishForCurrentUser("reset_system");
    }

    @PostMapping("/full-test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> fullTest() {
        return publishForCurrentUser("full_test");
    }

    @PostMapping("/emergency")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> emergency() {
        return publishForCurrentUser("emergency_alert");
    }

    @PostMapping("/emergency-off")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> emergencyOff() {
        return publishForCurrentUser("emergency_off");
    }

    /** Tắt toàn bộ đầu ra thủ công: LED, còi, bơm — tương đương về phần cứng với emergency_off */
    @PostMapping("/outputs-off")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> allOutputsOff() {
        return publishForCurrentUser("all_outputs_off");
    }
}
