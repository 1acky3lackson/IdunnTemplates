package com.jackyblackson.idunntemplates.backend.commercial.service;

import com.jackyblackson.idunntemplates.backend.commercial.dto.checkout.ContributionDto;
import com.jackyblackson.idunntemplates.backend.commercial.entity.Project;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.CommercialRoleType;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.UserProjectContribution;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseProduct;
import com.jackyblackson.idunntemplates.backend.commercial.repository.ProjectRepository;
import com.jackyblackson.idunntemplates.backend.commercial.repository.chekout.UserProjectContributionRepository;
import com.jackyblackson.idunntemplates.backend.commercial.repository.netease.NeteaseProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class UserProjectContributionService {

    @Autowired
    private UserProjectContributionRepository contributionRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private NeteaseProductRepository productRepository;

    public List<UserProjectContribution> getAllActiveProjectContributions(Long projectId) {
        return contributionRepository.findByProjectIdAndRoleAndDeleteTimeMsIsNull(projectId, CommercialRoleType.BUILDER);
    }

    public List<UserProjectContribution> getAllActiveProductContributions(Long productId) {
        return contributionRepository.findByProductIdAndDeleteTimeMsIsNull(productId);
    }

    @Transactional
    public void addProjectContributionAndRecalculate(Long projectId, ContributionDto.AddRequest request, String username) {
        if (request.getRole() != CommercialRoleType.BUILDER) {
            throw new IllegalArgumentException("Project contributions only support BUILDER role");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectId));

        UserProjectContribution record = new UserProjectContribution();
        record.setProject(project);
        record.setProduct(null);
        record.setUsername(request.getUsername());
        record.setRole(request.getRole());
        record.setContributePoints(request.getContributePoints());
        record.setComment(request.getComment());
        record.setCreateUsername(username);
        record.setCreateTimeMs(String.valueOf(System.currentTimeMillis()));

        contributionRepository.save(record);
        recalculateAndSaveRatiosForProjectRole(projectId, request.getRole());
    }

    @Transactional
    public void addProductContributionAndRecalculate(Long productId, ContributionDto.AddRequest request, String username) {
        if (request.getRole() == CommercialRoleType.BUILDER) {
            throw new IllegalArgumentException("Product contributions do not support BUILDER role");
        }

        NeteaseProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        UserProjectContribution record = new UserProjectContribution();
        record.setProject(null);
        record.setProduct(product);
        record.setUsername(request.getUsername());
        record.setRole(request.getRole());
        record.setContributePoints(request.getContributePoints());
        record.setComment(request.getComment());
        record.setCreateUsername(username);
        record.setCreateTimeMs(String.valueOf(System.currentTimeMillis()));

        contributionRepository.save(record);
        recalculateAndSaveRatiosForProductRole(productId, request.getRole());
    }

    private void recalculateAndSaveRatiosForProjectRole(Long projectId, CommercialRoleType role) {
        List<UserProjectContribution> records = contributionRepository
                .findByProjectIdAndRoleAndDeleteTimeMsIsNull(projectId, role);
        updateRatios(records);
        contributionRepository.saveAll(records);
    }

    private void recalculateAndSaveRatiosForProductRole(Long productId, CommercialRoleType role) {
        List<UserProjectContribution> records = contributionRepository
                .findByProductIdAndRoleAndDeleteTimeMsIsNull(productId, role);
        updateRatios(records);
        contributionRepository.saveAll(records);
    }

    private void updateRatios(List<UserProjectContribution> records) {
        int totalPoints = records.stream()
                .mapToInt(c -> c.getContributePoints() != null ? c.getContributePoints() : 0)
                .sum();

        for (UserProjectContribution record : records) {
            if (totalPoints == 0) {
                record.setContributeRatio(0.0);
            } else {
                double ratio = (double) (record.getContributePoints() != null ? record.getContributePoints() : 0)
                        / totalPoints;
                record.setContributeRatio(ratio);
            }
        }
    }

    public Map<CommercialRoleType, ContributionDto.RecalculatePreviewResponse> recalculateProject(Long projectId) {
        return buildPreviewResponse(getProjectContributionsGroupedByRole(projectId));
    }

    public Map<CommercialRoleType, ContributionDto.RecalculatePreviewResponse> recalculateProduct(Long productId) {
        return buildPreviewResponse(getProductContributionsGroupedByRole(productId));
    }

    private Map<CommercialRoleType, ContributionDto.RecalculatePreviewResponse> buildPreviewResponse(
            Map<CommercialRoleType, List<UserProjectContribution>> grouped) {
        Map<CommercialRoleType, ContributionDto.RecalculatePreviewResponse> result =
                new EnumMap<>(CommercialRoleType.class);

        for (CommercialRoleType role : CommercialRoleType.values()) {
            List<UserProjectContribution> records = new ArrayList<>(grouped.getOrDefault(role, new ArrayList<>()));
            updateRatios(records);

            ContributionDto.RecalculatePreviewResponse response = new ContributionDto.RecalculatePreviewResponse();
            response.setRole(role);
            response.setTotalPoints(records.stream()
                    .mapToInt(c -> c.getContributePoints() != null ? c.getContributePoints() : 0)
                    .sum());
            response.setContributions(records);
            result.put(role, response);
        }

        return result;
    }

    @Transactional
    public void softDeleteContribution(Long recordId, String deleteReason, String username) {
        UserProjectContribution record = contributionRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("Contribution not found: " + recordId));

        if (record.getDeleteTimeMs() != null) {
            return;
        }

        record.setDeleteTimeMs(System.currentTimeMillis());
        record.setDeleteReason(deleteReason);
        record.setDeleteUsername(username);
        contributionRepository.save(record);

        if (record.getProject() != null) {
            recalculateAndSaveRatiosForProjectRole(record.getProject().getId(), record.getRole());
        }
        if (record.getProduct() != null) {
            recalculateAndSaveRatiosForProductRole(record.getProduct().getId(), record.getRole());
        }
    }

    @Transactional
    public void updateContributionPointsAndRecalculate(Long recordId, Integer newPoints, String username) {
        UserProjectContribution record = contributionRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("Contribution not found: " + recordId));

        if (record.getDeleteTimeMs() != null) {
            throw new IllegalStateException("Cannot update a deleted contribution record.");
        }

        record.setContributePoints(newPoints);
        contributionRepository.save(record);

        if (record.getProject() != null) {
            recalculateAndSaveRatiosForProjectRole(record.getProject().getId(), record.getRole());
        }
        if (record.getProduct() != null) {
            recalculateAndSaveRatiosForProductRole(record.getProduct().getId(), record.getRole());
        }
    }

    public Map<CommercialRoleType, List<UserProjectContribution>> getProjectContributionsGroupedByRole(Long projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectId));

        Map<CommercialRoleType, List<UserProjectContribution>> result = emptyRoleMap();
        result.put(
                CommercialRoleType.BUILDER,
                contributionRepository.findByProjectIdAndRoleAndDeleteTimeMsIsNull(projectId, CommercialRoleType.BUILDER)
        );
        return result;
    }

    public Map<CommercialRoleType, List<UserProjectContribution>> getProductContributionsGroupedByRole(Long productId) {
        NeteaseProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        if (product.getProject() == null) {
            throw new IllegalStateException("Product has no project: " + productId);
        }

        Map<CommercialRoleType, List<UserProjectContribution>> result = emptyRoleMap();
        result.put(
                CommercialRoleType.BUILDER,
                contributionRepository.findByProjectIdAndRoleAndDeleteTimeMsIsNull(
                        product.getProject().getId(),
                        CommercialRoleType.BUILDER
                )
        );
        result.put(
                CommercialRoleType.MODIFIER,
                contributionRepository.findByProductIdAndRoleAndDeleteTimeMsIsNull(productId, CommercialRoleType.MODIFIER)
        );
        result.put(
                CommercialRoleType.UPLOADER,
                contributionRepository.findByProductIdAndRoleAndDeleteTimeMsIsNull(productId, CommercialRoleType.UPLOADER)
        );
        return result;
    }

    private Map<CommercialRoleType, List<UserProjectContribution>> emptyRoleMap() {
        Map<CommercialRoleType, List<UserProjectContribution>> result = new EnumMap<>(CommercialRoleType.class);
        for (CommercialRoleType role : CommercialRoleType.values()) {
            result.put(role, new ArrayList<>());
        }
        return result;
    }

    public Page<Project> getUserParticipatedProjects(String username, Specification<Project> spec, Pageable pageable) {
        Specification<Project> participantSpec = (root, query, cb) -> {
            Root<UserProjectContribution> contributionRoot = query.from(UserProjectContribution.class);

            Predicate userMatches = cb.equal(contributionRoot.get("username"), username);
            Predicate contributionNotDeleted = cb.isNull(contributionRoot.get("deleteTimeMs"));

            Predicate directBuilder = cb.and(
                    cb.equal(contributionRoot.get("project"), root),
                    cb.equal(contributionRoot.get("role"), CommercialRoleType.BUILDER)
            );
            Predicate productParticipant = cb.and(
                    cb.equal(contributionRoot.get("product").get("project"), root),
                    contributionRoot.get("role").in(Arrays.asList(
                            CommercialRoleType.MODIFIER,
                            CommercialRoleType.UPLOADER
                    ))
            );

            query.distinct(true);
            return cb.and(userMatches, contributionNotDeleted, cb.or(directBuilder, productParticipant));
        };

        Specification<Project> combinedSpec = Specification.where(spec).and(participantSpec);
        return projectRepository.findAll(combinedSpec, pageable);
    }

    public boolean isUserParticipant(String username, Long projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectId));

        boolean isBuilder = contributionRepository.existsByUsernameAndProjectIdInAndRoleInAndDeleteTimeMsIsNull(
                username,
                Collections.singletonList(projectId),
                Collections.singletonList(CommercialRoleType.BUILDER)
        );
        if (isBuilder) {
            return true;
        }

        return contributionRepository.existsByUsernameAndProduct_Project_IdInAndRoleInAndDeleteTimeMsIsNull(
                username,
                Collections.singletonList(projectId),
                Arrays.asList(CommercialRoleType.MODIFIER, CommercialRoleType.UPLOADER)
        );
    }

    public boolean isUserParticipantInProduct(String username, Long productId) {
        NeteaseProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        if (product.getProject() != null) {
            boolean isBuilder = contributionRepository.existsByUsernameAndProjectIdInAndRoleInAndDeleteTimeMsIsNull(
                    username,
                    Collections.singletonList(product.getProject().getId()),
                    Collections.singletonList(CommercialRoleType.BUILDER)
            );
            if (isBuilder) {
                return true;
            }
        }

        return contributionRepository.existsByUsernameAndProductIdInAndRoleInAndDeleteTimeMsIsNull(
                username,
                Collections.singletonList(productId),
                Arrays.asList(CommercialRoleType.MODIFIER, CommercialRoleType.UPLOADER)
        );
    }

    public boolean isUserParticipantInAnyProduct(String username, Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return false;
        }
        return contributionRepository.existsByUsernameAndProductIdInAndRoleInAndDeleteTimeMsIsNull(
                username,
                productIds,
                Arrays.asList(CommercialRoleType.MODIFIER, CommercialRoleType.UPLOADER)
        );
    }
}
