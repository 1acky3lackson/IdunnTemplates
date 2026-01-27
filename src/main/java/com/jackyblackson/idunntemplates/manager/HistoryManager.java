package com.jackyblackson.idunntemplates.manager;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.domain.PlayerSession;
import com.jackyblackson.idunntemplates.core.history.ChangeSetFingerprintCalculator;
import com.jackyblackson.idunntemplates.core.history.IdunnHistoryWrapper;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.history.changeset.ChangeSet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage;

public class HistoryManager implements Listener {

    private final SessionManager sessionManager;

    public HistoryManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    // =================================================================================
    // API: REMEMBER (保持不变)
    // =================================================================================

    public void remember(Player player, EditSession editSession, IdunnHistoryWrapper wrapper) {
        BukkitPlayer adaptedPlayer = BukkitAdapter.adapt(player);
        LocalSession faweSession = WorldEdit.getInstance().getSessionManager().get(adaptedPlayer);
        PlayerSession mySession = sessionManager.getSession(player.getUniqueId());

        if (faweSession == null || mySession == null) return;

        int preSize = getFaweHistorySize(faweSession);

        // 执行 FAWE 记录
        faweSession.remember(editSession);

        // 获取刚生成的 ChangeSet 并计算指纹
        List<?> history = faweSession.getHistory();
        if (history != null && !history.isEmpty()) {
            Object lastObj = history.get(history.size() - 1);
            if (lastObj instanceof ChangeSet) {
                String fingerprint = ChangeSetFingerprintCalculator.calculateFingerprint((ChangeSet) lastObj);
                wrapper.setFingerprint(fingerprint);
            }
        }

        // 同步 Map (Index)
        syncMapAfterRemember(mySession.getPreference().getHistoryMap(), preSize, getFaweHistorySize(faweSession), wrapper);
        sessionManager.saveSession(player.getUniqueId());
    }

    // =================================================================================
    // 逻辑核心: 预处理 UNDO / REDO
    // =================================================================================

    /**
     * 预处理撤回操作。
     * 模拟 FAWE 的撤回循环，找到涉及的 Idunn 记录并处理，但不阻止 FAWE 执行。
     */
    private void preProcessUndo(Player targetPlayer, int amount) {
        LocalSession session = getSession(targetPlayer);
        if (session == null) return;

        List<?> history = session.getHistory();
        if (history == null || history.isEmpty()) return;

        // 获取当前状态
        // FAWE 逻辑：Undo 从 historyIndex 开始，向后(index减小)执行
        int currentIndex = session.getHistoryIndex(); // 当前指向"最后一次操作"
        int negIndex = session.getHistoryNegativeIndex(); // 当前倒数位置

        // 模拟循环
        for (int i = 0; i < amount; i++) {
            // 检查是否到底
            // 注意：当 negIndex == size 时，说明已经全部撤回到起点了
            if (negIndex + i >= history.size()) {
                break;
            }

            // 计算这一步 Undo 将要撤销的 Index
            // 当前 Index 是 currentIndex，撤销它之后，指针会变成 currentIndex - 1
            // 循环中每一步，指针都会相对前一步 -1
            int targetIndex = currentIndex - i;

            if (targetIndex >= 0) {
                processIdunnLogic(targetPlayer, session, targetIndex, true);
            }
        }
    }

    /**
     * 预处理重做操作。
     */
    private void preProcessRedo(Player targetPlayer, int amount) {
        LocalSession session = getSession(targetPlayer);
        if (session == null) return;

        List<?> history = session.getHistory();
        if (history == null || history.isEmpty()) return;

        // 获取当前状态
        // FAWE 逻辑：Redo 意味着 historyNegativeIndex 减小，指针 index 增加
        int currentIndex = session.getHistoryIndex();
        int negIndex = session.getHistoryNegativeIndex();

        // 模拟循环
        for (int i = 0; i < amount; i++) {
            // 检查是否到顶
            // 如果 negIndex == 0，说明已经在最新状态，无法 Redo
            if (negIndex - i <= 0) {
                break;
            }

            // 计算这一步 Redo 将要恢复的 Index
            // Redo 是将"未来"的操作重新生效。
            // 按照 FAWE 源码：negIndex--; ChangeSet = get(getHistoryIndex());
            // 意味着 Redo 的目标是 (currentIndex + 1 + i) ?
            // 让我们回看 FAWE 源码：
            // redo() -> historyNegativeIndex--; ChangeSet c = history.get(getHistoryIndex());
            // getHistoryIndex() 是基于 size 和 negativeIndex 计算的： size - 1 - negIndex
            // 所以，Redo 时，先减少 negIndex (意味着 Index 增加 1)，然后获取那个新的位置。
            // 所以目标 Index = currentIndex + 1 + i

            int targetIndex = currentIndex + 1 + i;

            if (targetIndex < history.size()) {
                processIdunnLogic(targetPlayer, session, targetIndex, false);
            }
        }
    }

