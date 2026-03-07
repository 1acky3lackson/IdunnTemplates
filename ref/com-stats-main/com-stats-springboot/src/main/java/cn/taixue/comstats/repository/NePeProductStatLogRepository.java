package cn.taixue.comstats.repository;

import cn.taixue.comstats.model.NePeProductStatLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NePeProductStatLogRepository extends JpaRepository<NePeProductStatLog, Long> {
}
