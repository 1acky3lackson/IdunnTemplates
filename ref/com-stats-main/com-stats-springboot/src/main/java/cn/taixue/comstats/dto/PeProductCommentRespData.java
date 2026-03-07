package cn.taixue.comstats.dto;

import lombok.Data;

import java.util.List;

@Data
public class PeProductCommentRespData {
    private int count;
    private List<PeProductComment> item;
}
