package cn.taixue.comstats.repository;

import cn.taixue.comstats.model.NePeProductFeedbackLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NePeProductFeedbackLogRepository extends JpaRepository<NePeProductFeedbackLog, Long> {
}
