package com.jackyblackson.idunntemplates.core.effect;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.Objects;

public class ParticleUtil {

    /**
     * Draws a line between two points.
     */
    public static void drawLine(Location start, Location end, Particle particle, double density, double offsetX, double offsetY, double offsetZ, int count, Object data) {
        World world = start.getWorld();
        if (world == null || !world.equals(end.getWorld())) return;

        double distance = start.distance(end);
        Vector p1 = start.toVector();
        Vector p2 = end.toVector();
        Vector vector = p2.clone().subtract(p1).normalize().multiply(density);
        
        double covered = 0;
        for (; covered < distance; p1.add(vector)) {
            world.spawnParticle(particle, p1.getX(), p1.getY(), p1.getZ(), count, offsetX, offsetY, offsetZ, 0, data);
            covered += density;
        }
    }

    public static void drawLine(Location start, Location end, Particle particle, double density, double offsetX, double offsetY, double offsetZ, int count) {
        drawLine(start, end, particle, density, offsetX, offsetY, offsetZ, count, null);
    }
    
    /**
     * Draws a box defined by 8 corners.
     * Corners order is expected to be consistent (e.g., bottom-rect then top-rect).
     * Usually: 0-3 bottom (counter-clockwise), 4-7 top (counter-clockwise matching bottom).
     */
    public static void drawBox(Location[] corners, Particle particle, Object data) {
        if (corners.length != 8) return;
        
        // Bottom loop
        drawLine(corners[0], corners[1], particle, data);
        drawLine(corners[1], corners[2], particle, data);
        drawLine(corners[2], corners[3], particle, data);
        drawLine(corners[3], corners[0], particle, data);
        
        // Top loop
        drawLine(corners[4], corners[5], particle, data);
        drawLine(corners[5], corners[6], particle, data);
        drawLine(corners[6], corners[7], particle, data);
        drawLine(corners[7], corners[4], particle, data);
        
        // Verticals
        drawLine(corners[0], corners[4], particle, data);
        drawLine(corners[1], corners[5], particle, data);
        drawLine(corners[2], corners[6], particle, data);
        drawLine(corners[3], corners[7], particle, data);
    }

    public static void drawBox(Location[] corners, Particle particle) {
        drawBox(corners, particle, null);
    }
    
    public static void drawSurfaceGridAABB(Location min, Location max, double spacing, Particle particle, Object data) {
        if (spacing <= 0) return;
        World world = min.getWorld();
        
        double minX = Math.min(min.getX(), max.getX());
        double minY = Math.min(min.getY(), max.getY());
        double minZ = Math.min(min.getZ(), max.getZ());
        double maxX = Math.max(min.getX(), max.getX());
        double maxY = Math.max(min.getY(), max.getY());
        double maxZ = Math.max(min.getZ(), max.getZ());
        
        // Draw standard edges first
        Location[] corners = new Location[8];
        corners[0] = new Location(world, minX, minY, minZ);
        corners[1] = new Location(world, maxX, minY, minZ);
        corners[2] = new Location(world, maxX, minY, maxZ);
        corners[3] = new Location(world, minX, minY, maxZ);
        corners[4] = new Location(world, minX, maxY, minZ);
        corners[5] = new Location(world, maxX, maxY, minZ);
        corners[6] = new Location(world, maxX, maxY, maxZ);
        corners[7] = new Location(world, minX, maxY, maxZ);
        drawBox(corners, particle, data);
        
        // Grid lines
        // XY Planes (Front/Back) - vary Z
        // Front (minZ) and Back (maxZ)
        // Vertical lines on XY face: vary X, draw Y line
        for (double x = minX + spacing; x < maxX; x += spacing) {
            drawLine(new Location(world, x, minY, minZ), new Location(world, x, maxY, minZ), particle, data);
            drawLine(new Location(world, x, minY, maxZ), new Location(world, x, maxY, maxZ), particle, data);
        }
        // Horizontal lines on XY face: vary Y, draw X line
        for (double y = minY + spacing; y < maxY; y += spacing) {
            drawLine(new Location(world, minX, y, minZ), new Location(world, maxX, y, minZ), particle, data);
            drawLine(new Location(world, minX, y, maxZ), new Location(world, maxX, y, maxZ), particle, data);
        }
        
        // ZY Planes (Left/Right) - vary X
        // Left (minX) and Right (maxX)
        // Vertical lines on ZY face: vary Z, draw Y line
        for (double z = minZ + spacing; z < maxZ; z += spacing) {
            drawLine(new Location(world, minX, minY, z), new Location(world, minX, maxY, z), particle, data);
            drawLine(new Location(world, maxX, minY, z), new Location(world, maxX, maxY, z), particle, data);
        }
        // Horizontal lines on ZY face: vary Y, draw Z line
        for (double y = minY + spacing; y < maxY; y += spacing) {
            drawLine(new Location(world, minX, y, minZ), new Location(world, minX, y, maxZ), particle, data);
            drawLine(new Location(world, maxX, y, minZ), new Location(world, maxX, y, maxZ), particle, data);
        }
        
        // XZ Planes (Top/Bottom) - vary Y
        // Bottom (minY) and Top (maxY)
        // Lines along Z: vary X
        for (double x = minX + spacing; x < maxX; x += spacing) {
            drawLine(new Location(world, x, minY, minZ), new Location(world, x, minY, maxZ), particle, data);
            drawLine(new Location(world, x, maxY, minZ), new Location(world, x, maxY, maxZ), particle, data);
        }
        // Lines along X: vary Z
        for (double z = minZ + spacing; z < maxZ; z += spacing) {
            drawLine(new Location(world, minX, minY, z), new Location(world, maxX, minY, z), particle, data);
            drawLine(new Location(world, minX, maxY, z), new Location(world, maxX, maxY, z), particle, data);
        }
    }
    
    public static void drawSurfaceGridAABB(Location min, Location max, double spacing, Particle particle) {
        drawSurfaceGridAABB(min, max, spacing, particle, null);
    }

    private static void drawLine(Location start, Location end, Particle particle, Object data) {
        drawLine(start, end, particle, 0.5, 0, 0, 0, 1, data);
    }

    private static void drawLine(Location start, Location end, Particle particle) {
        drawLine(start, end, particle, 0.5, 0, 0, 0, 1, null);
    }
    
    public static void spawnMagicParticles(Location center) {
        Objects.requireNonNull(center.getWorld()).spawnParticle(Particle.WITCH, center, 3, 0.5, 0.5, 0.5, 0.05);
    }
}
