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
        return resolveTemplates(manager, null, false);
    }

    /**
     * Resolves templates with support for recursive sets.
     * @param manager TemplateManager
     * @param setResolver Function to resolve a set name to a TemplateSet. Input is "namespace:name".
     * @param isGlobalContext If true, this set is being resolved in a global context, so it cannot reference private sets.
     */
    public Map<Template, Double> resolveTemplates(TemplateManager manager, java.util.function.Function<String, TemplateSet> setResolver, boolean isGlobalContext) {
        return resolveTemplatesRecursive(manager, setResolver, isGlobalContext, new HashSet<>());
    }

    private Map<Template, Double> resolveTemplatesRecursive(TemplateManager manager, 
                                                            java.util.function.Function<String, TemplateSet> setResolver, 
                                                            boolean isGlobalContext,
                                                            Set<String> visitedSets) {
        Map<Template, Double> result = new HashMap<>();
        
        for (TemplateSetSource source : sources) {
            String path = source.getPath();
            
            // 1. Check if it is a Set Reference
            // Format: "set:ns:name" or "ns:name" (if we treat all non-slash as sets? No, conflict with templates)
            // Safer to require "set:" prefix OR "ns:name" where ns contains "." or is "global"?
            // User said: "Recursive use other sets...".
            // Let's assume if it starts with "set:" it is explicitly a set.
            // Or if it matches "namespace:name" pattern.
            boolean isSetRef = path.startsWith("set:") || path.contains(":");
            
            if (isSetRef) {
                if (setResolver == null) continue;
                
                String setName = path.startsWith("set:") ? path.substring(4) : path;
                
                // Security Check: Global cannot use Personal
                if (isGlobalContext) {
                    // Check if target is personal
                    // Assuming personal sets have "player." in namespace
                    if (setName.startsWith("player.") || (setName.contains(":") && setName.split(":")[0].startsWith("player."))) {
                        // Skip illegal reference
                        continue; 
                    }
                }
                
                // Cycle Detection
                if (visitedSets.contains(setName)) continue;
                Set<String> newVisited = new HashSet<>(visitedSets);
                newVisited.add(setName);
                
                TemplateSet childSet = setResolver.apply(setName);
                if (childSet != null) {
                    // Recurse
                    // Child context: if we are global, child must be treated as global context (or just we are in global chain)
                    // If we are private, we can reference global or private.
                    Map<Template, Double> childResult = childSet.resolveTemplatesRecursive(manager, setResolver, isGlobalContext, newVisited);
                    
                    // Merge child result with weight
                    for (Map.Entry<Template, Double> entry : childResult.entrySet()) {
                        merge(result, entry.getKey(), entry.getValue() * source.getWeight());
                    }
                }
                continue;
            }
            
            // 2. Normal Template/Directory resolution
            // Clean path
            if (path.startsWith("/")) path = path.substring(1);
            
            // Check if exact match
            Template t = manager.getTemplate(path);
            if (t != null) {
                merge(result, t, source.getWeight());
                continue;
            }
            
            // Treat as directory prefix
            String prefix = path.endsWith("/") ? path : path + "/";
            
            for (Template cand : manager.getTemplates()) {
                String cPath = cand.getPath();
                if (cPath.startsWith("_")) cPath = cPath.substring(1); 
                
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
        return pickRandom(manager, null, false);
    }

    public Template pickRandom(TemplateManager manager, java.util.function.Function<String, TemplateSet> setResolver, boolean isGlobalContext) {
        Map<Template, Double> map = resolveTemplates(manager, setResolver, isGlobalContext);
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
