package com.jackyblackson.idunntemplates.core.history;

import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.history.change.*;
import com.sk89q.worldedit.history.changeset.ChangeSet;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

public class ChangeSetFingerprintCalculator {

    // 采样深度：头尾各取多少个
    private static final int EDGE_SAMPLE_DEPTH = 15;

    /**
     * 计算 ChangeSet 的唯一指纹
     * 采用 "双端采样" 策略，避免单端数据单一（如空气墙）导致指纹雷同
     */
    public static String calculateFingerprint(ChangeSet changeSet) {
        if (changeSet == null) return "null";

        StringBuilder sb = new StringBuilder();

        // 1. 基础特征：总大小 (这是最强的区分特征)
        sb.append("S:").append(changeSet.longSize()).append("|");

        // 2. 头部采样 (Forward Iterator)
        // 捕获操作序列的"开始"部分
        Iterator<Change> forwardIter = changeSet.forwardIterator();
        int fwdCount = 0;
        sb.append("HEAD:[");
        while (forwardIter.hasNext() && fwdCount < EDGE_SAMPLE_DEPTH) {
            Change change = forwardIter.next();
            appendChangeSignature(sb, change);
            sb.append(";");
            fwdCount++;
        }
        sb.append("]|");

        // 3. 尾部采样 (Backward Iterator)
        // 捕获操作序列的"结束"部分
        // 注意：这是倒序的，即最后一个操作会最先被采样
        Iterator<Change> backwardIter = changeSet.backwardIterator();
        int bwdCount = 0;
        sb.append("TAIL:[");
        while (backwardIter.hasNext() && bwdCount < EDGE_SAMPLE_DEPTH) {
            Change change = backwardIter.next();
            appendChangeSignature(sb, change);
            sb.append(";");
            bwdCount++;
        }
        sb.append("]");

        // 4. 生成哈希 (MD5)
        return hashMD5(sb.toString());
    }

    private static void appendChangeSignature(StringBuilder sb, Change change) {
        // --- BlockChange (Record in newer WE/FAWE) ---
        if (change instanceof BlockChange bc) {
            // 格式: BL@x,y,z[OldID->NewID]
            sb.append("BL@");
            // 注意：新版 FAWE/WE 使用 record 风格的方法名 (position() 而不是 getPosition())
            // 如果你使用的是旧版 API，请改回 getPosition()
            appendVec3(sb, bc.position());
            sb.append("[");
            appendBlock(sb, bc.previous());
            sb.append("->");
            appendBlock(sb, bc.current());
            sb.append("]");
        }
        // --- BiomeChange3D ---
        else if (change instanceof BiomeChange3D bc) {
            sb.append("B3@");
            appendVec3(sb, bc.position());
            sb.append("[");
            appendBiome(sb, bc.previous());
            sb.append("->");
            appendBiome(sb, bc.current());
            sb.append("]");
        }
        // --- BiomeChange (Legacy) ---
        else if (change instanceof BiomeChange bc) {
            sb.append("B2@");
            BlockVector2 pos = bc.getPosition();
            sb.append(pos.x()).append(",").append(pos.z());
            sb.append("[");
            appendBiome(sb, bc.getPrevious());
            sb.append("->");
            appendBiome(sb, bc.getCurrent());
            sb.append("]");
        }
        // --- EntityCreate ---
        else if (change instanceof EntityCreate ec) {
            sb.append("EC@");
            // Location 可能在序列化中微调，取整比较安全
            appendLoc(sb, ec.getEntity() != null ? ec.getEntity().getLocation() : null);
            sb.append("[");
            appendEntityState(sb, ec.state);
            sb.append("]");
        }
        // --- EntityRemove ---
        else if (change instanceof EntityRemove er) {
            sb.append("ER@");
            appendEntityState(sb, er.state);
        }
        // --- 未知类型 ---
        else {
            sb.append(change.getClass().getSimpleName());
        }
    }

    // --- 辅助拼接方法 (保持紧凑) ---

    private static void appendVec3(StringBuilder sb, BlockVector3 v) {
        if (v == null) return;
        sb.append(v.x()).append(",").append(v.y()).append(",").append(v.z());
    }

    private static void appendLoc(StringBuilder sb, Location l) {
        if (l == null) return;
        // 取整以避免浮点数精度造成的哈希不一致
        sb.append((int) l.toVector().x()).append(",")
                .append((int) l.toVector().y()).append(",")
                .append((int) l.toVector().z());
    }

    private static void appendBlock(StringBuilder sb, BaseBlock b) {
        if (b == null) {
            sb.append("null");
        } else {
            // BlockType ID (例如 "minecraft:stone") 是最稳定的特征
            sb.append(b.getBlockType().id());
            // 如果是 AIR，我们不需要 NBT，否则加上 NBT 的哈希增强区分度
            if (!b.getBlockType().getMaterial().isAir() && b.getNbt() != null) {
                sb.append("#").append(b.getNbt().toString());
            }
        }
    }

    private static void appendBiome(StringBuilder sb, BiomeType b) {
        if (b == null) sb.append("null");
        else sb.append(b.id());
    }

    private static void appendEntityState(StringBuilder sb, BaseEntity state) {
        if (state == null) {
            sb.append("null");
        } else {
            if (state.getType() != null) sb.append(state.getType().id());
            if (state.getNbt() != null) sb.append("#").append(state.getNbt().toString());
        }
    }

    private static String hashMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashInBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashInBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(input.hashCode());
        }
    }
}
