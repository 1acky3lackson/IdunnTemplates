package com.jackyblackson.idunntemplates.command.sub;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.util.MessageUtil;
import com.jackyblackson.idunntemplates.command.IdunnSubCommand;
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

        player.sendMessage("§aRequesting authentication link...");

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                        boolean registered = json.get("registered").getAsBoolean();
                        String link = json.get("link").getAsString();

                        String frontendUrl = IdunnTemplates.getInstance().getConfig().getString("services.thumbnail-generation.renderer-url", "http://localhost:3000");
                        String fullLink = frontendUrl + link;

                        if (registered) {
                            player.sendMessage("§cYou are already registered! §aLogin link:");
                            player.sendMessage("§b" + fullLink);
                        } else {
                            player.sendMessage("§aRegistration link:");
                            player.sendMessage("§b" + fullLink);
                        }
                    } else {
                        player.sendMessage("§cFailed to request link from backend. Error: " + response.statusCode());
                        IdunnTemplates.getInstance().getLogger().warning("Auth link request failed: " + response.body());
                    }
                })
                .exceptionally(ex -> {
                    player.sendMessage("§cFailed to connect to backend server.");
                    IdunnTemplates.getInstance().getLogger().warning("HTTP request failed: " + ex.getMessage());
                    return null;
                });
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return List.of();
    }
}
