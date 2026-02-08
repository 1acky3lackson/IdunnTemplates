package com.jackyblackson.idunntemplates.backend.migration;

import com.jackyblackson.idunntemplates.backend.store.repository.TemplateVersionRepository;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class TemplateVersionMigrationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TemplateVersionMigrationRunner.class);
    private final TemplateVersionRepository repository;

    public TemplateVersionMigrationRunner(TemplateVersionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking for TemplateVersion records with missing createdAt timestamps...");

        // 1. Find all records where createdAt is 0
        List<TemplateVersion> legacyVersions = repository.findByCreatedAt(0L);

        if (legacyVersions.isEmpty()) {
            log.info("No records found with createdAt = 0. Migration skipped.");
            return;
        }

        log.info("Found {} records to update.", legacyVersions.size());

        for (TemplateVersion version : legacyVersions) {
            try {
                // 2. Parse the versionId (String timestamp) into a long
                long timestamp = Long.parseLong(version.getVersionId());

                // 3. Update the field
                version.setCreatedAt(timestamp);

                // Note: JPA's dirty checking will handle the update within the @Transactional block,
                // but an explicit save is fine too.
                repository.save(version);
            } catch (NumberFormatException e) {
                log.error("Failed to parse versionId '{}' for record ID {}: {}",
                        version.getVersionId(), version.getId(), e.getMessage());
            }
        }

        log.info("Successfully updated {} records.", legacyVersions.size());
    }
}
