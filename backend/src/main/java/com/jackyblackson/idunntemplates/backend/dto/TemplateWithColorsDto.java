package com.jackyblackson.idunntemplates.backend.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;

import java.util.List;

public class TemplateWithColorsDto {

    @JsonUnwrapped
    private Template template;

    private List<String> colorSchemes;

    private String latestVersionName;
    private List<TemplateVersion> latestVersions;
    private int versionCount;
    private boolean canUse;
    private boolean canCommit;

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

    public String getLatestVersionName() {
        return latestVersionName;
    }

    public void setLatestVersionName(String latestVersionName) {
        this.latestVersionName = latestVersionName;
    }

    public List<TemplateVersion> getLatestVersions() {
        return latestVersions;
    }

    public void setLatestVersions(List<TemplateVersion> latestVersions) {
        this.latestVersions = latestVersions;
    }

    public int getVersionCount() {
        return versionCount;
    }

    public void setVersionCount(int versionCount) {
        this.versionCount = versionCount;
    }

    public boolean isCanUse() {
        return canUse;
    }

    public void setCanUse(boolean canUse) {
        this.canUse = canUse;
    }

    public boolean isCanCommit() {
        return canCommit;
    }

    public void setCanCommit(boolean canCommit) {
        this.canCommit = canCommit;
    }
}
