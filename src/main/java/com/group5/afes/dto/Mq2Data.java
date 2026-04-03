package com.group5.afes.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Mq2Data {

    @JsonProperty("CO")
    private float co;

    @JsonProperty("LPG")
    private float lpg;

    @JsonProperty("SMOKE")
    private float smoke;
}