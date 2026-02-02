package com.jackyblackson.idunntemplates.backend.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.jackyblackson.idunntemplates.core.domain.Template;

import java.util.List;

public class TemplateWithColorsDto {

    @JsonUnwrapped
    private Template template;

    private List<String> colorSchemes;

    public TemplateWithColorsDto(Template template, List<String> colorSchemes) {
        this.template = template;
        this.colorSchemes = colorSchemes;
    }

    public Template getTemplate() {
        return template;
    }

    public List<String> getColorSchemes() {
        return colorSchemes;
    }
}
