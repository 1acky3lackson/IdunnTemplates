package com.jackyblackson.idunntemplates.backend.dto.commercial;

import lombok.Data;

import java.util.List;

@Data
public class PeProductCommentRespData {
    private int count;
    private List<PeProductComment> data;
}
