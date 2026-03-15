package com.jackyblackson.idunntemplates.backend.commercial.repository.withdraw;

import com.jackyblackson.idunntemplates.backend.commercial.entity.withdraw.SystemWithdraw;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemWithdrawRepository extends JpaRepository<SystemWithdraw, Long>, JpaSpecificationExecutor<SystemWithdraw> {
    Page<SystemWithdraw> findByUsername(String username, Pageable pageable);
}
