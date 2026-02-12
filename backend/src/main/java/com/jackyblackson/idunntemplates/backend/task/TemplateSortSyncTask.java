package com.jackyblackson.idunntemplates.backend.task;

import com.jackyblackson.idunntemplates.backend.store.repository.TemplateRepository;
import com.jackyblackson.idunntemplates.backend.store.repository.TemplateVersionRepository;
import com.jackyblackson.idunntemplates.core.domain.Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class TemplateSortSyncTask {

    private final TemplateRepository templateRepository;
    private final TemplateVersionRepository versionRepository;

    /**
     * 场景 A: 应用启动时立即执行一次迁移
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("应用已就绪，开始执行初始数据补全...");
        syncMissingLastVersionAt();
    }

    /**
     * 场景 B: 每分钟扫描一次 (60000ms)，处理外部数据源新入库的数据
     */
    @Scheduled(fixedRate = 60000 * 5)
    public void scheduledSync() {
        log.debug("开始执行定时同步：扫描 lastVersionAt 为空的模板...");
        syncMissingLastVersionAt();
    }

    /**
     * 核心同步逻辑 (纯 ORM 实现)
     */
    @Transactional
    public void syncMissingLastVersionAt() {
        // 1. 查找所有缺失排序字段的记录
        List<Template> pendingTemplates = templateRepository.findAll();

        if (pendingTemplates.isEmpty()) {
            return;
        }

        log.info("检测到 {} 条模板数据，正在同步...", pendingTemplates.size());

        for (Template template : pendingTemplates) {
            try {
                // 2. ORM 查询最新版本时间
                versionRepository.findTopByTemplateOrderByCreatedAtDesc(template)
                        .ifPresentOrElse(
                                latest -> template.setLastVersionAt(latest.getCreatedAt()),
                                () -> {
                                    // 兜底策略：若无版本，则使用模板本身的创建时间
                                    // 确保 lastVersionAt 永远不为 null，否则排序会失效
                                    template.setLastVersionAt(
                                            template.getMetadata().getCreationTime()
                                    );
                                }
                        );
            } catch (Exception e) {
                log.error("同步模板 [{}] 排序字段失败: {}", template.getId(), e.getMessage());
            }
        }

        // 3. 批量写回数据库（更新本地冗余字段）
        templateRepository.saveAll(pendingTemplates);
        log.info("同步完成，已成功更新 {} 条记录。", pendingTemplates.size());
    }
}
