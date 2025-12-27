package com.jackyblackson.idunntemplates.command.sub.sets;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.set.TemplateSet;
import com.jackyblackson.idunntemplates.core.domain.PlayerPreference;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class SetsSaveCommand extends BaseSubCommand {

    private final SessionManager sessionManager;

    public SetsSaveCommand(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn set save <name>
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /idunn set save <name>");
            return;
        }
        
        String name = args[1];
        PlayerPreference pref = sessionManager.getSession(player.getUniqueId()).getPreference();
        
        // Deep copy needed? Using GSON to clone for simplicity/laziness or manual copy.
        // Actually, let's just create a new TemplateSet from current.
        // Or serialize/deserialize.
        // Manual copy is cleaner.
        TemplateSet current = pref.getCurrentSet();
        TemplateSet saved = new TemplateSet();
        saved.setRotate(current.getRotate());
        saved.setFlipX(current.getFlipX());
        saved.setFlipZ(current.getFlipZ());
        for (var src : current.getSources()) {
            saved.addSource(src.getPath(), src.getWeight());
        }
        
        pref.getSavedSets().put(name, saved);
        sessionManager.saveSession(player.getUniqueId());
        
        player.sendMessage(ChatColor.GREEN + "Saved current set as preset: " + name);
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return Collections.emptyList();
    }
}
