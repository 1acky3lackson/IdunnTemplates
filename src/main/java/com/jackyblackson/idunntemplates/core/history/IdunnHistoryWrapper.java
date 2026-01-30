package com.jackyblackson.idunntemplates.core.history;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.store.InstanceRepository;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage;

public class IdunnHistoryWrapper implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum HistoryType {
        INSTANCE_PLACE,
        STAGED_PLACE; // V2: New history type for staged recursive placement
    }

    private final HistoryType historyType;
    private final UUID playerUUID;

    // --- 新增：ChangeSet 指纹 ---
    private String changeSetFingerprint;
    private boolean valid = true;

    // 用于存储恢复所需的数据 (例如 Instance 的 JSON 或序列化对象)
    private final Map<String, Object> data = new HashMap<>();

    // 私有构造，通过静态工厂创建
    private IdunnHistoryWrapper(HistoryType historyType, UUID playerUUID) {
        this.historyType = historyType;
        this.playerUUID = playerUUID;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    // =================================
    // 指纹逻辑 (FINGERPRINT LOGIC)
    // =================================

    /**
     * 设置指纹 (在 Remember 时调用)
     */
    public void setFingerprint(String fingerprint) {
        this.changeSetFingerprint = fingerprint;
    }

    /**
     * 验证指纹是否匹配
     * @param currentFingerprint 当前 FAWE 历史栈中计算出的指纹
     * @return true 表示匹配，false 表示不匹配（数据已失效）
     */
    public boolean validateFingerprint(String currentFingerprint) {
        // 如果旧数据没有指纹，或者传入为空，视为失效，安全起见不执行自动逻辑
        if (this.changeSetFingerprint == null || currentFingerprint == null) {
            return false;
        }
        return this.changeSetFingerprint.equals(currentFingerprint);
    }

    // =================================
    // 业务逻辑 (BUSINESS LOGIC)
    // =================================

    /**
     * 执行撤回逻辑：
     * 1. 根据 ID 找到 Instance
     * 2. 执行硬删除 (Hard Delete)
     * * FAWE 会负责移除方块，这里只处理数据。
     */
    public void makeUndo() {
        if (historyType == HistoryType.INSTANCE_PLACE) {
            String instanceId = (String) data.get("instanceId");
            Instance instanceSnapshot = (Instance) data.get("instanceSnapshot");

            if (instanceId == null) return;

            InstanceRepository repo = getInstanceRepository();
            if (repo == null) return;

            // 尝试查找现有实例
            Instance target = repo.getAllLoadedInstances().stream()
                    .filter(i -> i.getId().equals(instanceId))
                    .findFirst()
                    .orElse(null);

            // 如果找到了，执行硬删除
            if (target != null) {
                repo.hardDelete(target);
            } else {
                repo.hardDelete(instanceSnapshot);
            }
            Player p = Bukkit.getPlayer(playerUUID);
            if (p != null) p.sendMessage(ChatColor.YELLOW + getMessage(p, "history.undo.success", instanceId.substring(0, 8), instanceSnapshot.getTemplate().getPath()));
        
        } else if (historyType == HistoryType.STAGED_PLACE) {
            // V2: Undo Staged Place
            String instanceId = (String) data.get("instanceId");
            Instance instanceSnapshot = (Instance) data.get("instanceSnapshot");
            UUID parentTemplateId = (UUID) data.get("parentTemplateId");
            
            if (instanceId == null || parentTemplateId == null) return;
            
            InstanceRepository repo = getInstanceRepository();
            com.jackyblackson.idunntemplates.manager.TemplateManager tm = getTemplateManager();
            if (repo == null || tm == null) return;
            
            // 1. Hard Delete Instance Record (Same as INSTANCE_PLACE)
            Instance target = repo.getAllLoadedInstances().stream()
                    .filter(i -> i.getId().equals(instanceId))
                    .findFirst()
                    .orElse(null);
            if (target != null) {
                repo.hardDelete(target);
            } else {
                repo.hardDelete(instanceSnapshot);
            }
            
            // 2. Remove from Parent's Staging Area
            com.jackyblackson.idunntemplates.core.domain.Template parent = tm.getTemplate(parentTemplateId);
            if (parent != null) {
                com.jackyblackson.idunntemplates.core.domain.StagedChanges staged = parent.getMetadata().getStagedChanges();
                // Find and remove the instance from addedInstances
                staged.getAddedInstances().removeIf(i -> i.getId().equals(instanceId));
                // Save metadata
                tm.saveTemplateMetadata(parent);
            }
            
            Player p = Bukkit.getPlayer(playerUUID);
            if (p != null) p.sendMessage(ChatColor.YELLOW + getMessage(
                    p, "history.undo.staged_success",
                    instanceId.substring(0, 8),
                    instanceSnapshot.getTemplate().getName(),
                    parent != null ? parent.getName() : "Unknown")
            );
        }
    }

    /**
     * 执行重做逻辑：
     * 1. 从数据中恢复 Instance 对象
     * 2. 重新保存到 Repository
     * * FAWE 会负责恢复方块。
     */
    public void makeRedo() {
        if (historyType == HistoryType.INSTANCE_PLACE) {
            // 从 Map 中获取之前保存的 Instance 对象
            Instance instanceSnapshot = (Instance) data.get("instanceSnapshot");

            if (instanceSnapshot == null) return;

            InstanceRepository repo = getInstanceRepository();
            if (repo == null) return;

            // 直接保存回仓库
            repo.saveInstance(instanceSnapshot);

            Player p = Bukkit.getPlayer(playerUUID);
            if (p != null) p.sendMessage(ChatColor.YELLOW + getMessage(p, "history.redo.success", instanceSnapshot.getId().substring(0, 8), instanceSnapshot.getTemplate().getPath()));
        
        } else if (historyType == HistoryType.STAGED_PLACE) {
            // V2: Redo Staged Place
            Instance instanceSnapshot = (Instance) data.get("instanceSnapshot");
            UUID parentTemplateId = (UUID) data.get("parentTemplateId");

            if (instanceSnapshot == null || parentTemplateId == null) return;

            InstanceRepository repo = getInstanceRepository();
            com.jackyblackson.idunntemplates.manager.TemplateManager tm = getTemplateManager();
            if (repo == null || tm == null) return;

            // 1. Restore Instance Record
            repo.saveInstance(instanceSnapshot);
            
            // 2. Add back to Parent's Staging Area
            com.jackyblackson.idunntemplates.core.domain.Template parent = tm.getTemplate(parentTemplateId);
            if (parent != null) {
                com.jackyblackson.idunntemplates.core.domain.StagedChanges staged = parent.getMetadata().getStagedChanges();
                staged.getAddedInstances().add(instanceSnapshot);
                tm.saveTemplateMetadata(parent);
            }

            Player p = Bukkit.getPlayer(playerUUID);
            if (p != null) p.sendMessage(ChatColor.YELLOW + getMessage(
                    p, "history.redo.staged_success",
                    instanceSnapshot.getId().substring(0, 8),
                    instanceSnapshot.getTemplate().getName(),
                    parent != null ? parent.getName() : "Unknown"
            ));
        }
    }

    // =================================
    // INSTANCE PLACE HISTORY
    // =================================

    public static IdunnHistoryWrapper placeInstanceHistory(Player p, Instance instance) {
        IdunnHistoryWrapper wrapper = new IdunnHistoryWrapper(HistoryType.INSTANCE_PLACE, p.getUniqueId());

        wrapper.data.put("instanceId", instance.getId());
        wrapper.data.put("instanceSnapshot", instance); // Instance 必须可序列化

        return wrapper;
    }
    
    public static IdunnHistoryWrapper stagedPlaceHistory(Player p, Instance instance, UUID parentTemplateId, Long lockTimestamp) {
        IdunnHistoryWrapper wrapper = new IdunnHistoryWrapper(HistoryType.STAGED_PLACE, p.getUniqueId());

        wrapper.data.put("instanceId", instance.getId());
        wrapper.data.put("instanceSnapshot", instance);
        wrapper.data.put("parentTemplateId", parentTemplateId);
        wrapper.data.put("lockTimestamp", lockTimestamp);

        return wrapper;
    }

    // =================================
    // HELPER METHODS
    // =================================

    /**
     * 这个历史记录是否有效。
     * @return 有效则返回 null，无效则返回提示信息地翻译键名
     */
    @Nullable
    public String isEffective() {
        if (this.historyType == HistoryType.INSTANCE_PLACE) {
            return null;
        }
        if (this.historyType == HistoryType.STAGED_PLACE) {
            UUID templateUUID = (UUID) this.data.get("parentTemplateId");
            Long lockTimestamp = (Long) this.data.get("lockTimestamp");
            if(templateUUID == null || lockTimestamp == null) {
                return "history.staged.error.wrong_data";
            }
            Template t = IdunnTemplates.getInstance().getTemplateManager().getTemplate(templateUUID);
            if(t == null) {
                return "history.staged.error.template_not_found";
            }
            if (t.isLocked() && Objects.equals(t.getMetadata().getLockedTimestamp(), lockTimestamp)) {
                return null;
            } else {
                return "history.staged.error.already_been_committed";
            }
        }
        return "history.error.unknown_type";
    }

    private InstanceRepository getInstanceRepository() {
        if (IdunnTemplates.getInstance() != null) {
            return IdunnTemplates.getInstance().getInstanceRepository();
        }
        return null;
    }
    
    private com.jackyblackson.idunntemplates.manager.TemplateManager getTemplateManager() {
        if (IdunnTemplates.getInstance() != null) {
            return IdunnTemplates.getInstance().getTemplateManager();
        }
        return null;
    }
}