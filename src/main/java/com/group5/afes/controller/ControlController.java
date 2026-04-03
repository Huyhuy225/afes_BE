package com.group5.afes.controller;

import com.group5.afes.services.ControlPublisherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/control")
public class ControlController {

    private final ControlPublisherService controlPublisherService;

    public ControlController(ControlPublisherService controlPublisherService) {
        this.controlPublisherService = controlPublisherService;
    }

    @PostMapping
    public ResponseEntity<?> control(@RequestBody Map<String, String> payload) {
        String action = payload.getOrDefault("action", "").trim();
        if (action.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing action"));
        }
        return publish(action);
    }

    @PostMapping("/pump")
    public ResponseEntity<?> pump(@RequestBody Map<String, String> payload) {
        String state = payload.getOrDefault("state", "").trim().toLowerCase();
        if ("on".equals(state)) {
            return publish("pump_on");
        }
        if ("off".equals(state)) {
            return publish("pump_off");
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid state, use on/off"));
    }

    @PostMapping("/test-alarm")
    public ResponseEntity<?> testAlarm() {
        return publish("test_alarm");
    }

    @PostMapping("/reset")
    public ResponseEntity<?> reset() {
        return publish("reset_system");
    }

    @PostMapping("/full-test")
    public ResponseEntity<?> fullTest() {
        return publish("full_test");
    }

    @PostMapping("/emergency")
    public ResponseEntity<?> emergency() {
        return publish("emergency_alert");
    }

    @PostMapping("/emergency-off")
    public ResponseEntity<?> emergencyOff() {
        return publish("emergency_off");
    }

    /** Tắt toàn bộ đầu ra thủ công: LED, còi, bơm — tương đương về phần cứng với emergency_off */
    @PostMapping("/outputs-off")
    public ResponseEntity<?> allOutputsOff() {
        return publish("all_outputs_off");
    }

    private ResponseEntity<?> publish(String action) {
        try {
            controlPublisherService.publishAction(action);
            return ResponseEntity.ok(Map.of("ok", true, "action", action));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("ok", false, "action", action, "error", ex.getMessage()));
        }
    }
}
