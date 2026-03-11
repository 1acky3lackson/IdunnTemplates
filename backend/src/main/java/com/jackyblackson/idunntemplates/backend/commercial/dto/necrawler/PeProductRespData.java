package com.jackyblackson.idunntemplates.backend.commercial.dto.necrawler;

import lombok.Data;

import java.util.List;

@Data
public class PeProductRespData {
    private int count;
    private List<PeProduct> item;
}
