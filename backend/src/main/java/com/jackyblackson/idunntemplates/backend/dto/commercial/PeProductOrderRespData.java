package com.jackyblackson.idunntemplates.backend.dto.commercial;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PeProductOrderRespData {
    private int count;
    private List<PeProductOrder> orders;
    @JsonProperty("total_diamonds")
    private int totalDiamond;
    @JsonProperty("total_points")
    private int totalPoints;
}
