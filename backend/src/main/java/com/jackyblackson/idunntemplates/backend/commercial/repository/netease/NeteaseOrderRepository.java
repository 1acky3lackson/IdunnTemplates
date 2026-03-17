package com.jackyblackson.idunntemplates.backend.commercial.repository.netease;

import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.CheckoutDetail;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseOrder;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface NeteaseOrderRepository extends
        JpaRepository<NeteaseOrder, Long>,
        JpaSpecificationExecutor<NeteaseOrder>
{

    /**
     * 根据 appOrderId 列表查询已存在的订单
     */
    List<NeteaseOrder> findByAppOrderIdIn(List<String> appOrderIds);

    List<NeteaseOrder> findByInternalStatusOrderByIdDesc(NeteaseOrderStatus internalStatus);

    List<NeteaseOrder> findByProductIdOrderByShipTimeMsAsc(Long productId);

    // 支持分页的查询
    Page<NeteaseOrder> findByInternalStatusOrderByIdDesc(NeteaseOrderStatus internalStatus, Pageable pageable);
}
