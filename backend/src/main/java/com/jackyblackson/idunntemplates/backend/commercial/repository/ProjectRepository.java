package com.jackyblackson.idunntemplates.backend.commercial.repository;

import com.jackyblackson.idunntemplates.backend.commercial.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProjectRepository extends
        JpaRepository<Project, Long>,
        JpaSpecificationExecutor<Project>
{
}
