package com.jackyblackson.idunntemplates.backend.dto.commercial;

import lombok.Data;

import java.util.List;

@Data
public class PeProductRespData {
    private int count;
    private List<PeProduct> item;
}
