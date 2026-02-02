package com.jackyblackson.idunntemplates.manager;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.permission.PermissionNames;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ResizeManager {
    private final IdunnTemplates plugin;
    private final Map<UUID, Long> lastUsageTime = new ConcurrentHashMap<>();

    public ResizeManager(IdunnTemplates plugin) {
        this.plugin = plugin;
    }

    public boolean hasCooldown(Player player) {
        if (player.hasPermission(PermissionNames.Resizes.bypassCooldown)) {
            return false;
        } else {
            long currentTime = System.currentTimeMillis();
            long lastUsed = (Long)this.lastUsageTime.getOrDefault(player.getUniqueId(), 0L);
            long timeSinceLastUse = (currentTime - lastUsed) / 1000L;
            return timeSinceLastUse < (long)this.plugin.getResizeConfigManager().getCooldownSeconds();
        }
    }

    public long getRemainingCooldown(Player player) {
        long currentTime = System.currentTimeMillis();
        long lastUsed = (Long)this.lastUsageTime.getOrDefault(player.getUniqueId(), 0L);
        long timeSinceLastUse = (currentTime - lastUsed) / 1000L;
        return Math.max(0L, (long)this.plugin.getResizeConfigManager().getCooldownSeconds() - timeSinceLastUse);
    }

    public void updateLastUsage(Player player) {
        this.lastUsageTime.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void smoothlyResizePlayer(final Player player, final double targetScale) {
        final double initialScale = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_SCALE)).getBaseValue();
        final double step = (targetScale - initialScale) / (double)this.plugin.getResizeConfigManager().getResizeSteps();
        (new BukkitRunnable() {
            int count = 0;

            public void run() {
                if (this.count++ >= ResizeManager.this.plugin.getResizeConfigManager().getResizeSteps()) {
                    Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_SCALE)).setBaseValue(targetScale);
                    this.cancel();
                } else {
                    Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_SCALE)).setBaseValue(initialScale + step * (double)this.count);
                }

            }
        }).runTaskTimer(this.plugin, 0L, 1L);
    }

    public boolean isValidScale(double scale, boolean hasExtendedPermission) {
        if (hasExtendedPermission) {
            return scale >= this.plugin.getResizeConfigManager().getExtendedMinScale() && scale <= this.plugin.getResizeConfigManager().getExtendedMaxScale();
        } else {
            return scale >= this.plugin.getResizeConfigManager().getDefaultMinScale() && scale <= this.plugin.getResizeConfigManager().getDefaultMaxScale();
        }
    }

    public double getMinScale(boolean hasExtendedPermission) {
        return hasExtendedPermission ? this.plugin.getResizeConfigManager().getExtendedMinScale() : this.plugin.getResizeConfigManager().getDefaultMinScale();
    }

    public double getMaxScale(boolean hasExtendedPermission) {
        return hasExtendedPermission ? this.plugin.getResizeConfigManager().getExtendedMaxScale() : this.plugin.getResizeConfigManager().getDefaultMaxScale();
    }
}
