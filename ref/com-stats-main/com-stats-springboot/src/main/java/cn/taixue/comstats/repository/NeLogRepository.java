package cn.taixue.comstats.repository;

import cn.taixue.comstats.model.NeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NeLogRepository extends JpaRepository<NeLog, Long> {
}
