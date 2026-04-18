package com.jackyblackson.idunntemplates.backend.commercial.repository.chekout;

import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.CommercialRoleType;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.UserProjectContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserProjectContributionRepository extends JpaRepository<UserProjectContribution, Long> {

    List<UserProjectContribution> findByProjectIdAndDeleteTimeMsIsNull(Long projectId);
    List<UserProjectContribution> findByProjectIdAndRoleAndDeleteTimeMsIsNull(Long projectId, CommercialRoleType role);
    List<UserProjectContribution> findByProductIdAndDeleteTimeMsIsNull(Long productId);
    List<UserProjectContribution> findByProductIdAndRoleAndDeleteTimeMsIsNull(Long productId, CommercialRoleType role);

    boolean existsByUsernameAndProjectIdInAndRoleInAndDeleteTimeMsIsNull(
            String username,
            Collection<Long> projectIds,
            Collection<CommercialRoleType> roles
    );

    boolean existsByUsernameAndProductIdInAndRoleInAndDeleteTimeMsIsNull(
            String username,
            Collection<Long> productIds,
            Collection<CommercialRoleType> roles
    );

    boolean existsByUsernameAndProduct_Project_IdInAndRoleInAndDeleteTimeMsIsNull(
            String username,
            Collection<Long> projectIds,
            Collection<CommercialRoleType> roles
    );
}
