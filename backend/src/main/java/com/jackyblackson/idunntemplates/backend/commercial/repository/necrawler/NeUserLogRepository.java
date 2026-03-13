package com.jackyblackson.idunntemplates.backend.commercial.repository.necrawler;

import com.jackyblackson.idunntemplates.backend.commercial.entity.crawler.NeUserLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NeUserLogRepository extends JpaRepository<NeUserLog, Long> {
}
