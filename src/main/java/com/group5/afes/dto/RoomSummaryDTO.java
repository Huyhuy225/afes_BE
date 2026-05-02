package com.group5.afes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomSummaryDTO {
    private Integer id;
    private String code;
    private String name;
    private Boolean monitoringEnabled;
    private Long userCount;

    /** Latest temperature reading (°C). */
    private Float temperature;

    /** Latest combined MQ2 total (CO + LPG + SMOKE) across mq2_1 and mq2_2 (ppm-ish). */
    private Float smokeTotal;

    /** Latest flame sensor value (percentage-ish). */
    private Float flame;

    /** Latest timestamp among the readings used for this summary. */
    private LocalDateTime updatedAt;
}
