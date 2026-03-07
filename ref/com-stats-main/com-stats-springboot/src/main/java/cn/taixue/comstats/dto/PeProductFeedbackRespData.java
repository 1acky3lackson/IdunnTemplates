package cn.taixue.comstats.dto;

import lombok.Data;

import java.util.List;

@Data
public class PeProductFeedbackRespData {
    private int count;
    private List<PeProductFeedback> item;
}
