package com.jackyblackson.idunntemplates.command.sub.project;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.api.BackendApiClient;
import com.jackyblackson.idunntemplates.manager.ProjectCatalogManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RefreshProjectCommand extends BaseSubCommand {

    private final ProjectCatalogManager projectCatalogManager;

    public RefreshProjectCommand(ProjectCatalogManager projectCatalogManager) {
        this.projectCatalogManager = projectCatalogManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 2) {
            sendUsage(player);
            return;
        }

        String identifier = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)).trim();
        player.sendMessage(ChatColor.GRAY + "Looking up project " + ChatColor.WHITE + identifier + ChatColor.GRAY + "...");

        projectCatalogManager.ensureFreshCatalog().thenAccept(projects ->
                IdunnTemplates.getInstance().getServer().getScheduler().runTask(IdunnTemplates.getInstance(), () -> {
                    if (!player.isOnline()) {
                        return;
                    }

                    Optional<BackendApiClient.ProjectDetails> exact = projectCatalogManager.findExactProject(identifier);
                    if (exact.isPresent()) {
                        triggerRefresh(player, exact.get());
                        return;
                    }

                    List<BackendApiClient.ProjectDetails> candidates = projectCatalogManager.searchProjects(identifier, 5);
                    if (candidates.isEmpty()) {
                        player.sendMessage(ChatColor.RED + "No matching project was found for: " + identifier);
                        sendUsage(player);
                        return;
                    }
                    if (candidates.size() == 1) {
                        triggerRefresh(player, candidates.getFirst());
                        return;
                    }

                    player.sendMessage(ChatColor.YELLOW + "Found multiple matching projects. Please refine your input or use one of these IDs:");
                    for (BackendApiClient.ProjectDetails project : candidates) {
                        player.sendMessage(ChatColor.GRAY + " - "
                                + ChatColor.AQUA + projectCatalogManager.formatProjectLabel(project)
                                + ChatColor.DARK_GRAY + " | "
                                + ChatColor.WHITE + firstNonBlank(project.pathName, project.worldName, project.kind, ""));
                    }
                })
        );
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 2) {
            return projectCatalogManager.searchProjects(args[1], 10).stream()
                    .map(projectCatalogManager::formatProjectLabel)
                    .toList();
        }
        return Collections.emptyList();
    }

    private void triggerRefresh(Player player, BackendApiClient.ProjectDetails project) {
        IdunnTemplates.getInstance().getProjectSettlementSyncManager().refreshProject(project.id);
        player.sendMessage(ChatColor.YELLOW + "Started settlement snapshot refresh for "
                + ChatColor.AQUA + firstNonBlank(project.displayName, project.name, project.pathName, "#" + project.id)
                + ChatColor.GRAY + " (#" + project.id + ")");
    }

    private void sendUsage(Player player) {
        player.sendMessage(ChatColor.RED + "Usage: /idunn project refresh <projectId|name|displayName|path>");
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
