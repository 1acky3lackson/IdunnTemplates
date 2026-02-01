package com.jackyblackson.idunntemplates.manager;

import com.google.gson.Gson;
import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class PermissionServerManager {

    private final IdunnTemplates plugin;
    private HttpServer server;
    private final Gson gson = new Gson();

    public PermissionServerManager(IdunnTemplates plugin) {
        this.plugin = plugin;
    }

    public void start() {
        int port = plugin.getConfig().getInt("server.port", 8085);
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new PermissionHandler());
            server.setExecutor(null); // Creates a default executor
            server.start();
            plugin.getLogger().info("Permission verification server started on port " + port);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to start permission server on port " + port, e);
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            plugin.getLogger().info("Permission server stopped.");
        }
    }

    private class PermissionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);

            String uuidStr = params.get("uuid");
            String permission = params.get("permission");

            if (uuidStr == null || permission == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Missing uuid or permission parameter");
                sendResponse(exchange, 400, gson.toJson(error));
                return;
            }

            UUID uuid;
            try {
                uuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException e) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Invalid UUID format");
                sendResponse(exchange, 400, gson.toJson(error));
                return;
            }

            // Check if player is online on the main thread
            CompletableFuture<Map<String, Object>> onlineCheckFuture = new CompletableFuture<>();
            Bukkit.getScheduler().runTask(plugin, () -> {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    Map<String, Object> res = new HashMap<>();
                    boolean hasPerm = player.hasPermission(permission);
                    res.put("result", hasPerm);
                    res.put("source", "online_player");
                    res.put("name", player.getName());
                    onlineCheckFuture.complete(res);
                } else {
                    onlineCheckFuture.complete(null);
                }
            });

            Map<String, Object> result;
            try {
                result = onlineCheckFuture.join();
            } catch (Exception e) {
                 plugin.getLogger().log(Level.WARNING, "Error waiting for main thread check", e);
                 Map<String, Object> error = new HashMap<>();
                 error.put("error", "Internal server error during online check");
                 sendResponse(exchange, 500, gson.toJson(error));
                 return;
            }

            if (result == null) {
                // Player is offline, check LuckPerms
                result = new HashMap<>();
                if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
                    try {
                        User user = LuckPermsProvider.get().getUserManager().loadUser(uuid).join();
                        if (user != null) {
                            boolean hasPerm = user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
                            result.put("result", hasPerm);
                            result.put("source", "luckperms");
                            result.put("name", user.getUsername());
                        } else {
                            result.put("error", "User not found in LuckPerms");
                            result.put("source", "luckperms_error");
                        }
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "Error checking LuckPerms", e);
                        result.put("error", "Error checking LuckPerms: " + e.getMessage());
                        result.put("source", "luckperms_exception");
                    }
                } else {
                    result.put("error", "Player offline and LuckPerms not found");
                    result.put("source", "offline_no_luckperms");
                }
            }

            sendResponse(exchange, 200, gson.toJson(result));
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) {
            return result;
        }
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }
}
