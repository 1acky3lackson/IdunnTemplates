package com.jackyblackson.idunntemplates.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PathDto {
    private String path;
    private boolean canSave;
    private boolean canCommit;
    private boolean canUse;
}
