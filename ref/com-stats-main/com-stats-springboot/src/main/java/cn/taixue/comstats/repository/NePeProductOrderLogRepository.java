package cn.taixue.comstats.repository;

import cn.taixue.comstats.model.NePeProductOrderLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NePeProductOrderLogRepository extends JpaRepository<NePeProductOrderLog, Long> {
}
