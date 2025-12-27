package com.jackyblackson.idunntemplates.core.effect;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class ParticleUtil {

    /**
     * Draws a line between two points.
     */
    public static void drawLine(Location start, Location end, Particle particle, double density, double offsetX, double offsetY, double offsetZ, int count) {
        World world = start.getWorld();
        if (world == null || !world.equals(end.getWorld())) return;

        double distance = start.distance(end);
        Vector p1 = start.toVector();
        Vector p2 = end.toVector();
        Vector vector = p2.clone().subtract(p1).normalize().multiply(density);
        
        double covered = 0;
        for (; covered < distance; p1.add(vector)) {
            world.spawnParticle(particle, p1.getX(), p1.getY(), p1.getZ(), count, offsetX, offsetY, offsetZ, 0);
            covered += density;
        }
    }
    
    /**
     * Draws a box defined by 8 corners.
     * Corners order is expected to be consistent (e.g., bottom-rect then top-rect).
     * Usually: 0-3 bottom (counter-clockwise), 4-7 top (counter-clockwise matching bottom).
     */
    public static void drawBox(Location[] corners, Particle particle) {
        if (corners.length != 8) return;
        
        // Bottom loop
        drawLine(corners[0], corners[1], particle);
        drawLine(corners[1], corners[2], particle);
        drawLine(corners[2], corners[3], particle);
        drawLine(corners[3], corners[0], particle);
        
        // Top loop
        drawLine(corners[4], corners[5], particle);
        drawLine(corners[5], corners[6], particle);
        drawLine(corners[6], corners[7], particle);
        drawLine(corners[7], corners[4], particle);
        
        // Verticals
        drawLine(corners[0], corners[4], particle);
        drawLine(corners[1], corners[5], particle);
        drawLine(corners[2], corners[6], particle);
        drawLine(corners[3], corners[7], particle);
    }
    
    public static void drawSurfaceGridAABB(Location min, Location max, double spacing, Particle particle) {
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
        drawBox(corners, particle);
        
        // Grid lines
        // XY Planes (Front/Back) - vary Z
        // Front (minZ) and Back (maxZ)
        // Vertical lines on XY face: vary X, draw Y line
        for (double x = minX + spacing; x < maxX; x += spacing) {
            drawLine(new Location(world, x, minY, minZ), new Location(world, x, maxY, minZ), particle);
            drawLine(new Location(world, x, minY, maxZ), new Location(world, x, maxY, maxZ), particle);
        }
        // Horizontal lines on XY face: vary Y, draw X line
        for (double y = minY + spacing; y < maxY; y += spacing) {
            drawLine(new Location(world, minX, y, minZ), new Location(world, maxX, y, minZ), particle);
            drawLine(new Location(world, minX, y, maxZ), new Location(world, maxX, y, maxZ), particle);
        }
        
        // ZY Planes (Left/Right) - vary X
        // Left (minX) and Right (maxX)
        // Vertical lines on ZY face: vary Z, draw Y line
        for (double z = minZ + spacing; z < maxZ; z += spacing) {
            drawLine(new Location(world, minX, minY, z), new Location(world, minX, maxY, z), particle);
            drawLine(new Location(world, maxX, minY, z), new Location(world, maxX, maxY, z), particle);
        }
        // Horizontal lines on ZY face: vary Y, draw Z line
        for (double y = minY + spacing; y < maxY; y += spacing) {
            drawLine(new Location(world, minX, y, minZ), new Location(world, minX, y, maxZ), particle);
            drawLine(new Location(world, maxX, y, minZ), new Location(world, maxX, y, maxZ), particle);
        }
        
        // XZ Planes (Top/Bottom) - vary Y
        // Bottom (minY) and Top (maxY)
        // Lines along Z: vary X
        for (double x = minX + spacing; x < maxX; x += spacing) {
            drawLine(new Location(world, x, minY, minZ), new Location(world, x, minY, maxZ), particle);
            drawLine(new Location(world, x, maxY, minZ), new Location(world, x, maxY, maxZ), particle);
        }
        // Lines along X: vary Z
        for (double z = minZ + spacing; z < maxZ; z += spacing) {
            drawLine(new Location(world, minX, minY, z), new Location(world, maxX, minY, z), particle);
            drawLine(new Location(world, minX, maxY, z), new Location(world, maxX, maxY, z), particle);
        }
    }

    private static void drawLine(Location start, Location end, Particle particle) {
        drawLine(start, end, particle, 0.5, 0, 0, 0, 1);
    }
    
    public static void spawnMagicParticles(Location center) {
        center.getWorld().spawnParticle(Particle.WITCH, center, 3, 0.5, 0.5, 0.5, 0.05);
    }
}
