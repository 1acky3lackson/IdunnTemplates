package com.jackyblackson.idunntemplates.core.set;

public class TemplateSetSource {
    private String path;
    private double weight;

    public TemplateSetSource() {}

    public TemplateSetSource(String path, double weight) {
        this.path = path;
        this.weight = weight;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
