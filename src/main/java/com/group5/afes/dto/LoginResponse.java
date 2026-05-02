package com.group5.afes.dto;

import com.group5.afes.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private String username;
    private String fullName;
    private String role;
    private Integer userId;
    private Integer roomId;
    private String roomCode;
    private String roomName;

    public static LoginResponse from(User user, String token) {
        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole().getName())
                .userId(user.getId())
                .roomId(user.getRoom() != null ? user.getRoom().getId() : null)
                .roomCode(user.getRoom() != null ? user.getRoom().getCode() : null)
                .roomName(user.getRoom() != null ? user.getRoom().getName() : null)
                .build();
    }
}
