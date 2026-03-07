package com.jackyblackson.idunntemplates.backend.dto.commercial;

import lombok.Data;

@Data
public class NeResp<T> {
    private String status;
    private String msg;
    private T data;
}
