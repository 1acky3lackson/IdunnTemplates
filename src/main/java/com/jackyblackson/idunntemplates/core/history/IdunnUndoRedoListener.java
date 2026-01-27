package com.jackyblackson.idunntemplates.core.history;

import com.fastasyncworldedit.core.FaweAPI;
import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.util.BukkitPromise;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class IdunnUndoRedoListener implements Listener {

//    private final IdunnHistoryManager historyManager; // 假设你有管理 IdunnPlayerHistory 的类
//
//    public IdunnUndoRedoListener(IdunnHistoryManager manager) {
//        this.historyManager = manager;
//    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage().toLowerCase();

        boolean isUndo = msg.equals("//undo") || msg.equals("/undo");
        boolean isRedo = msg.equals("//redo") || msg.equals("/redo");

//        if (!isUndo && !isRedo) return;

        Player player = event.getPlayer();
        if (!player.hasPermission("worldedit.history.undo")) return;

        // 1. 拦截指令
//        event.setCancelled(true);

        // 2. 获取 FAWE 内部对象
        var adaptedPlayer = BukkitAdapter.adapt(player);
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(adaptedPlayer);
        EditSession resultSession = null;

        var historyList = session.getHistory();
        var historyIndex = session.getHistoryIndex();
        var historyNegIndex = session.getHistoryNegativeIndex();

        player.sendMessage("BEFORE: HistorySize=" + historyList.size() + "; index=" + historyIndex + "; negIndex=" + historyNegIndex);
        BukkitPromise.resolve(IdunnTemplates.getInstance(), null).then(voidResult -> {
            var adaptedPlayerAfter = BukkitAdapter.adapt(player);
            LocalSession sessionAfter  = WorldEdit.getInstance().getSessionManager().get(adaptedPlayer);
            EditSession resultSessionAfter  = null;

            var historyListAfter  = session.getHistory();
            var historyIndexAfter  = session.getHistoryIndex();
            var historyNegIndexAfter  = session.getHistoryNegativeIndex();
            player.sendMessage("AFTER: HistorySize=" + historyListAfter.size() + "; index=" + historyIndexAfter + "; negIndex=" + historyNegIndexAfter);
        });


//        // 3. 代理执行 FAWE 的操作
//        if (isUndo) {
//            // undo() 返回的是“被撤回的那个 Session”
//            resultSession = session.undo(BukkitAdapter.adapt(player.getWorld()), BukkitAdapter.adapt(player));
//        } else {
//            // redo() 返回的是“被重做的那个 Session”
//            resultSession = session.redo(BukkitAdapter.adapt(player.getWorld()), BukkitAdapter.adapt(player));
//        }
//
//        // 4. 结果处理
//        if (resultSession != null) {
//            // 获取该 Session 的时间戳（这就是我们的 editSessionAddress）
//            // 注意：FAWE 不同版本 getTime() 方法位置可能不同，通常在 EditSession 或其内部
//            long sessionTime = resultSession.getDate(); // 或者 resultSession.getTimestamp();
//
//            // 获取该玩家的自定义历史记录
//            IdunnPlayerHistory myHistory = historyManager.getHistory(player);
//
//            // 尝试匹配 Wrapper
//            IdunnHistoryWrapper wrapper = myHistory.get(sessionTime);
//
//            if (wrapper != null) {
//                // --- 命中！这是我们插件的操作 ---
//                if (isUndo) {
//                    player.sendMessage("§e[Idunn] 同步撤回关联数据...");
//                    wrapper.makeUndo();
//                } else {
//                    player.sendMessage("§e[Idunn] 同步恢复关联数据...");
//                    wrapper.makeRedo();
//                }
//            } else {
//                // 未命中，说明是其他插件或玩家手撸的操作，FAWE 已处理完方块，无需我们干预
//                player.sendMessage("§7[FAWE] 操作已" + (isUndo ? "撤回" : "重做"));
//            }
//
//        } else {
//            player.sendMessage("§c没有可" + (isUndo ? "撤回" : "重做") + "的历史记录。");
//        }
    }
}
