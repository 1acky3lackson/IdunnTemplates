package com.jackyblackson.idunntemplates.backend.commercial.service;

import com.jackyblackson.idunntemplates.backend.commercial.dto.checkout.ContributionDto;
import com.jackyblackson.idunntemplates.backend.commercial.entity.Project;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.UserProjectContribution;
import com.jackyblackson.idunntemplates.backend.commercial.repository.ProjectRepository;
import com.jackyblackson.idunntemplates.backend.commercial.repository.chekout.UserProjectContributionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserProjectContributionService {

    @Autowired
    private UserProjectContributionRepository contributionRepository;

    @Autowired
    private ProjectRepository projectRepository;

    /**
     * 获取当前项目下的所有活跃贡献记录（不分角色，仅本项目）
     */
    public List<UserProjectContribution> getAllActiveContributions(Long projectId) {
        return contributionRepository.findByProjectIdAndDeleteTimeMsIsNull(projectId);
    }

    /**
     * 添加新记录，并根据角色自动重算对应范围内的占比
     */
    @Transactional
    public void addContributionAndRecalculate(Long projectId, ContributionDto.AddRequest request, String username) {
        // 确定记录归属的项目
        Project targetProject;
        if (request.getRole() == UserProjectContribution.RoleType.BUILDER) {
            // BUILDER 必须关联到父项目
            Project currentProject = projectRepository.findById(projectId)
                    .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectId));
            if (currentProject.getParentProject() == null) {
                throw new IllegalStateException("Cannot add BUILDER to a project without parent project.");
            }
            targetProject = currentProject.getParentProject();
        } else {
            // MODIFIER 或 UPLOADER 关联到当前项目
            targetProject = projectRepository.findById(projectId)
                    .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectId));
        }

        // 创建新记录
        UserProjectContribution newRecord = new UserProjectContribution();
        newRecord.setProject(targetProject);
        newRecord.setUsername(request.getUsername());
        newRecord.setRole(request.getRole());
        newRecord.setContributePoints(request.getContributePoints());
        newRecord.setComment(request.getComment());
        newRecord.setCreateUsername(username);
        newRecord.setCreateTimeMs(String.valueOf(System.currentTimeMillis()));

        contributionRepository.save(newRecord);

        // 重算该角色在目标项目下的占比
        recalculateAndSaveRatiosForRole(targetProject.getId(), request.getRole());
    }

    /**
     * 为指定项目下的特定角色重新计算并保存贡献占比
     */
    private void recalculateAndSaveRatiosForRole(Long projectId, UserProjectContribution.RoleType role) {
        // 仅查询该项目下该角色的活跃记录
        List<UserProjectContribution> records = contributionRepository
                .findByProjectIdAndRoleAndDeleteTimeMsIsNull(projectId, role);

        int totalPoints = records.stream()
                .mapToInt(c -> c.getContributePoints() != null ? c.getContributePoints() : 0)
                .sum();

        for (UserProjectContribution record : records) {
            if (totalPoints == 0) {
                record.setContributeRatio(0.0);
            } else {
                double ratio = (double) (record.getContributePoints() != null ? record.getContributePoints() : 0) / totalPoints;
                record.setContributeRatio(ratio);
            }
        }

        contributionRepository.saveAll(records);
    }

    /**
     * 预览重新计算所有角色的占比（按角色分组，BUILDER 取父项目，其他取本项目）
     */
    public Map<UserProjectContribution.RoleType, ContributionDto.RecalculatePreviewResponse> recalculate(Long projectId) {
        // 获取按角色分组的贡献记录（使用新规则）
        Map<UserProjectContribution.RoleType, List<UserProjectContribution>> grouped = getContributionsGroupedByRole(projectId);

        Map<UserProjectContribution.RoleType, ContributionDto.RecalculatePreviewResponse> result = new EnumMap<>(UserProjectContribution.RoleType.class);

        for (UserProjectContribution.RoleType role : UserProjectContribution.RoleType.values()) {
            List<UserProjectContribution> records = grouped.getOrDefault(role, new ArrayList<>());

            int totalPoints = records.stream()
                    .mapToInt(c -> c.getContributePoints() != null ? c.getContributePoints() : 0)
                    .sum();

            // 模拟计算占比（不保存到数据库）
            records.forEach(record -> {
                if (totalPoints == 0) {
                    record.setContributeRatio(0.0);
                } else {
                    double ratio = (double) (record.getContributePoints() != null ? record.getContributePoints() : 0) / totalPoints;
                    record.setContributeRatio(ratio);
                }
            });

            ContributionDto.RecalculatePreviewResponse response = new ContributionDto.RecalculatePreviewResponse();
            response.setRole(role);
            response.setTotalPoints(totalPoints);
            response.setContributions(records);

            result.put(role, response);
        }

        return result;
    }

    /**
     * 软删除记录，并自动重算对应角色的占比
     */
    @Transactional
    public void softDeleteContribution(Long recordId, String deleteReason, String username) {
        UserProjectContribution record = contributionRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("Contribution not found: " + recordId));

        if (record.getDeleteTimeMs() != null) {
            return; // 已经删除
        }

        record.setDeleteTimeMs(System.currentTimeMillis());
        record.setDeleteReason(deleteReason);
        record.setDeleteUsername(username);

        contributionRepository.save(record);

        // 删除后重算该角色在记录所属项目下的占比
        recalculateAndSaveRatiosForRole(record.getProject().getId(), record.getRole());
    }

    /**
     * 修改分数，并自动重算对应角色的占比
     */
    @Transactional
    public void updateContributionPointsAndRecalculate(Long recordId, Integer newPoints, String username) {
        UserProjectContribution record = contributionRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("Contribution not found: " + recordId));

        if (record.getDeleteTimeMs() != null) {
            throw new IllegalStateException("Cannot update a deleted contribution record.");
        }

        record.setContributePoints(newPoints);
        // 如有需要可记录更新人
        // record.setUpdateUsername(username);

        contributionRepository.save(record);

        // 重算该角色在记录所属项目下的占比
        recalculateAndSaveRatiosForRole(record.getProject().getId(), record.getRole());
    }

    /**
     * 新方法：获取按角色分组的贡献记录（BUILDER 来自父项目，其他来自本项目）
     */
    public Map<UserProjectContribution.RoleType, List<UserProjectContribution>> getContributionsGroupedByRole(Long projectId) {
        Project currentProject = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectId));

        Map<UserProjectContribution.RoleType, List<UserProjectContribution>> result = new EnumMap<>(UserProjectContribution.RoleType.class);

        // 处理 MODIFIER 和 UPLOADER：来自当前项目
        for (UserProjectContribution.RoleType role : Arrays.asList(
                UserProjectContribution.RoleType.MODIFIER,
                UserProjectContribution.RoleType.UPLOADER)) {
            List<UserProjectContribution> list = contributionRepository
                    .findByProjectIdAndRoleAndDeleteTimeMsIsNull(projectId, role);
            result.put(role, list);
        }

        // 处理 BUILDER：来自父项目（如果存在）
        if (currentProject.getParentProject() != null) {
            Long parentId = currentProject.getParentProject().getId();
            List<UserProjectContribution> builders = contributionRepository
                    .findByProjectIdAndRoleAndDeleteTimeMsIsNull(parentId, UserProjectContribution.RoleType.BUILDER);
            result.put(UserProjectContribution.RoleType.BUILDER, builders);
        } else {
            result.put(UserProjectContribution.RoleType.BUILDER, new ArrayList<>());
        }

        return result;
    }
}