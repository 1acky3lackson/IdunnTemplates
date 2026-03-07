package cn.taixue.comstats.dto;

import lombok.Data;

@Data
public class NeResp<T> {
    private String status;
    private String msg;
    private T data;
}
