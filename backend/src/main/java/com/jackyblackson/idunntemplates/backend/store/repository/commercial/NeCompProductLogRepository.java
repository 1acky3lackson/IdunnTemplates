package com.jackyblackson.idunntemplates.backend.store.repository.commercial;

import com.jackyblackson.idunntemplates.backend.domain.commercial.NeCompProductLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NeCompProductLogRepository extends JpaRepository<NeCompProductLog, Long> {
}
