package com.jackyblackson.idunntemplates.backend.commercial.repository;

import com.jackyblackson.idunntemplates.backend.commercial.entity.World;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WorldRepository extends
        JpaRepository<World, Long>,
        JpaSpecificationExecutor<World>
{
    java.util.Optional<World> findFirstByMountNameOrName(String mountName, String name);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(MAX(w.id), 0) FROM World w")
    Long findMaxId();
}
