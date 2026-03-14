package com.jackyblackson.idunntemplates.backend.commercial.repository.balance;

import com.jackyblackson.idunntemplates.backend.commercial.entity.balance.UserBalanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserBalanceRecordRepository extends JpaRepository<UserBalanceRecord, Long>, JpaSpecificationExecutor<UserBalanceRecord> {
}