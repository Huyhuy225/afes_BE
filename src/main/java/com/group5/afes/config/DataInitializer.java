package com.group5.afes.config;

import com.group5.afes.entity.Role;
import com.group5.afes.entity.Room;
import com.group5.afes.entity.User;
import com.group5.afes.repository.RoleRepository;
import com.group5.afes.repository.RoomRepository;
import com.group5.afes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
        private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = ensureRole("ADMIN", "Administrator - Full Access");
        Role userRole = ensureRole("USER", "Regular User - View Only");
        Room adminRoom = ensureRoomNormalized(
                "ADMIN-ROOM",
                "Phòng quản trị",
                "Khu vực quản trị và giám sát",
                java.util.List.of()
        );
        Room userRoom = ensureRoomNormalized(
                "101",
                "Phòng 101",
                "Phòng mẫu cho user1 và cảm biến demo",
                java.util.List.of("ROOM-101")
        );

        ensureUser(
                "admin",
                "admin@afes.local",
                "Administrator",
                "admin123",
                adminRole,
                adminRoom
        );

        ensureUser(
                "user1",
                "user1@afes.local",
                "Test User",
                "user1123",
                userRole,
                userRoom
        );
    }

    @SuppressWarnings("SameParameterValue")
        private Room ensureRoomNormalized(String code, String name, String description, java.util.List<String> legacyCodes) {
                Room existing = roomRepository.findByCodeIgnoreCase(code).orElse(null);
                if (existing == null) {
                        for (String legacy : legacyCodes) {
                                if (legacy == null || legacy.isBlank()) continue;
                                existing = roomRepository.findByCodeIgnoreCase(legacy).orElse(null);
                                if (existing != null) {
                                        existing.setCode(code);
                                        existing.setName(name);
                                        existing.setDescription(description);
                                        existing.setMonitoringEnabled(true);
                                        return roomRepository.save(existing);
                                }
                        }
                }

                if (existing != null) {
                        existing.setCode(code);
                        existing.setName(name);
                        existing.setDescription(description);
                        existing.setMonitoringEnabled(true);
                        return roomRepository.save(existing);
                }

                return roomRepository.save(Room.builder()
                                .code(code)
                                .name(name)
                                .description(description)
                                .monitoringEnabled(true)
                                .build());
    }

    private Role ensureRole(String name, String description) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(name)
                        .description(description)
                        .build()));
    }

        private void ensureUser(String username, String email, String fullName, String rawPassword, Role role, Room room) {
        User user = userRepository.findByUsername(username)
                .orElseGet(User::new);

        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
                user.setRoom(room);
        user.setIsActive(true);

        userRepository.save(user);
    }
}