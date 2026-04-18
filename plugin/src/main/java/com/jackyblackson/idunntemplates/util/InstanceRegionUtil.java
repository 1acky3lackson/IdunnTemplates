package com.jackyblackson.idunntemplates.util;

import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import org.bukkit.Location;

public final class InstanceRegionUtil {

    private InstanceRegionUtil() {
    }

    public static com.sk89q.worldedit.regions.Region calculateWorldRegion(
            Clipboard clipboard,
            Location target,
            int rot,
            boolean flipX,
            boolean flipY,
            boolean flipZ,
            int mxn,
            int mxp,
            int myn,
            int myp,
            int mzn,
            int mzp
    ) {
        BlockVector3 min = clipboard.getRegion().getMinimumPoint();
        BlockVector3 max = clipboard.getRegion().getMaximumPoint();

        int minX = min.x() + mxn;
        int maxX = max.x() - mxp;
        int minY = min.y() + myn;
        int maxY = max.y() - myp;
        int minZ = min.z() + mzn;
        int maxZ = max.z() - mzp;

        if (minX > maxX || minY > maxY || minZ > maxZ) {
            return new com.sk89q.worldedit.regions.CuboidRegion(BlockVector3.at(0, 0, 0), BlockVector3.at(0, 0, 0));
        }

        AffineTransform transform = new AffineTransform();
        transform = transform.rotateY(rot);
        if (flipX) transform = transform.scale(BlockVector3.at(-1, 1, 1).toVector3());
        if (flipY) transform = transform.scale(BlockVector3.at(1, -1, 1).toVector3());
        if (flipZ) transform = transform.scale(BlockVector3.at(1, 1, -1).toVector3());

        BlockVector3 origin = clipboard.getOrigin();
        BlockVector3[] corners = new BlockVector3[8];
        corners[0] = BlockVector3.at(minX, minY, minZ);
        corners[1] = BlockVector3.at(minX, minY, maxZ);
        corners[2] = BlockVector3.at(minX, maxY, minZ);
        corners[3] = BlockVector3.at(minX, maxY, maxZ);
        corners[4] = BlockVector3.at(maxX, minY, minZ);
        corners[5] = BlockVector3.at(maxX, minY, maxZ);
        corners[6] = BlockVector3.at(maxX, maxY, minZ);
        corners[7] = BlockVector3.at(maxX, maxY, maxZ);

        int wMinX = Integer.MAX_VALUE, wMinY = Integer.MAX_VALUE, wMinZ = Integer.MAX_VALUE;
        int wMaxX = Integer.MIN_VALUE, wMaxY = Integer.MIN_VALUE, wMaxZ = Integer.MIN_VALUE;
        BlockVector3 targetVec = BlockVector3.at(target.getBlockX(), target.getBlockY(), target.getBlockZ());

        for (BlockVector3 corner : corners) {
            var relative = corner.toVector3().subtract(origin.toVector3());
            var transformed = transform.apply(relative);
            BlockVector3 worldPos = transformed.toBlockPoint().add(targetVec);

            if (worldPos.x() < wMinX) wMinX = worldPos.x();
            if (worldPos.y() < wMinY) wMinY = worldPos.y();
            if (worldPos.z() < wMinZ) wMinZ = worldPos.z();
            if (worldPos.x() > wMaxX) wMaxX = worldPos.x();
            if (worldPos.y() > wMaxY) wMaxY = worldPos.y();
            if (worldPos.z() > wMaxZ) wMaxZ = worldPos.z();
        }

        return new com.sk89q.worldedit.regions.CuboidRegion(
                BlockVector3.at(wMinX, wMinY, wMinZ),
                BlockVector3.at(wMaxX, wMaxY, wMaxZ)
        );
    }

    public static com.sk89q.worldedit.regions.Region calculateWorldRegion(
            Clipboard clipboard,
            Location target,
            Instance instance
    ) {
        return calculateWorldRegion(
                clipboard,
                target,
                instance.getRotationY(),
                instance.isFlipX(),
                instance.isFlipY(),
                instance.isFlipZ(),
                instance.getMaskXNeg(),
                instance.getMaskXPos(),
                instance.getMaskYNeg(),
                instance.getMaskYPos(),
                instance.getMaskZNeg(),
                instance.getMaskZPos()
        );
    }
}
