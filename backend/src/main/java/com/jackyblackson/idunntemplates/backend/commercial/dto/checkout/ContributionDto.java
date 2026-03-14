package com.jackyblackson.idunntemplates.backend.commercial.dto.checkout;

import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.CommercialRoleType;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.UserProjectContribution;
import lombok.Data;
import java.util.List;

public class ContributionDto {

    @Data
    public static class AddRequest {
        private CommercialRoleType role;
        private String username;
        private Integer contributePoints;
        private String comment;
    }

    @Data
    public static class DeleteRequest {
        private String deleteReason;
    }

    @Data
    public static class RecalculatePreviewResponse {
        private CommercialRoleType role;
        private Integer totalPoints;
        // 包含重新计算了 ratio 但未保存到数据库的记录列表，供前端展示
        private List<UserProjectContribution> contributions;
    }

    @Data
    public static class UpdateRequest {
        // 根据需要可以添加 @NotNull 等校验注解
        private Integer contributePoints;

        // 如果你希望同时允许修改备注，也可以加上 comment
        private String comment;
    }
}
