package com.jackyblackson.idunntemplates.backend.commercial.dto.necrawler;

import lombok.Data;

import java.util.List;

@Data
public class PeProductCommentRespData {
    private int count;
    private List<PeProductComment> data;
}
