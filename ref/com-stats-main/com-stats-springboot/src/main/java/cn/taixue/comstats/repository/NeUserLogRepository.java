package cn.taixue.comstats.repository;

import cn.taixue.comstats.model.NeUserLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NeUserLogRepository extends JpaRepository<NeUserLog, Long> {
}
