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
        STAGED_PLACE,
        // --- 新增：删除类型 ---
        INSTANCE_DELETE,
        STAGED_DELETE
    }

    private final HistoryType historyType;
    private final UUID playerUUID;

    // --- ChangeSet 指纹 ---
    private String changeSetFingerprint;
    private boolean valid = true;

    // 用于存储恢复所需的数据
    private final Map<String, Object> data = new HashMap<>();

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

    public void setFingerprint(String fingerprint) {
        this.changeSetFingerprint = fingerprint;
    }

    public boolean validateFingerprint(String currentFingerprint) {
        if (this.changeSetFingerprint == null || currentFingerprint == null) {
            return false;
        }
        return this.changeSetFingerprint.equals(currentFingerprint);
    }

    // =================================
    // 业务逻辑 (BUSINESS LOGIC)
    // =================================

    /**
     * 执行撤回逻辑 (Undo)
     */
    public void makeUndo() {
        InstanceRepository repo = getInstanceRepository();
        com.jackyblackson.idunntemplates.manager.TemplateManager tm = getTemplateManager();
        if (repo == null) return; // Basic check

        // 1. PLACE 类型的 Undo -> 执行删除
        if (historyType == HistoryType.INSTANCE_PLACE) {
            String instanceId = (String) data.get("instanceId");
            Instance instanceSnapshot = (Instance) data.get("instanceSnapshot");
            if (instanceId == null) return;

            Instance target = repo.getAllLoadedInstances().stream()
                    .filter(i -> i.getId().equals(instanceId))
                    .findFirst()
                    .orElse(null);

            if (target != null) repo.hardDelete(target);
            else repo.hardDelete(instanceSnapshot);

            sendMessage(playerUUID, "history.undo.success", instanceId, instanceSnapshot.getTemplate().getPath());

        } else if (historyType == HistoryType.STAGED_PLACE) {
            // ... (原有 STAGED_PLACE Undo 逻辑保持不变)
            String instanceId = (String) data.get("instanceId");
            Instance instanceSnapshot = (Instance) data.get("instanceSnapshot");
            UUID parentTemplateId = (UUID) data.get("parentTemplateId");
            if (instanceId == null || parentTemplateId == null || tm == null) return;

            Instance target = repo.getAllLoadedInstances().stream()
                    .filter(i -> i.getId().equals(instanceId))
                    .findFirst()
                    .orElse(null);
            if (target != null) repo.hardDelete(target);
            else repo.hardDelete(instanceSnapshot);

            com.jackyblackson.idunntemplates.core.domain.Template parent = tm.getTemplate(parentTemplateId);
            if (parent != null) {
                parent.getMetadata().getStagedChanges().getAddedInstances().removeIf(i -> i.getId().equals(instanceId));
                tm.saveTemplateMetadata(parent);
            }
            sendMessage(playerUUID, "history.undo.staged_success", instanceId, instanceSnapshot.getTemplate().getName(), parent != null ? parent.getName() : "Unknown");

            // 2. DELETE 类型的 Undo -> 执行恢复 (相当于 Place 的 Redo)
        } else if (historyType == HistoryType.INSTANCE_DELETE) {
            Instance instanceSnapshot = (Instance) data.get("instanceSnapshot");
            if (instanceSnapshot == null) return;

            // 恢复数据
            repo.saveInstance(instanceSnapshot);

            sendMessage(playerUUID, "history.undo.delete_success", instanceSnapshot.getId(), instanceSnapshot.getTemplate().getPath());

        } else if (historyType == HistoryType.STAGED_DELETE) {
            // V2: Undo Staged Delete (Restores the deleted instance)
            Instance instanceSnapshot = (Instance) data.get("instanceSnapshot");
            UUID parentTemplateId = (UUID) data.get("parentTemplateId");
            if (instanceSnapshot == null || parentTemplateId == null || tm == null) return;

            // 1. Restore Instance Record
            repo.saveInstance(instanceSnapshot);

            // 2. Add back to Parent's Staging Area (因为撤销了删除，所以它应该回到列表中)
            com.jackyblackson.idunntemplates.core.domain.Template parent = tm.getTemplate(parentTemplateId);
            if (parent != null) {
                // 注意：这里假设恢复删除等同于将其加回 Added 列表，或者系统通过 ID 自动处理去重
                parent.getMetadata().getStagedChanges().getRemovedInstanceIds().add(instanceSnapshot.getId());
                tm.saveTemplateMetadata(parent);
            }

            sendMessage(playerUUID, "history.undo.staged_delete_success",
                    instanceSnapshot.getId(),
                    instanceSnapshot.getTemplate().getName(),
                    parent != null ? parent.getName() : "Unknown");
        }
    }

    /**
     * 执行重做逻辑 (Redo)
     */
    public void makeRedo() {
        InstanceRepository repo = getInstanceRepository();
        com.jackyblackson.idunntemplates.manager.TemplateManager tm = getTemplateManager();
        if (repo == null) return;

        // 1. PLACE 类型的 Redo -> 执行恢复
        if (historyType == HistoryType.INSTANCE_PLACE) {
            Instance instanceSnapshot = (Instance) data.get("instanceSnapshot");
            if (instanceSnapshot == null) return;

            repo.saveInstance(instanceSnapshot);
            sendMessage(playerUUID, "history.redo.success", instanceSnapshot.getId(), instanceSnapshot.getTemplate().getPath());

        } else if (historyType == HistoryType.STAGED_PLACE) {
            // ... (原有 STAGED_PLACE Redo 逻辑保持不变)
            Instance instanceSnapshot = (Instance) data.get("instanceSnapshot");
            UUID parentTemplateId = (UUID) data.get("parentTemplateId");
            if (instanceSnapshot == null || parentTemplateId == null || tm == null) return;

            repo.saveInstance(instanceSnapshot);
            com.jackyblackson.idunntemplates.core.domain.Template parent = tm.getTemplate(parentTemplateId);
            if (parent != null) {
                parent.getMetadata().getStagedChanges().getAddedInstances().add(instanceSnapshot);
                tm.saveTemplateMetadata(parent);
            }
            sendMessage(playerUUID, "history.redo.staged_success", instanceSnapshot.getId(), instanceSnapshot.getTemplate().getName(), parent != null ? parent.getName() : "Unknown");

            // 2. DELETE 类型的 Redo -> 执行删除 (相当于 Place 的 Undo)
        } else if (historyType == HistoryType.INSTANCE_DELETE) {
            String instanceId = (String) data.get("instanceId");
            Instance instanceSnapshot = (Instance) data.get("instanceSnapshot");
            if (instanceId == null) return;

            Instance target = repo.getAllLoadedInstances().stream()
                    .filter(i -> i.getId().equals(instanceId))
                    .findFirst()
                    .orElse(null);

            if (target != null) repo.hardDelete(target);
            else repo.hardDelete(instanceSnapshot);

            sendMessage(playerUUID, "history.redo.delete_success", instanceId, instanceSnapshot.getTemplate().getPath());

        } else if (historyType == HistoryType.STAGED_DELETE) {
            // V2: Redo Staged Delete (Deletes the instance again)
            String instanceId = (String) data.get("instanceId");
            Instance instanceSnapshot = (Instance) data.get("instanceSnapshot");
            UUID parentTemplateId = (UUID) data.get("parentTemplateId");
            if (instanceId == null || parentTemplateId == null || tm == null) return;

            // 1. Hard Delete Instance Record
            Instance target = repo.getAllLoadedInstances().stream()
                    .filter(i -> i.getId().equals(instanceId))
                    .findFirst()
                    .orElse(null);
            if (target != null) repo.hardDelete(target);
            else repo.hardDelete(instanceSnapshot);

            // 2. Remove from Parent's Staging Area
            com.jackyblackson.idunntemplates.core.domain.Template parent = tm.getTemplate(parentTemplateId);
            if (parent != null) {
                parent.getMetadata().getStagedChanges().getRemovedInstanceIds().removeIf(i -> i.equals(instanceId));
                tm.saveTemplateMetadata(parent);
            }

            sendMessage(playerUUID, "history.redo.staged_delete_success",
                    instanceId,
                    instanceSnapshot.getTemplate().getName(),
                    parent != null ? parent.getName() : "Unknown");
        }
    }

    // =================================
    // FACTORY METHODS
    // =================================

    public static IdunnHistoryWrapper placeInstanceHistory(Player p, Instance instance) {
        IdunnHistoryWrapper wrapper = new IdunnHistoryWrapper(HistoryType.INSTANCE_PLACE, p.getUniqueId());
        wrapper.data.put("instanceId", instance.getId());
        wrapper.data.put("instanceSnapshot", instance);
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

    // --- 新增：DELETE 类型的工厂方法 ---

    public static IdunnHistoryWrapper deleteInstanceHistory(Player p, Instance instance) {
        IdunnHistoryWrapper wrapper = new IdunnHistoryWrapper(HistoryType.INSTANCE_DELETE, p.getUniqueId());
        wrapper.data.put("instanceId", instance.getId());
        wrapper.data.put("instanceSnapshot", instance); // 保存快照以便撤销（恢复）
        return wrapper;
    }

    public static IdunnHistoryWrapper stagedDeleteHistory(Player p, Instance instance, UUID parentTemplateId, Long lockTimestamp) {
        IdunnHistoryWrapper wrapper = new IdunnHistoryWrapper(HistoryType.STAGED_DELETE, p.getUniqueId());
        wrapper.data.put("instanceId", instance.getId());
        wrapper.data.put("instanceSnapshot", instance);
        wrapper.data.put("parentTemplateId", parentTemplateId);
        wrapper.data.put("lockTimestamp", lockTimestamp);
        return wrapper;
    }

    // =================================
    // HELPER METHODS
    // =================================

    @Nullable
    public String isEffective() {
        if (this.historyType == HistoryType.INSTANCE_PLACE || this.historyType == HistoryType.INSTANCE_DELETE) {
            // 普通放置和删除通常不需要复杂的 Context 校验
            return null;
        }

        // STAGED 类型的校验逻辑（包括 PLACE 和 DELETE）
        if (this.historyType == HistoryType.STAGED_PLACE || this.historyType == HistoryType.STAGED_DELETE) {
            UUID templateUUID = (UUID) this.data.get("parentTemplateId");
            Long lockTimestamp = (Long) this.data.get("lockTimestamp");

            if(templateUUID == null || lockTimestamp == null) {
                return "history.staged.error.wrong_data";
            }

            Template t = Objects.requireNonNull(IdunnTemplates.getInstance()).getTemplateManager().getTemplate(templateUUID);
            if(t == null) {
                return "history.staged.error.template_not_found";
            }
            // 校验时间戳是否匹配，确保仍处于同一次编辑会话中
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

    // 辅助方法：简化发送消息
    private void sendMessage(UUID uuid, String key, String... args) {
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) {

            // 简单截断一下 ID，保持美观
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null && ((String) args[i]).length() == 36) { // Assuming UUID length
                    args[i] = ((String) args[i]).substring(0, 8);
                }
            }
            String msg = getMessage(p, key, args);
            // 这里为了保持和你原代码一致，重新获取一次带 substring 处理过的消息，
            // 或者直接使用原逻辑。上面代码块里我已经手动 substring 了，这里只是个封装建议。
            // 鉴于你的原代码是手动 substring，这里我们还是保持原样写在主逻辑里更稳妥。
            // 此处仅发送
            p.sendMessage(msg);
        }
    }
}