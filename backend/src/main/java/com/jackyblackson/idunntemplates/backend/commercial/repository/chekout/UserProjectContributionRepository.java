package com.jackyblackson.idunntemplates.backend.commercial.repository.chekout;

import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.CommercialRoleType;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.UserProjectContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProjectContributionRepository extends JpaRepository<UserProjectContribution, Long> {

    // 1. 查询指定项目（及其父项目）下所有未删除的记录
    List<UserProjectContribution> findByProjectIdInAndDeleteTimeMsIsNull(List<Long> projectIds);

    // 2. 查询指定项目（及其父项目）下，特定 Role 的所有未删除记录
    List<UserProjectContribution> findByProjectIdInAndRoleAndDeleteTimeMsIsNull(List<Long> projectIds, CommercialRoleType role);
    List<UserProjectContribution> findByProjectIdAndDeleteTimeMsIsNull(Long projectId);
    List<UserProjectContribution> findByProjectIdAndRoleAndDeleteTimeMsIsNull(Long projectId, CommercialRoleType role);
}
