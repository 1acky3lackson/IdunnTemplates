package com.jackyblackson.idunntemplates.backend.commercial.repository.chekout;

import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.GlobalCheckoutParamContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GlobalCheckoutParamContextRepository extends JpaRepository<GlobalCheckoutParamContext, Long> {
    /**
     * 查找当前有效的（未被禁用）参数配置。
     * 由于业务上只允许一条有效记录，该方法最多返回一条。
     */
    Optional<GlobalCheckoutParamContext> findFirstByDisableTimeMsIsNull();

    /**
     * 获取所有记录，按创建时间降序排列（最新的在前）
     */
    List<GlobalCheckoutParamContext> findAllByOrderByCreateTimeMsDesc();
}
