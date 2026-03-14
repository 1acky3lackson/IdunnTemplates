package com.jackyblackson.idunntemplates.backend.commercial.service;

import com.jackyblackson.idunntemplates.backend.commercial.dto.checkout.ContributionDto;
import com.jackyblackson.idunntemplates.backend.commercial.entity.Project;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.CommercialRoleType;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.UserProjectContribution;
import com.jackyblackson.idunntemplates.backend.commercial.repository.ProjectRepository;
import com.jackyblackson.idunntemplates.backend.commercial.repository.chekout.UserProjectContributionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
        if (request.getRole() == CommercialRoleType.BUILDER) {
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
    private void recalculateAndSaveRatiosForRole(Long projectId, CommercialRoleType role) {
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
    public Map<CommercialRoleType, ContributionDto.RecalculatePreviewResponse> recalculate(Long projectId) {
        // 获取按角色分组的贡献记录（使用新规则）
        Map<CommercialRoleType, List<UserProjectContribution>> grouped = getContributionsGroupedByRole(projectId);

        Map<CommercialRoleType, ContributionDto.RecalculatePreviewResponse> result = new EnumMap<>(CommercialRoleType.class);

        for (CommercialRoleType role : CommercialRoleType.values()) {
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
    public Map<CommercialRoleType, List<UserProjectContribution>> getContributionsGroupedByRole(Long projectId) {
        Project currentProject = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectId));

        Map<CommercialRoleType, List<UserProjectContribution>> result = new EnumMap<>(CommercialRoleType.class);

        // 处理 MODIFIER 和 UPLOADER：来自当前项目
        for (CommercialRoleType role : Arrays.asList(
                CommercialRoleType.MODIFIER,
                CommercialRoleType.UPLOADER)) {
            List<UserProjectContribution> list = contributionRepository
                    .findByProjectIdAndRoleAndDeleteTimeMsIsNull(projectId, role);
            result.put(role, list);
        }

        // 处理 BUILDER：来自父项目（如果存在）
        if (currentProject.getParentProject() != null) {
            Long parentId = currentProject.getParentProject().getId();
            List<UserProjectContribution> builders = contributionRepository
                    .findByProjectIdAndRoleAndDeleteTimeMsIsNull(parentId, CommercialRoleType.BUILDER);
            result.put(CommercialRoleType.BUILDER, builders);
        } else {
            result.put(CommercialRoleType.BUILDER, new ArrayList<>());
        }

        return result;
    }

    public Page<Project> getUserParticipatedProjects(String username, Specification<Project> spec, Pageable pageable) {
        Specification<Project> participantSpec = (root, query, cb) -> {
            // 1. 引用贡献表
            Root<UserProjectContribution> contributionRoot = query.from(UserProjectContribution.class);

            // 2. 基本条件：用户名匹配且贡献记录未删除
            Predicate userMatches = cb.equal(contributionRoot.get("username"), username);
            Predicate contributionNotDeleted = cb.isNull(contributionRoot.get("deleteTimeMs"));

            // 3. 核心逻辑：符合以下条件之一即视为参与 (对应 isUserParticipant 的逻辑)

            // 条件 A：本项目直接参与 (MODIFIER, UPLOADER, BUILDER)
            // 注意：你刚才的代码在 direct 检查里也加了 BUILDER，这里保持一致
            Predicate isDirectParticipant = cb.and(
                    cb.equal(contributionRoot.get("project"), root),
                    contributionRoot.get("role").in(
                            CommercialRoleType.MODIFIER,
                            CommercialRoleType.UPLOADER,
                            CommercialRoleType.BUILDER
                    )
            );

            // 条件 B：通过父项目参与 (父项目的 BUILDER)
            // 逻辑：contribution 的 project 等于 root 的 parentProject，且角色是 BUILDER
            Predicate isParentBuilder = cb.and(
                    cb.equal(contributionRoot.get("project"), root.get("parentProject")),
                    cb.equal(contributionRoot.get("role"), CommercialRoleType.BUILDER)
            );

            // 4. 合并参与条件
            Predicate hasParticipation = cb.or(isDirectParticipant, isParentBuilder);

            // 5. 应用 DISTINCT 避免分页总数统计错误
            query.distinct(true);

            return cb.and(userMatches, contributionNotDeleted, hasParticipation);
        };

        // 合并外部传入的 spec（如名称搜索等）
        Specification<Project> combinedSpec = Specification.where(spec).and(participantSpec);

        return projectRepository.findAll(combinedSpec, pageable);
    }

    /**
     * 判断用户是否参与了该项目
     * 参与标准：
     * 1. 在本项目中是 MODIFIER 或 UPLOADER
     * 2. 如果项目有父项目，在父项目中是 BUILDER
     */
    public boolean isUserParticipant(String username, Long projectId) {
        Project currentProject = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectId));

        // 1. 检查本项目角色 (MODIFIER, UPLOADER)
        boolean isDirectParticipant = contributionRepository.existsByUsernameAndProjectIdInAndRoleInAndDeleteTimeMsIsNull(
                username,
                Collections.singletonList(projectId),
                Arrays.asList(CommercialRoleType.MODIFIER, CommercialRoleType.UPLOADER, CommercialRoleType.BUILDER)
        );

        if (isDirectParticipant) return true;

        // 2. 检查父项目角色 (BUILDER)
        if (currentProject.getParentProject() != null) {
            return contributionRepository.existsByUsernameAndProjectIdInAndRoleInAndDeleteTimeMsIsNull(
                    username,
                    Collections.singletonList(currentProject.getParentProject().getId()),
                    Collections.singletonList(CommercialRoleType.BUILDER)
            );
        }

        return false;
    }
}