package com.jackyblackson.idunntemplates.backend.commercial.controller;

import com.jackyblackson.idunntemplates.backend.annotation.AuthRequired;
import com.jackyblackson.idunntemplates.backend.commercial.dto.checkout.ContributionDto;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.CommercialRoleType;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.UserProjectContribution;
import com.jackyblackson.idunntemplates.backend.commercial.service.UserProjectContributionService;
import com.jackyblackson.idunntemplates.backend.dto.UserContext;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/commercial/projects/{projectId}/contributions")
@AllArgsConstructor
public class UserProjectContributionController {

    private final UserProjectContributionService contributionService;

    /**
     * 1) 获取当前项目及其父项目下所有未删除的 Contribution 列表
     */
    @GetMapping
    @AuthRequired
    public ResponseEntity<List<UserProjectContribution>> getContributions(
            @PathVariable Long projectId,
            UserContext userContext
    ) {
        List<UserProjectContribution> list = contributionService.getAllActiveContributions(projectId);
        return ResponseEntity.ok(list);
    }

    /**
     * 2) 添加新记录，并自动重算对应 Role 的占比
     */
    @PostMapping
    @AuthRequired
    public ResponseEntity<Void> addContribution(
            @PathVariable Long projectId,
            @Valid @RequestBody ContributionDto.AddRequest request,
            UserContext userContext
    ) {
        contributionService.addContributionAndRecalculate(projectId, request, userContext.getUsername());
        return ResponseEntity.ok().build();
    }

    /**
     * 3) 预览重新计算的过程（不写入数据库），供前端展示
     */
    @GetMapping("/recalculate")
    @AuthRequired
    public ResponseEntity<Map<CommercialRoleType, ContributionDto.RecalculatePreviewResponse>> recalculation(
            @PathVariable Long projectId,
            UserContext userContext
    ) {
        Map<CommercialRoleType, ContributionDto.RecalculatePreviewResponse> result =
                contributionService.recalculate(projectId);
        return ResponseEntity.ok(result);
    }

    /**
     * 4) 软删除指定记录，并自动重算该 Role 剩余记录的占比
     * 注意：这里把记录 id 放在了路径末尾
     */
    @DeleteMapping("/{contributionId}")
    @AuthRequired
    public ResponseEntity<Void> deleteContribution(
            @PathVariable Long projectId,
            @PathVariable Long contributionId,
            @Valid @RequestBody ContributionDto.DeleteRequest request,
            UserContext userContext
    ) {
        contributionService.softDeleteContribution(contributionId, request.getDeleteReason(), userContext.getUsername());
        return ResponseEntity.ok().build();
    }

    /**
     * 5) 修改指定记录的分数，并自动重算该 Role 记录的占比
     */
    @PatchMapping("/{contributionId}")
    @AuthRequired
    public ResponseEntity<Void> updateContributionPoints(
            @PathVariable Long projectId,
            @PathVariable Long contributionId,
            @Valid @RequestBody ContributionDto.UpdateRequest request,
            UserContext userContext
    ) {
        contributionService.updateContributionPointsAndRecalculate(
                contributionId,
                request.getContributePoints(),
                userContext.getUsername()
        );
        return ResponseEntity.ok().build();
    }

    /**
     * 获取按角色分组的贡献记录
     * - BUILDER 来自父项目（如果存在）
     * - MODIFIER 和 UPLOADER 来自当前项目
     */
    @GetMapping("/grouped")
    @AuthRequired
    public ResponseEntity<Map<CommercialRoleType, List<UserProjectContribution>>> getContributionsGroupedByRole(
            @PathVariable Long projectId,
            UserContext userContext
    ) {
        Map<CommercialRoleType, List<UserProjectContribution>> grouped =
                contributionService.getContributionsGroupedByRole(projectId);
        return ResponseEntity.ok(grouped);
    }
}
