package com.group5.afes.controller;

import com.group5.afes.entity.Room;
import com.group5.afes.entity.User;
import com.group5.afes.repository.UserRepository;
import com.group5.afes.services.ControlPublisherService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/control")
public class ControlController {

    private final ControlPublisherService controlPublisherService;
    private final UserRepository userRepository;

    public ControlController(ControlPublisherService controlPublisherService, UserRepository userRepository) {
        this.controlPublisherService = controlPublisherService;
        this.userRepository = userRepository;
    }

    private Room getUserRoom(Principal principal) {
        if (principal == null) return null;
        Optional<User> userOpt = userRepository.findByUsername(principal.getName());
        return userOpt.map(User::getRoom).orElse(null);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")  // Only admin can control
    public ResponseEntity<?> control(@RequestBody Map<String, String> payload, Principal principal) {
        String action = payload.getOrDefault("action", "").trim();
        if (action.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing action"));
        }
        return publish(action, getUserRoom(principal));
    }

    @PostMapping("/pump")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> pump(@RequestBody Map<String, String> payload, Principal principal) {
        String state = payload.getOrDefault("state", "").trim().toLowerCase();
        if ("on".equals(state)) {
            return publish("pump_on", getUserRoom(principal));
        }
        if ("off".equals(state)) {
            return publish("pump_off", getUserRoom(principal));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid state, use on/off"));
    }

    @PostMapping("/test-alarm")
    public ResponseEntity<?> testAlarm(Principal principal) {
        return publish("test_alarm", getUserRoom(principal));
    }

    @PostMapping("/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reset(Principal principal) {
        return publish("reset_system", getUserRoom(principal));
    }

    @PostMapping("/full-test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> fullTest(Principal principal) {
        return publish("full_test", getUserRoom(principal));
    }

    @PostMapping("/emergency")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> emergency(Principal principal) {
        return publish("emergency_alert", getUserRoom(principal));
    }

    @PostMapping("/emergency-off")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> emergencyOff(Principal principal) {
        return publish("emergency_off", getUserRoom(principal));
    }

    /** Tắt toàn bộ đầu ra thủ công: LED, còi, bơm — tương đương về phần cứng với emergency_off */
    @PostMapping("/outputs-off")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> allOutputsOff(Principal principal) {
        return publish("all_outputs_off", getUserRoom(principal));
    }

    private ResponseEntity<?> publish(String action, Room room) {
        try {
            if (room == null) {
                return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "User has no assigned room"));
            }
            controlPublisherService.publishAction(action, room);
            return ResponseEntity.ok(Map.of("ok", true, "action", action));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("ok", false, "action", action, "error", ex.getMessage()));
        }
    }
}
