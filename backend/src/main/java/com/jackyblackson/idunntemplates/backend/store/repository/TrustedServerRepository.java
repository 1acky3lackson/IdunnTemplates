package com.jackyblackson.idunntemplates.backend.store.repository;

import com.jackyblackson.idunntemplates.backend.domain.TrustedServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrustedServerRepository extends JpaRepository<TrustedServer, Long> {
    Optional<TrustedServer> findByToken(String token);
}
