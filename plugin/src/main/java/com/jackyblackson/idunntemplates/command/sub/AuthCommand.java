package com.jackyblackson.idunntemplates.command.sub;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.command.IdunnSubCommand;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthCommand implements IdunnSubCommand {

    private final HttpClient httpClient;
    private final Gson gson;

    public AuthCommand() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.gson = new Gson();
    }

    @Override
    public void execute(Player player, String[] args) {
        String baseUrl = IdunnTemplates.getInstance().getConfig().getString("services.backend.api-url", "http://localhost:8080/api/v1");
        if (baseUrl != null && baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        String serverToken = IdunnTemplates.getInstance().getConfig().getString("services.backend.server-token", "");

        String urlString = baseUrl.replace("/api/v1", "/api/auth/link"); // Assuming api-url points to /api/v1

        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("username", player.getName());
        bodyMap.put("uuid", player.getUniqueId().toString());

        String jsonBody = gson.toJson(bodyMap);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .timeout(Duration.ofSeconds(3))
                .header("Content-Type", "application/json")
                .header("Authorization", serverToken)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        player.sendMessage("§a正在生成认证链接...");

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                        boolean registered = json.get("registered").getAsBoolean();
                        String link = json.get("link").getAsString();

                        System.out.println(response.body());

                        String frontendUrl = IdunnTemplates.getInstance().getConfig().getString("services.frontend.url");
                        if (frontendUrl == null || frontendUrl.isBlank()) {
                            frontendUrl = IdunnTemplates.getInstance().getConfig().getString("services.thumbnail-generation.renderer-url", "http://localhost:5173/");
                        }
                        if (frontendUrl.endsWith("/")) {
                            frontendUrl = frontendUrl.substring(0, frontendUrl.length() - 1);
                        }
                        String fullLink = frontendUrl + link;

                        Bukkit.getScheduler().runTask(IdunnTemplates.getInstance(), () -> {
                            if (!player.isOnline()) {
                                return;
                            }

                            if (registered) {
                                player.sendMessage("§a检测到你已完成注册，请点击下方文字登录：");
                                player.spigot().sendMessage(createOpenUrlComponent("§b§n[点击登录]", fullLink, "点击打开登录页面"));
                            } else {
                                player.sendMessage("§a请点击下方文字完成注册：");
                                player.spigot().sendMessage(createOpenUrlComponent("§b§n[点击注册]", fullLink, "点击打开注册页面"));
                            }
                        });
                    } else {
                        Bukkit.getScheduler().runTask(IdunnTemplates.getInstance(), () -> {
                            if (player.isOnline()) {
                                player.sendMessage("§c向后端请求认证链接失败，状态码: " + response.statusCode());
                            }
                        });
                        IdunnTemplates.getInstance().getLogger().warning("Auth link request failed: " + response.body());
                    }
                })
                .exceptionally(ex -> {
                    Bukkit.getScheduler().runTask(IdunnTemplates.getInstance(), () -> {
                        if (player.isOnline()) {
                            player.sendMessage("§c连接后端服务失败，无法生成认证链接。");
                        }
                    });
                    IdunnTemplates.getInstance().getLogger().warning("HTTP request failed: " + ex.getMessage());
                    return null;
                });
    }

    private TextComponent createOpenUrlComponent(String label, String url, String hoverText) {
        TextComponent component = new TextComponent(label);
        component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
        return component;
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return List.of();
    }
}
