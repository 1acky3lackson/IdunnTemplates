package cn.taixue.comstats.repository;

import cn.taixue.comstats.model.NePeProductLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NePeProductLogRepository extends JpaRepository<NePeProductLog, Long> {
}
