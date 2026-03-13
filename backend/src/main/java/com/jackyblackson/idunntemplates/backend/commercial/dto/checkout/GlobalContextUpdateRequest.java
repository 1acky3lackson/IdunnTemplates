package com.jackyblackson.idunntemplates.backend.commercial.dto.checkout;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GlobalContextUpdateRequest {
    private Double taixueRatio;
    private Double commercialRatio;
    private Double templateDefectParam;
    private Double placerRatio;
    private Double uploaderRatio;

    @NotBlank(message = "更新原因不能为空")
    private String updateReason;
}
