package com.group5.afes.services;

import com.group5.afes.dto.LoginRequest;
import com.group5.afes.dto.LoginResponse;
import com.group5.afes.dto.UserDTO;
import com.group5.afes.entity.Room;
import com.group5.afes.entity.Role;
import com.group5.afes.entity.User;
import com.group5.afes.repository.RoomRepository;
import com.group5.afes.repository.RoleRepository;
import com.group5.afes.repository.UserRepository;
import com.group5.afes.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!user.getIsActive()) {
            throw new RuntimeException("User account is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole().getName());
        return LoginResponse.from(user, token);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDTO::from)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return UserDTO.from(user);
    }

    public UserDTO createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (user.getPhoneNumber() == null || user.getPhoneNumber().trim().isEmpty()) {
            throw new RuntimeException("Phone number is required");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        applyRoomIfPresent(user);
        user.setRole(resolveRole(user.getRole()));
        User savedUser = userRepository.save(user);
        return UserDTO.from(savedUser);
    }

    public UserDTO updateUser(Integer id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setFullName(userDetails.getFullName());
        user.setEmail(userDetails.getEmail());
        if (userDetails.getPhoneNumber() != null && !userDetails.getPhoneNumber().trim().isEmpty()) {
            user.setPhoneNumber(userDetails.getPhoneNumber());
        }
        user.setIsActive(userDetails.getIsActive());
        user.setRoom(resolveRoom(userDetails.getRoom()));
        if (userDetails.getRole() != null) {
            user.setRole(resolveRole(userDetails.getRole()));
        }
        
        User updatedUser = userRepository.save(user);
        return UserDTO.from(updatedUser);
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    private void applyRoomIfPresent(User user) {
        Room room = resolveRoom(user.getRoom());
        if (room != null) {
            user.setRoom(room);
        }
    }

    private Room resolveRoom(Room room) {
        if (room == null || room.getId() == null) {
            return null;
        }
        return roomRepository.findById(room.getId()).orElse(null);
    }

    private Role resolveRole(Role role) {
        if (role == null) {
            return roleRepository.findByName("USER").orElseThrow(() -> new RuntimeException("Default role USER not found"));
        }

        if (role.getId() != null) {
            return roleRepository.findById(role.getId())
                    .orElseThrow(() -> new RuntimeException("Role not found"));
        }

        if (role.getName() != null && !role.getName().isBlank()) {
            return roleRepository.findByName(role.getName())
                    .orElseThrow(() -> new RuntimeException("Role not found"));
        }

        return roleRepository.findByName("USER").orElseThrow(() -> new RuntimeException("Default role USER not found"));
    }
}
