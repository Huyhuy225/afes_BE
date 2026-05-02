package com.group5.afes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomOverviewDTO {
    private RoomSummaryDTO summary;
    private long sensorRecordCount;
}
