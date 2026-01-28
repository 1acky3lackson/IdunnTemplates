package com.jackyblackson.idunntemplates.manager;

import com.jackyblackson.idunntemplates.core.domain.Template;
import java.util.List;

public class ParentTemplateConflictException extends Exception {
    private final List<Template> intersectingTemplates;

    public ParentTemplateConflictException(List<Template> intersectingTemplates) {
        super("Detected recursive placement inside " + intersectingTemplates.size() + " templates.");
        this.intersectingTemplates = intersectingTemplates;
    }

    public List<Template> getIntersectingTemplates() {
        return intersectingTemplates;
    }
}
