package com.jackyblackson.idunntemplates.backend.commercial.repository.balance;

import com.jackyblackson.idunntemplates.backend.commercial.entity.balance.UserBalanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBalanceRecordRepository extends JpaRepository<UserBalanceRecord, Long> {
}