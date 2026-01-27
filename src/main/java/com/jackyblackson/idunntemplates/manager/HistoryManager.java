package com.jackyblackson.idunntemplates.manager;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.domain.PlayerSession;
import com.jackyblackson.idunntemplates.core.history.ChangeSetFingerprintCalculator;
import com.jackyblackson.idunntemplates.core.history.IdunnHistoryWrapper;
import com.jackyblackson.idunntemplates.core.util.BukkitPromise;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.history.changeset.ChangeSet;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryManager implements Listener {

    private final SessionManager sessionManager;
    private final WorldEdit worldEdit = WorldEdit.getInstance();

    public HistoryManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * 检查给定的 FAWE 历史索引是否对应一个 Idunn 的历史记录包装器。
     */
    private boolean isWrapperExist(Player player, int index) {
        if (player == null) return false;
        PlayerSession session = sessionManager.getSession(player.getUniqueId());
        if (session == null) return false;
        Map<Integer, IdunnHistoryWrapper> map = session.getPreference().getHistoryMap();
        return map.containsKey(index);
    }

    // =================================================================================
    // API: REMEMBER (核心同步逻辑 + 指纹计算)
    // =================================================================================

    public void remember(Player player, EditSession editSession, IdunnHistoryWrapper wrapper) {
        BukkitPlayer adaptedPlayer = BukkitAdapter.adapt(player);
        LocalSession faweSession = WorldEdit.getInstance().getSessionManager().get(adaptedPlayer);
        PlayerSession mySession = sessionManager.getSession(player.getUniqueId());

        if (faweSession == null || mySession == null) return;

        // 1. 获取操作前的状态
        int preSize = getFaweHistorySize(faweSession);
        // player.sendMessage("HistoryManager.remember: preSize = " + preSize);

        // 2. 执行 FAWE 记录
        faweSession.remember(editSession);

        // 3. 获取操作后的状态
        int currentSize = getFaweHistorySize(faweSession);

        // 4. --- 关键：获取刚生成的 ChangeSet 并计算指纹 ---
        List<?> history = faweSession.getHistory();
        if (history != null && !history.isEmpty()) {
            // 新操作位于列表末尾
            Object lastObj = history.get(history.size() - 1);
            if (lastObj instanceof ChangeSet) {
                String fingerprint = ChangeSetFingerprintCalculator.calculateFingerprint((ChangeSet) lastObj);
                wrapper.setFingerprint(fingerprint);
            }
        }

        // 5. 同步我们的 Map
        syncMapAfterRemember(mySession.getPreference().getHistoryMap(), preSize, currentSize, wrapper);

        // 可选：触发保存
        sessionManager.saveSession(player.getUniqueId());
    }

    // =================================================================================
    // API: UNDO / REDO (包含惰性验证逻辑)
    // =================================================================================

    public boolean undo(Player player) {
        BukkitPlayer adaptedPlayer = BukkitAdapter.adapt(player);
        LocalSession faweSession = WorldEdit.getInstance().getSessionManager().get(adaptedPlayer);
        if (faweSession == null) return false;

        // 1. 预判 Index
        int targetIndex = getNextUndoIndex(faweSession);
        if (targetIndex == -1) return false;

        // 2. 获取并验证 Wrapper
        IdunnHistoryWrapper wrapper = getValidatedWrapper(player, faweSession, targetIndex);
        if (wrapper != null) {
            wrapper.makeUndo();
            return true;
        }

        // 3. 执行 FAWE 撤回
//        BlockBag blockBag = adaptedPlayer instanceof Player ? faweSession.getBlockBag(adaptedPlayer) : null;
//        EditSession result = faweSession.undo(blockBag, BukkitAdapter.adapt(player));
//
//        if (result != null) {
//            worldEdit.flushBlockBag(adaptedPlayer, result);
//
//            // 4. 只有当 Wrapper 通过验证且存在时，才执行 Idunn 撤回
//
//            return true;
//        }
        return false;
    }

    public boolean redo(Player player) {
        BukkitPlayer adaptedPlayer = BukkitAdapter.adapt(player);
        LocalSession faweSession = WorldEdit.getInstance().getSessionManager().get(adaptedPlayer);
        if (faweSession == null) return false;

        // 1. 预判 Index
        int targetIndex = getNextRedoIndex(faweSession);
        if (targetIndex == -1) return false;

        // 2. 获取并验证 Wrapper
        IdunnHistoryWrapper wrapper = getValidatedWrapper(player, faweSession, targetIndex);
        if (wrapper != null) {
            wrapper.makeRedo();
            return true;
        }

        // 3. 执行 FAWE 重做
//        BlockBag blockBag = adaptedPlayer instanceof Player ? faweSession.getBlockBag(adaptedPlayer) : null;
//        EditSession result = faweSession.redo(blockBag, BukkitAdapter.adapt(player));
//
//        if (result != null) {
//            worldEdit.flushBlockBag(adaptedPlayer, result);
//
//            // 4. 只有当 Wrapper 通过验证且存在时，才执行 Idunn 重做
//
//            return true;
//        }
        return false;
    }

    // =================================================================================
    // 核心：惰性验证与清理 (LAZY VALIDATION)
    // =================================================================================

    /**
     * 获取指定 Index 的 Wrapper，并执行指纹验证。
     * 如果指纹不匹配（说明发生了截断或覆盖），则自动从 Map 中清理掉该 Wrapper 并返回 null。
     */
    private IdunnHistoryWrapper getValidatedWrapper(Player player, LocalSession faweSession, int index) {
        PlayerSession mySession = sessionManager.getSession(player.getUniqueId());
        if (mySession == null) return null;

        Map<Integer, IdunnHistoryWrapper> map = mySession.getPreference().getHistoryMap();
        IdunnHistoryWrapper wrapper = map.get(index);

        if (wrapper == null) return null;

        // 开始验证
        List<?> history = faweSession.getHistory();

        // 1. 越界检查 (Index 超出当前历史范围，说明发生了截断)
        if (index < 0 || index >= history.size()) {
            map.remove(index);
            sessionManager.saveSession(player.getUniqueId());
            player.sendMessage("§8[Idunn] Auto-cleaned truncated history at index " + index);
            return null;
        }

        // 2. 指纹检查
        Object faweObj = history.get(index);
        if (faweObj instanceof ChangeSet) {
            String currentFp = ChangeSetFingerprintCalculator.calculateFingerprint((ChangeSet) faweObj);

            if (!wrapper.validateFingerprint(currentFp)) {
                // 指纹不匹配，说明该位置被覆盖了 (Overwrite)
                map.remove(index);
                sessionManager.saveSession(player.getUniqueId());
                player.sendMessage("§8[Idunn] Auto-cleaned stale history at index " + index);
                return null;
            }
        } else {
            // 如果取出的不是 ChangeSet，说明类型不对，也要清理
            map.remove(index);
            player.sendMessage("§8[Idunn] Auto-cleaned wrong typed history at index " + index);
            sessionManager.saveSession(player.getUniqueId());
            return null;
        }

        // 验证通过
        return wrapper;
    }

    // =================================================================================
    // 监听器: 智能拦截指令
    // =================================================================================

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage().toLowerCase().trim();
        boolean isUndo = msg.equals("//undo") || msg.equals("/undo") || msg.equals("/fawe:undo");
        boolean isRedo = msg.equals("//redo") || msg.equals("/redo") || msg.equals("/fawe:redo");

        if (!isUndo && !isRedo) return;

        Player player = event.getPlayer();
        if (!player.hasPermission("worldedit.history.undo")) return;

        // 1. 获取 FAWE Session
        BukkitPlayer adaptedPlayer = BukkitAdapter.adapt(player);
        LocalSession faweSession = WorldEdit.getInstance().getSessionManager().get(adaptedPlayer);
        if (faweSession == null) return;

        // 2. [Peek] 预读：看一眼即将被操作的 Index 是多少
        int targetIndex = -1;
        if (isUndo) {
            targetIndex = getNextUndoIndex(faweSession);
        } else {
            targetIndex = getNextRedoIndex(faweSession);
        }

        if (targetIndex == -1) return;

        // 3. [Check] 检查这个 Index 是否属于 Idunn 的历史记录
        // 注意：这里仅仅检查是否存在 key。
        // 如果该 key 已经失效（指纹不匹配），会在随后的 undo()/redo() 调用中被 getValidatedWrapper 自动清理
        boolean isIdunnRecord = isWrapperExist(player, targetIndex);

        if (isIdunnRecord) {
            // A. 是我们的记录 -> 自定义逻辑

            if (isUndo) {
                undo(player);
            } else {
                redo(player);
            }
        }
        // B. 不是我们的记录 -> 放行

        // Debug After
        /*
        BukkitPromise.resolve(IdunnTemplates.getInstance(), null).then(voidResult -> {
             // ... debugging logic ...
        });
        */
    }

    // =================================================================================
    // 内部逻辑: Map 同步 (处理 Shift)
    // =================================================================================

    private void syncMapAfterRemember(Map<Integer, IdunnHistoryWrapper> map, int preSize, int currentSize, IdunnHistoryWrapper newWrapper) {
        int newIndex = currentSize - 1;

        // 1. [截断处理] 清除旧的"未来"
        map.keySet().removeIf(key -> key >= newIndex);

        // 2. [溢出处理] 列表满时的左移
        if (preSize > 0 && preSize == currentSize) {
            map.remove(0);
            Map<Integer, IdunnHistoryWrapper> shiftedMap = new HashMap<>();
            for (Map.Entry<Integer, IdunnHistoryWrapper> entry : map.entrySet()) {
                int oldKey = entry.getKey();
                if (oldKey > 0) {
                    shiftedMap.put(oldKey - 1, entry.getValue());
                }
            }
            map.clear();
            map.putAll(shiftedMap);
        }

        // 3. [添加]
        map.put(newIndex, newWrapper);
    }

    // =================================================================================
    // 内部逻辑: 辅助工具
    // =================================================================================

    private int getFaweHistorySize(LocalSession session) {
        try {
            List<?> list = session.getHistory();
            return list == null ? 0 : list.size();
        } catch (Exception e) { return 0; }
    }

    private int getNextUndoIndex(LocalSession session) {
        try {
            int pointer = session.getHistoryIndex();
            // Undo 对应当前指针位置 (FAWE 逻辑: 指针指向下一个空位或当前栈顶?
            // 通常 undo 是操作 history[index-1] 然后 index--，或者 history[index] 取决于实现细节。
            // 根据你的调试反馈，这里似乎直接用 historyIndex 即可)
            return pointer;
        } catch (Exception e) { return -1; }
    }

    private int getNextRedoIndex(LocalSession session) {
        try {
            int pointer = session.getHistoryIndex();
            int negPointer = session.getHistoryNegativeIndex();
            // Redo 意味着向未来走
            if (negPointer > 0) {
                return pointer + 1;
            }
        } catch (Exception e) { return -1; }
        return -1;
    }
}