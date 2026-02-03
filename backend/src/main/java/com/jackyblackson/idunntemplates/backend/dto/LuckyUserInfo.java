package com.jackyblackson.idunntemplates.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LuckyUserInfo {
    private String name;
    private String uuid;
}