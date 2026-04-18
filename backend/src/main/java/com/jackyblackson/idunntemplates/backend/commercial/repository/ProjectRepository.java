package com.jackyblackson.idunntemplates.backend.commercial.repository;

import com.jackyblackson.idunntemplates.backend.commercial.entity.Project;
import org.hibernate.query.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("SELECT p FROM Project p " +
            "WHERE p.deleteTimeMs IS NULL " +
            "AND p.world.id = :worldId " +
            "AND p.minX IS NOT NULL AND p.maxX IS NOT NULL " +
            "AND p.minY IS NOT NULL AND p.maxY IS NOT NULL " +
            "AND p.minZ IS NOT NULL AND p.maxZ IS NOT NULL " +
            "AND p.minX <= :maxX AND p.maxX >= :minX " +
            "AND p.minY <= :maxY AND p.maxY >= :minY " +
            "AND p.minZ <= :maxZ AND p.maxZ >= :minZ")
    java.util.List<Project> findOverlappingProjects(
            @Param("worldId") Long worldId,
            @Param("minX") Integer minX,
            @Param("minY") Integer minY,
            @Param("minZ") Integer minZ,
            @Param("maxX") Integer maxX,
            @Param("maxY") Integer maxY,
            @Param("maxZ") Integer maxZ
    );
}
