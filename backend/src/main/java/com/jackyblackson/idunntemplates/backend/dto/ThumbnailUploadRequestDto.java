package com.jackyblackson.idunntemplates.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThumbnailUploadRequestDto {
    private String token;
    private String image;
}
