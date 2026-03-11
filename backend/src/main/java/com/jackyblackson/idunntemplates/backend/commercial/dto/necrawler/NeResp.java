package com.jackyblackson.idunntemplates.backend.commercial.dto.necrawler;

import lombok.Data;

@Data
public class NeResp<T> {
    private String status;
    private String msg;
    private T data;
}
