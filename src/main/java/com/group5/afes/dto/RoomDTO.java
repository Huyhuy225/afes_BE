package com.group5.afes.dto;

import com.group5.afes.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDTO {
    private Integer id;
    private String code;
    private String name;
    private String description;
    private Boolean monitoringEnabled;
    private Long userCount;

    public static RoomDTO from(Room room, Long userCount) {
        return RoomDTO.builder()
                .id(room.getId())
                .code(room.getCode())
                .name(room.getName())
                .description(room.getDescription())
                .monitoringEnabled(room.getMonitoringEnabled())
                .userCount(userCount)
                .build();
    }
}