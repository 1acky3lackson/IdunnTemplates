package com.jackyblackson.idunntemplates.command;

import org.bukkit.entity.Player;
import java.util.List;

public interface IdunnSubCommand {
    void execute(Player player, String[] args);
    List<String> tabComplete(Player player, String[] args);
}
