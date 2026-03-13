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
import java.util.stream.Collectors;

@Service
public class UserProjectContributionService {

    @Autowired
    private UserProjectContributionRepository contributionRepository;

    @Autowired
    private ProjectRepository projectRepository;

    /**
     * 获取 Project 及其可能存在的 ParentProject 的 ID 列表
     */
    private List<Long> getRelevantProjectIds(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectId));

        List<Long> ids = new ArrayList<>();
        ids.add(project.getId());
        if (project.getParentProject() != null) {
            ids.add(project.getParentProject().getId());
        }
        return ids;
    }

    /**
     * 1) 获取该项目及其父项目下所有未删除的记录
     */
    public List<UserProjectContribution> getAllActiveContributions(Long projectId) {
        List<Long> projectIds = getRelevantProjectIds(projectId);
        return contributionRepository.findByProjectIdInAndDeleteTimeMsIsNull(projectIds);
    }

    /**
     * 2) 添加新记录，并重新计算保存该 Role 的占比
     */
    @Transactional
    public void addContributionAndRecalculate(Long projectId, ContributionDto.AddRequest request, String username) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectId));

        // 插入新记录
        UserProjectContribution newRecord = new UserProjectContribution();
        newRecord.setProject(project);
        newRecord.setUsername(request.getUsername());
        newRecord.setRole(request.getRole());
        newRecord.setContributePoints(request.getContributePoints());
        newRecord.setComment(request.getComment());
        newRecord.setCreateUsername(username);
        // 注意：你的实体类中 createTimeMs 定义为 String 类型
        newRecord.setCreateTimeMs(String.valueOf(System.currentTimeMillis()));

        contributionRepository.save(newRecord);

        // 重新计算并保存该 Role 的占比
        recalculateAndSaveRatiosForRole(projectId, request.getRole());
    }

    /**
     * 内部方法：为特定 Role 重新计算并保存 ContributeRatio
     */
    private void recalculateAndSaveRatiosForRole(Long projectId, UserProjectContribution.RoleType role) {
        List<Long> projectIds = getRelevantProjectIds(projectId);
        List<UserProjectContribution> records = contributionRepository
                .findByProjectIdInAndRoleAndDeleteTimeMsIsNull(projectIds, role);

        // 统计总分
        int totalPoints = records.stream()
                .mapToInt(c -> c.getContributePoints() != null ? c.getContributePoints() : 0)
                .sum();

        // 计算并更新占比
        for (UserProjectContribution record : records) {
            if (totalPoints == 0) {
                record.setContributeRatio(0.0);
            } else {
                double ratio = (double) (record.getContributePoints() != null ? record.getContributePoints() : 0) / totalPoints;
                record.setContributeRatio(ratio);
            }
        }

        // 批量保存
        contributionRepository.saveAll(records);
    }

    /**
     * 3) 重新计算所有 Role 的占比，并返回计算过程数据（不落库，仅供预览）
     */
    public Map<UserProjectContribution.RoleType, ContributionDto.RecalculatePreviewResponse> previewRecalculation(Long projectId) {
        List<Long> projectIds = getRelevantProjectIds(projectId);
        List<UserProjectContribution> allRecords = contributionRepository
                .findByProjectIdInAndDeleteTimeMsIsNull(projectIds);

        // 按 Role 分组
        Map<UserProjectContribution.RoleType, List<UserProjectContribution>> groupedByRole = allRecords.stream()
                .collect(Collectors.groupingBy(UserProjectContribution::getRole));

        Map<UserProjectContribution.RoleType, ContributionDto.RecalculatePreviewResponse> result = new EnumMap<>(UserProjectContribution.RoleType.class);

        // 遍历所有可能的 Role 保证数据完整
        for (UserProjectContribution.RoleType role : UserProjectContribution.RoleType.values()) {
            List<UserProjectContribution> records = groupedByRole.getOrDefault(role, new ArrayList<>());

            int totalPoints = records.stream()
                    .mapToInt(c -> c.getContributePoints() != null ? c.getContributePoints() : 0)
                    .sum();

            // 模拟计算占比写入对象中（不调用 save）
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
     * 4) 软删除记录（并在删除后自动重算该 Role 的占比以保证一致性）
     */
    @Transactional
    public void softDeleteContribution(Long recordId, String deleteReason, String username) {
        UserProjectContribution record = contributionRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("Contribution not found: " + recordId));

        if (record.getDeleteTimeMs() != null) {
            return; // 已经删除了
        }

        record.setDeleteTimeMs(System.currentTimeMillis());
        record.setDeleteReason(deleteReason);
        record.setDeleteUsername(username);

        contributionRepository.save(record);

        // 删除后，剩下的记录占比加起来就不等于 1 了，因此需要触发重算
        recalculateAndSaveRatiosForRole(record.getProject().getId(), record.getRole());
    }

    /**
     * 5) 修改分数并重新计算该 Role 的占比
     */
    @Transactional
    public void updateContributionPointsAndRecalculate(Long recordId, Integer newPoints, String username) {
        UserProjectContribution record = contributionRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("Contribution not found: " + recordId));

        // 验证没有被标记为删除
        if (record.getDeleteTimeMs() != null) {
            throw new IllegalStateException("Cannot update a deleted contribution record.");
        }

        // 更新分数
        record.setContributePoints(newPoints);

        // 如果你的实体类有记录最后更新人的需求，可以在这里 set
        // record.setUpdateUsername(username);

        contributionRepository.save(record);

        // 复用之前的逻辑：重新计算并保存该 Role 的占比
        recalculateAndSaveRatiosForRole(record.getProject().getId(), record.getRole());
    }
}
