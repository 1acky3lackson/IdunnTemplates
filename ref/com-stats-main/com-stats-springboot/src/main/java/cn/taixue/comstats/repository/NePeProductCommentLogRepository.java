package cn.taixue.comstats.repository;

import cn.taixue.comstats.model.NePeProductCommentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NePeProductCommentLogRepository extends JpaRepository<NePeProductCommentLog, Long> {
}
