package com.group5.afes.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SensorDataDTO {

    @JsonProperty("mq2_1")
    private Mq2Data mq2_1;

    @JsonProperty("mq2_2")
    private Mq2Data mq2_2;
}