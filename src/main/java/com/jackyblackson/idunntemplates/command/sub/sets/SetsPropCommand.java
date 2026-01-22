package com.jackyblackson.idunntemplates.command.sub.sets;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.set.TemplateSet;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SetsPropCommand extends BaseSubCommand {

    private final SessionManager sessionManager;

    public SetsPropCommand(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn set prop <key> <val>
        if (args.length < 3) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.prop.usage"));
            return;
        }
        
        String key = args[1].toLowerCase();
        String val = args[2].toLowerCase();
        
        TemplateSet set = sessionManager.getSession(player.getUniqueId()).getPreference().getCurrentSet();
        
        switch (key) {
            case "rotation":
            case "rot":
                set.setRotate(val);
                break;
            case "flipx":
                set.setFlipX(val);
                break;
            case "flipz":
                set.setFlipZ(val);
                break;
            default:
                player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.prop.unknown"));
                return;
        }
        
        sessionManager.saveSession(player.getUniqueId());
        player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.prop.success", key, val));
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 2) {
            List<String> props = new ArrayList<>();
            props.add("rotation"); props.add("flipx"); props.add("flipz");
            return filter(props, args[1]);
        }
        if (args.length == 3) {
            String key = args[1].toLowerCase();
            if (key.equals("rotation") || key.equals("rot")) {
                List<String> rots = new ArrayList<>();
                rots.add("0"); rots.add("90"); rots.add("180"); rots.add("270"); rots.add("random");
                return filter(rots, args[2]);
            }
            if (key.equals("flipx") || key.equals("flipz")) {
                List<String> bools = new ArrayList<>();
                bools.add("true"); bools.add("false"); bools.add("random");
                return filter(bools, args[2]);
            }
        }
        return Collections.emptyList();
    }
}
