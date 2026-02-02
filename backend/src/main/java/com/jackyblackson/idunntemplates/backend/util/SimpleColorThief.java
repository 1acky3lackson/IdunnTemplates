package com.jackyblackson.idunntemplates.backend.util;

import java.awt.image.BufferedImage;
import java.util.*;

public class SimpleColorThief {

    /**
     * Extracts dominant colors from the image.
     * Uses a simple quantization and frequency counting approach with distance filtering.
     */
    public static List<String> getPalette(BufferedImage image, int count) {
        if (image == null) return Collections.emptyList();

        Map<Integer, Integer> colorCounts = new HashMap<>();
        int width = image.getWidth();
        int height = image.getHeight();

        // Sample pixels
        int step = 1;
        if (width * height > 40000) {
            step = (int) Math.sqrt((width * height) / 10000.0);
        }
        if (step < 1) step = 1;

        for (int x = 0; x < width; x += step) {
            for (int y = 0; y < height; y += step) {
                int rgb = image.getRGB(x, y);
                // Ignore transparent pixels
                int alpha = (rgb >> 24) & 0xFF;
                if (alpha < 128) continue;

                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb) & 0xFF;

                // Quantization (reduce noise)
                r = (r / 8) * 8;
                g = (g / 8) * 8;
                b = (b / 8) * 8;

                int quantized = (r << 16) | (g << 8) | b;
                colorCounts.put(quantized, colorCounts.getOrDefault(quantized, 0) + 1);
            }
        }

        // Sort by frequency
        List<Map.Entry<Integer, Integer>> sorted = new ArrayList<>(colorCounts.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        List<String> palette = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : sorted) {
            if (palette.size() >= count) break;

            int rgb = entry.getKey();
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = (rgb) & 0xFF;

            boolean distinct = true;
            for (String existingHex : palette) {
                int existing = Integer.parseInt(existingHex, 16);
                int er = (existing >> 16) & 0xFF;
                int eg = (existing >> 8) & 0xFF;
                int eb = (existing) & 0xFF;

                // Euclidean distance
                double dist = Math.sqrt(Math.pow(r - er, 2) + Math.pow(g - eg, 2) + Math.pow(b - eb, 2));
                if (dist < 40) { // Threshold (tunable)
                    distinct = false;
                    break;
                }
            }

            if (distinct) {
                palette.add(String.format("%06x", rgb));
            }
        }

        return palette;
    }
}
