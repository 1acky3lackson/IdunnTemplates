package com.jackyblackson.idunntemplates.backend.dto.commercial;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CompProductRespData {
    private int count;
    private List<CompProduct> item;
}
