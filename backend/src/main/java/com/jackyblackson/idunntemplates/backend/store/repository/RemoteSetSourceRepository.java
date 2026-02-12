package com.jackyblackson.idunntemplates.backend.store.repository;

import com.jackyblackson.idunntemplates.backend.domain.RemoteSetSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RemoteSetSourceRepository extends JpaRepository<RemoteSetSource, Long> {
    List<RemoteSetSource> findByTargetSet_Id(Long targetSetId);
    List<RemoteSetSource> findByRemoteSet_Id(Long remoteSetId);
}
