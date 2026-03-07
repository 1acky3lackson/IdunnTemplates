package com.jackyblackson.idunntemplates.backend.dto.commercial;

import lombok.Data;

import java.util.List;

@Data
public class PeProductFeedbackRespData {
    private int count;
    private List<PeProductFeedback> data;
}
