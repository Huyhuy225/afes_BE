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
public class UserDTO {
    private Integer id;
    private String username;
    private String email;
    private String phoneNumber;
    private String fullName;
    private String role;
    private Integer roomId;
    private String roomCode;
    private String roomName;
    private Boolean isActive;

    public static UserDTO from(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .fullName(user.getFullName())
                .role(user.getRole().getName())
                .roomId(user.getRoom() != null ? user.getRoom().getId() : null)
                .roomCode(user.getRoom() != null ? user.getRoom().getCode() : null)
                .roomName(user.getRoom() != null ? user.getRoom().getName() : null)
                .isActive(user.getIsActive())
                .build();
    }
}
