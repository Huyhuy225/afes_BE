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
        Room adminRoom = ensureRoom("ADMIN-ROOM", "Phòng quản trị", "Khu vực quản trị và giám sát");
        Room userRoom = ensureRoom("ROOM-101", "Phòng 101", "Phòng mẫu cho user1 và cảm biến demo");

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
    private Room ensureRoom(String code, String name, String description) {
        return roomRepository.findByCode(code)
                .orElseGet(() -> roomRepository.save(Room.builder()
                        .code(code)
                        .name(name)
                        .description(description)
                        .monitoringEnabled(true)
                        .build()));
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