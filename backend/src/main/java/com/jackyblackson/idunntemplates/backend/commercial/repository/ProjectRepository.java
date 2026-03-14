package com.jackyblackson.idunntemplates.backend.commercial.repository;

import com.jackyblackson.idunntemplates.backend.commercial.entity.Project;
import org.hibernate.query.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;

public interface ProjectRepository extends
        JpaRepository<Project, Long>,
        JpaSpecificationExecutor<Project>
{
    /**
     * 优化后的查询：通过 JOIN 关联贡献表，筛选特定用户的项目
     * 使用 JOIN 而不是子查询，能更好地利用索引
     */
    @Query("SELECT DISTINCT p FROM Project p " +
            "JOIN UserProjectContribution c ON c.project.id = p.id " +
            "WHERE c.username = :username " +
            "AND c.deleteTimeMs IS NULL " +
            "AND p.deleteTimeMs IS NULL")
    org.springframework.data.domain.Page<Project> findByContributorUsername(@Param("username") String username,
                                                                            org.springframework.data.domain.Pageable pageable);
}
