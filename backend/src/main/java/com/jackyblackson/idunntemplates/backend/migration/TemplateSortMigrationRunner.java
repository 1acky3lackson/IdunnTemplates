package com.jackyblackson.idunntemplates.backend.migration;

import com.jackyblackson.idunntemplates.backend.store.repository.TemplateRepository;
import com.jackyblackson.idunntemplates.backend.store.repository.TemplateVersionRepository;
import com.jackyblackson.idunntemplates.core.domain.Template;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TemplateSortMigrationRunner {

    private final TemplateRepository templateRepository;
    private final TemplateVersionRepository versionRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void migrateMissingLastVersionAt() {
        log.info("Starting Template lastVersionAt migration (ORM mode)...");

        // 1. 找出所有缺失字段的模板
        List<Template> pending = templateRepository.findByLastVersionAtIsNull();

        if (pending.isEmpty()) {
            log.info("No templates require migration.");
            return;
        }

        // 2. 遍历补全（ORM 方式）
        for (Template template : pending) {
            // 查询该模板关联的最新的一个版本
            versionRepository.findTopByTemplateOrderByCreatedAtDesc(template)
                    .ifPresentOrElse(
                            latest -> template.setLastVersionAt(latest.getCreatedAt()),
                            () -> template.setLastVersionAt(template.getMetadata().getCreationTime()) // 若无版本，用模板创建时间兜底
                    );
        }

        // 3. 批量保存
        templateRepository.saveAll(pending);
        log.info("Migration completed. Updated {} templates.", pending.size());
    }
}
