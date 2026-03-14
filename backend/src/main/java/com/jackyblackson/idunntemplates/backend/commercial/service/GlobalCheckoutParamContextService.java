package com.jackyblackson.idunntemplates.backend.commercial.service;

import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.GlobalCheckoutParamContext;
import com.jackyblackson.idunntemplates.backend.commercial.repository.chekout.GlobalCheckoutParamContextRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Transactional
@AllArgsConstructor
public class GlobalCheckoutParamContextService {
    private final GlobalCheckoutParamContextRepository repository;

    // 应用层锁，保证创建或更新操作的原子性
    private final Lock lock = new ReentrantLock();

    /**
     * 获取当前有效的参数配置。
     * 如果不存在有效记录，则使用默认参数创建一条新记录并返回。
     * @return 有效的 GlobalCheckoutParamContext 对象
     */
    public GlobalCheckoutParamContext getEffectiveConfig() {
        // 先尝试无锁查询，提高性能
        Optional<GlobalCheckoutParamContext> existing = repository.findFirstByDisableTimeMsIsNull();
        if (existing.isPresent()) {
            return existing.get();
        }

        // 无有效记录，加锁并双重检查，防止并发创建多条
        lock.lock();
        try {
            existing = repository.findFirstByDisableTimeMsIsNull();
            if (existing.isPresent()) {
                return existing.get();
            }

            // 创建默认配置，创建人设为 system
            GlobalCheckoutParamContext defaultConfig = new GlobalCheckoutParamContext("system");
            // 其他字段已通过实体字段默认值初始化（如 taixueRatio=0.3 等）
            return repository.save(defaultConfig);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 更新全局参数配置。
     * 将当前有效记录标记为禁用（设置 disableTimeMs 和 disableReason），
     * 然后根据传入的新参数创建一条新记录。
     *
     * @param newConfig        包含新参数的对象（ID 将被忽略，允许字段为 null，null 表示沿用实体默认值）
     * @param updateReason     更新原因，将写入旧记录的 disableReason
     * @param operatorUsername 操作人用户名，将作为新记录的 createUsername
     * @return 新创建的参数配置
     */
    public GlobalCheckoutParamContext updateConfig(GlobalCheckoutParamContext newConfig,
                                                   String updateReason,
                                                   String operatorUsername) {
        lock.lock();
        try {
            // 1. 禁用当前有效记录
            Optional<GlobalCheckoutParamContext> currentOpt = repository.findFirstByDisableTimeMsIsNull();
            if (currentOpt.isPresent()) {
                GlobalCheckoutParamContext current = currentOpt.get();
                current.setDisableTimeMs(System.currentTimeMillis());
                current.setDisableReason(updateReason);
                repository.save(current); // 保存禁用状态
            }

            // 2. 创建新记录
            GlobalCheckoutParamContext newEntity = new GlobalCheckoutParamContext(operatorUsername);
            // 仅复制非空字段，避免覆盖实体默认值
            if (newConfig.getTaixueRatio() != null) {
                newEntity.setTaixueRatio(newConfig.getTaixueRatio());
            }
            if (newConfig.getCommercialRatio() != null) {
                newEntity.setCommercialRatio(newConfig.getCommercialRatio());
            }
            if (newConfig.getTemplateDefectParam() != null) {
                newEntity.setTemplateDefectParam(newConfig.getTemplateDefectParam());
            }
            if (newConfig.getPlacerRatio() != null) {
                newEntity.setPlacerRatio(newConfig.getPlacerRatio());
            }
            if (newConfig.getUploaderRatio() != null) {
                newEntity.setUploaderRatio(newConfig.getUploaderRatio());
            }
            if (newConfig.getReleaseDelayDays() != null) {
                newEntity.setReleaseDelayDays(newConfig.getReleaseDelayDays());
            }
            // 显式设置创建时间戳为当前时间（更精确）
            newEntity.setCreateTimeMs(System.currentTimeMillis());

            return repository.save(newEntity);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取所有历史配置记录，按创建时间倒序排列（最新的在前）
     */
    public List<GlobalCheckoutParamContext> getAllConfigs() {
        return repository.findAllByOrderByCreateTimeMsDesc();
    }
}
