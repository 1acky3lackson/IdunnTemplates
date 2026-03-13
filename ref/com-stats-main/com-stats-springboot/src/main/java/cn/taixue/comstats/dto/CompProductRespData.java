package cn.taixue.comstats.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CompProductRespData {
    private int count;
    private List<CompProduct> item;
}
