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

    private static void drawLine(Location start, Location end, Particle particle) {
        drawLine(start, end, particle, 0.5, 0, 0, 0, 1);
    }
    
    public static void spawnMagicParticles(Location center) {
        center.getWorld().spawnParticle(Particle.WITCH, center, 3, 0.5, 0.5, 0.5, 0.05);
    }
}
