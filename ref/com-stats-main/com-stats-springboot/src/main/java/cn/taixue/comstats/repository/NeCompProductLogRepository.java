package cn.taixue.comstats.repository;

import cn.taixue.comstats.model.NeCompProductLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NeCompProductLogRepository extends JpaRepository<NeCompProductLog, Long> {
}