    /**
     * 处理单个历史节点的 Idunn 逻辑 (验证指纹 -> 执行/清理)
     */
    private void processIdunnLogic(Player player, LocalSession session, int index, boolean isUndo) {
        // 1. 获取玩家数据
        PlayerSession playerSession = sessionManager.getSession(player.getUniqueId());
        if (playerSession == null) return;
        Map<Integer, IdunnHistoryWrapper> map = playerSession.getPreference().getHistoryMap();

        // 2. 检查是否有记录
        IdunnHistoryWrapper wrapper = map.get(index);
        if (wrapper == null) return;

        // 3. 惰性验证 (指纹检查)
        List<?> history = session.getHistory();
        Object faweObj = history.get(index);

        // 如果对象是 ChangeSet，计算指纹并比对
        if (faweObj instanceof ChangeSet) {
            String currentFp = ChangeSetFingerprintCalculator.calculateFingerprint((ChangeSet) faweObj);
            if (!wrapper.validateFingerprint(currentFp)) {
                // 指纹不匹配：说明发生了覆盖/错位，删除脏数据
                map.remove(index);
                sessionManager.saveSession(player.getUniqueId());
                player.sendMessage(getMessage(player, "history.manager.remove.stale", String.valueOf(index)));
                return;
            }
        } else {
            map.remove(index); // 类型不对，清理
            sessionManager.saveSession(player.getUniqueId());
            player.sendMessage(getMessage(player, "history.manager.remove.wrong_type", String.valueOf(index), faweObj.getClass().getName()));
            return;
        }

        // 4. 验证通过，执行业务逻辑
        if (isUndo) {
            wrapper.makeUndo();
        } else {
            wrapper.makeRedo();
        }
    }

    // =================================================================================
    // 监听器: 拦截与解析
    // =================================================================================

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        String[] split = message.split(" ");
        if (split.length == 0) return;

        String command = split[0].toLowerCase();
        // 兼容 /undo, //undo, /fawe:undo
        boolean isUndo = command.endsWith("undo");
        boolean isRedo = command.endsWith("redo");

        // 快速过滤非 WE 指令
        if (!isUndo && !isRedo) return;
        // 简单的判定，防止拦截了其他插件的同名指令(虽然 undo/redo 极其罕见)
        if (!command.equals("/undo") && !command.equals("//undo") && !command.equals("/redo") && !command.equals("//redo")) {
            if (!command.contains("worldedit") && !command.contains("fawe")) return;
        }

        Player sender = event.getPlayer();
        if (!sender.hasPermission("worldedit.history.undo")) return;

        // --- 解析参数 ---
        // 格式: /undo [amount] [player] 或 /undo [player] [amount]
        // 默认值
        int amount = 1;
        Player targetPlayer = sender;

        if (split.length > 1) {
            String arg1 = split[1];
            String arg2 = (split.length > 2) ? split[2] : null;

            // 尝试解析 Arg1
            Integer arg1Int = tryParseInt(arg1);

            if (arg1Int != null) {
                // Arg1 是数字 -> amount
                amount = arg1Int;
                // 此时 Arg2 可能是玩家
                if (arg2 != null) {
                    Player p = Bukkit.getPlayer(arg2);
                    if (p != null) targetPlayer = p;
                }
            } else {
                // Arg1 不是数字 -> 可能是玩家
                Player p = Bukkit.getPlayer(arg1);
                if (p != null) targetPlayer = p;

                // 此时 Arg2 可能是数字
                if (arg2 != null) {
                    Integer arg2Int = tryParseInt(arg2);
                    if (arg2Int != null) amount = arg2Int;
                }
            }
        }

        // 权限检查: 如果操作的是他人
        if (!targetPlayer.getUniqueId().equals(sender.getUniqueId())) {
            if (!sender.hasPermission(isUndo ? "worldedit.history.undo.other" : "worldedit.history.redo.other")) {
                return; // 让 FAWE 自己去拒绝权限，我们不插手
            }
        }

        // --- 执行预处理 ---
        // 我们只负责处理数据，处理完后 event.setCancelled(false) 让 FAWE 处理方块
        if (isUndo) {
            preProcessUndo(targetPlayer, amount);
        } else {
            preProcessRedo(targetPlayer, amount);
        }

        // 显式放行 (虽然默认就是 false，但表明意图)
        event.setCancelled(false);
    }

    // =================================================================================
    // 内部逻辑: Map 同步 (处理 Shift)
    // =================================================================================

    private void syncMapAfterRemember(Map<Integer, IdunnHistoryWrapper> map, int preSize, int currentSize, IdunnHistoryWrapper newWrapper) {
        int newIndex = currentSize - 1;
        // 截断处理
        map.keySet().removeIf(key -> key >= newIndex);
        // 溢出处理
        if (preSize > 0 && preSize == currentSize) {
            map.remove(0);
            Map<Integer, IdunnHistoryWrapper> shiftedMap = new HashMap<>();
            for (Map.Entry<Integer, IdunnHistoryWrapper> entry : map.entrySet()) {
                int oldKey = entry.getKey();
                if (oldKey > 0) shiftedMap.put(oldKey - 1, entry.getValue());
            }
            map.clear();
            map.putAll(shiftedMap);
        }
        map.put(newIndex, newWrapper);
    }

    // =================================================================================
    // 辅助工具
    // =================================================================================

    private LocalSession getSession(Player player) {
        try {
            BukkitPlayer adapted = BukkitAdapter.adapt(player);
            return WorldEdit.getInstance().getSessionManager().get(adapted);
        } catch (Exception e) { return null; }
    }

    private int getFaweHistorySize(LocalSession session) {
        try {
            List<?> list = session.getHistory();
            return list == null ? 0 : list.size();
        } catch (Exception e) { return 0; }
    }

    private Integer tryParseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}