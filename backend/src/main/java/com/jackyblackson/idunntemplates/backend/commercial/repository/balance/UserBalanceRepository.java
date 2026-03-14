package com.jackyblackson.idunntemplates.backend.commercial.repository.balance;

import com.jackyblackson.idunntemplates.backend.commercial.entity.balance.UserBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface UserBalanceRepository extends JpaRepository<UserBalance, Long> {

    Optional<UserBalance> findByUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ub FROM UserBalance ub WHERE ub.username = :username")
    Optional<UserBalance> findByUsernameWithLock(@Param("username") String username);
}