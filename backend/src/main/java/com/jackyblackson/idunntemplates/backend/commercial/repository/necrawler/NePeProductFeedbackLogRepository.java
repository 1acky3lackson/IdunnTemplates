package com.jackyblackson.idunntemplates.backend.commercial.repository.necrawler;

import com.jackyblackson.idunntemplates.backend.commercial.entity.crawler.NePeProductFeedbackLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NePeProductFeedbackLogRepository extends JpaRepository<NePeProductFeedbackLog, Long> {
}
