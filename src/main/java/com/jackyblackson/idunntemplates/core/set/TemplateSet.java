package com.jackyblackson.idunntemplates.core.set;

import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.manager.TemplateManager;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class TemplateSet {
    private List<TemplateSetSource> sources = new ArrayList<>();
    
    // Properties: "0", "90", "180", "270", "random"
    private String rotate = "0"; 
    // "true", "false", "random"
    private String flipX = "false";
    private String flipZ = "false";

    public TemplateSet() {}

    public List<TemplateSetSource> getSources() {
        return sources;
    }

    public void setSources(List<TemplateSetSource> sources) {
        this.sources = sources;
    }

    public String getRotate() {
        return rotate;
    }

    public void setRotate(String rotate) {
        this.rotate = rotate;
    }

    public String getFlipX() {
        return flipX;
    }

    public void setFlipX(String flipX) {
        this.flipX = flipX;
    }

    public String getFlipZ() {
        return flipZ;
    }

    public void setFlipZ(String flipY) {
        this.flipZ = flipY;
    }
    
    public void addSource(String path, double weight) {
        sources.add(new TemplateSetSource(path, weight));
    }
    
    public boolean removeSource(String path) {
        return sources.removeIf(s -> s.getPath().equalsIgnoreCase(path));
    }
    
    public void clear() {
        sources.clear();
    }

    /**
     * Resolves all templates from sources with their effective weights.
     */
    public Map<Template, Double> resolveTemplates(TemplateManager manager) {
        Map<Template, Double> result = new HashMap<>();
        
        for (TemplateSetSource source : sources) {
            String path = source.getPath();
            // Clean path
            if (path.startsWith("/")) path = path.substring(1);
            
            // Check if exact match
            Template t = manager.getTemplate(path);
            if (t != null) {
                merge(result, t, source.getWeight());
                continue;
            }
            
            // Treat as directory prefix
            // Normalize path for prefix check: "users/jacky" -> "users/jacky/"
            String prefix = path.endsWith("/") ? path : path + "/";
            
            for (Template cand : manager.getTemplates()) {
                String cPath = cand.getPath();
                if (cPath.startsWith("_")) cPath = cPath.substring(1); // Handle internal storage paths if exposed
                
                // My TemplateManager normalize logic might strip leading _.
                // Let's assume manager.getTemplates() returns loaded templates.
                // We compare paths.
                // Template path: users/jacky/mytmpl
                
                // Check if starts with prefix (directory)
                // Need to handle both raw path and display path logic
                
                // Let's rely on string containment for now.
                // A better way might be manager.getTemplatesUnder(path)
                
                // Assuming path stored in Template is the full relative path
                if (cand.getPath().startsWith(prefix) || cand.getPath().equals(path)) {
                    merge(result, cand, source.getWeight());
                }
            }
        }
        return result;
    }
    
    private void merge(Map<Template, Double> map, Template t, double w) {
        if (map.containsKey(t)) {
            map.put(t, Math.max(map.get(t), w));
        } else {
            map.put(t, w);
        }
    }
    
    public Template pickRandom(TemplateManager manager) {
        Map<Template, Double> map = resolveTemplates(manager);
        if (map.isEmpty()) return null;
        
        double total = 0;
        for (double w : map.values()) total += w;
        
        double r = ThreadLocalRandom.current().nextDouble() * total;
        double current = 0;
        for (Map.Entry<Template, Double> entry : map.entrySet()) {
            current += entry.getValue();
            if (r <= current) return entry.getKey();
        }
        // Fallback
        return map.keySet().iterator().next();
    }
    
    public int resolveRotation() {
        if ("random".equalsIgnoreCase(rotate)) {
            int[] rots = {0, 90, 180, 270};
            return rots[ThreadLocalRandom.current().nextInt(rots.length)];
        }
        try {
            return Integer.parseInt(rotate);
        } catch (Exception e) {
            return 0;
        }
    }
    
    public boolean resolveFlipX() {
        if ("random".equalsIgnoreCase(flipX)) return ThreadLocalRandom.current().nextBoolean();
        return Boolean.parseBoolean(flipX);
    }
    
    public boolean resolveFlipZ() {
        if ("random".equalsIgnoreCase(flipZ)) return ThreadLocalRandom.current().nextBoolean();
        return Boolean.parseBoolean(flipZ);
    }
}
