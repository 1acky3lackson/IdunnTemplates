package com.jackyblackson.idunntemplates.backend.commercial.service;

import com.jackyblackson.idunntemplates.backend.commercial.dto.checkout.TemplateUsage;
import com.jackyblackson.idunntemplates.backend.commercial.entity.Project;
import com.jackyblackson.idunntemplates.core.domain.Template;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ProjectTemplateRelationService {
    public Map<Template, TemplateUsage> getTemplateUsageForProject(Project project) {
        return Map.of();
    }
}
