package com.jackyblackson.idunntemplates.core.util;

import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Location;

public class TransformUtil {

    public static Clipboard transformClipboard(Clipboard original, int rotation, boolean flipX, boolean flipY, boolean flipZ) {
        AffineTransform transform = new AffineTransform();
        transform = transform.rotateY(rotation);
        if (flipX) transform = transform.scale(BlockVector3.at(-1, 1, 1).toVector3());
        if (flipY) transform = transform.scale(BlockVector3.at(1, -1, 1).toVector3());
        if (flipZ) transform = transform.scale(BlockVector3.at(1, 1, -1).toVector3());

        return createTransformedClipboard(original, transform);
    }
    public static Clipboard createTransformedClipboard(Clipboard original, AffineTransform transform) {
        Region region = original.getRegion();
        BlockVector3 origin = original.getOrigin();
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        BlockVector3[] corners = new BlockVector3[8];
        corners[0] = min;
        corners[1] = BlockVector3.at(min.x(), min.y(), max.z());
        corners[2] = BlockVector3.at(min.x(), max.y(), min.z());
        corners[3] = BlockVector3.at(min.x(), max.y(), max.z());
        corners[4] = BlockVector3.at(max.x(), min.y(), min.z());
        corners[5] = BlockVector3.at(max.x(), min.y(), max.z());
        corners[6] = BlockVector3.at(max.x(), max.y(), min.z());
        corners[7] = max;

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        for (BlockVector3 c : corners) {
            Vector3 v = c.toVector3().subtract(origin.toVector3());
            Vector3 t = transform.apply(v);
            BlockVector3 b = t.toBlockPoint();

            if (b.x() < minX) minX = b.x();
            if (b.y() < minY) minY = b.y();
            if (b.z() < minZ) minZ = b.z();
            if (b.x() > maxX) maxX = b.x();
            if (b.y() > maxY) maxY = b.y();
            if (b.z() > maxZ) maxZ = b.z();
        }

        BlockVector3 newMin = BlockVector3.at(minX, minY, minZ).add(origin);
        BlockVector3 newMax = BlockVector3.at(maxX, maxY, maxZ).add(origin);

        CuboidRegion newRegion = new CuboidRegion(null, newMin, newMax);
        BlockArrayClipboard target = new BlockArrayClipboard(newRegion);
        target.setOrigin(origin);

        ClipboardHolder holder = new ClipboardHolder(original);
        holder.setTransform(holder.getTransform().combine(transform));

        try {
            Operation op = holder.createPaste(target)
                    .to(origin)
                    .ignoreAirBlocks(false)
                    .build();
            Operations.completeLegacy(op);
        } catch (Exception e) { e.printStackTrace(); }

        return target;
    }

    public static BlockVector3 getInstanceMinPos(Location location, Clipboard clipboard) {
        var offset = clipboard.getRegion().getMinimumPoint();
        var origin = clipboard.getOrigin();
        return BlockVector3.at(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        ).subtract(
                BlockVector3.at(
                        origin.x(),
                        origin.y(),
                        origin.z()
                ).subtract(offset)
        );
    }
}
