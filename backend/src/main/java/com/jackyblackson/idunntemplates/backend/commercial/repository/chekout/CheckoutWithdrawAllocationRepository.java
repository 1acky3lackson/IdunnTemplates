package com.jackyblackson.idunntemplates.backend.commercial.repository.chekout;

import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.CheckoutWithdrawAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CheckoutWithdrawAllocationRepository extends
        JpaRepository<CheckoutWithdrawAllocation, Long>,
        JpaSpecificationExecutor<CheckoutWithdrawAllocation>
{

}
