package com.jackyblackson.idunntemplates.manager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
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
            String method = exchange.getRequestMethod();
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);

            String uuidStr = params.get("uuid");
            String userName = params.get("username");

            // 基础参数校验
            if (uuidStr == null) {
                sendJsonError(exchange, 400, "Missing uuid parameter");
                return;
            }

            UUID uuid;
            try {
                uuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException e) {
                sendJsonError(exchange, 400, "Invalid UUID format");
                return;
            }

            // --- 处理请求逻辑 ---
            if ("GET".equals(method)) {
                handleGet(exchange, uuid, userName, params.get("permission"));
            } else if ("POST".equals(method)) {
                handlePost(exchange, uuid, userName);
            } else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        }

        // 处理单次鉴权 (GET)
        private void handleGet(HttpExchange exchange, UUID uuid, String userName, String permission) throws IOException {
            if (permission == null) {
                sendJsonError(exchange, 400, "Missing permission parameter");
                return;
            }

            // 将单个权限包装成列表进行统一处理
            Map<String, Object> fullResult = performPermissionChecks(uuid, userName, Collections.singletonList(permission));

            // 为了保持向后兼容性，GET 请求返回扁平结构 (result: boolean)
            // performPermissionChecks 返回的是 { results: Map<String, Boolean>, ... }
            // 我们需要将其解包
            Map<String, Boolean> resultsMap = (Map<String, Boolean>) fullResult.get("results");

            // 移除 results map，将单个结果放入根对象
            fullResult.remove("results");
            fullResult.put("result", resultsMap.get(permission));

            sendResponse(exchange, 200, gson.toJson(fullResult));
        }

        // 处理批量鉴权 (POST)
        private void handlePost(HttpExchange exchange, UUID uuid, String userName) throws IOException {
            List<String> permissions;
            try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
                Type listType = new TypeToken<List<String>>(){}.getType();
                permissions = gson.fromJson(reader, listType);
            } catch (Exception e) {
                sendJsonError(exchange, 400, "Invalid JSON body");
                return;
            }

            if (permissions == null || permissions.isEmpty()) {
                sendJsonError(exchange, 400, "Permission list is empty or null");
                return;
            }

            // 执行批量检查
            Map<String, Object> result = performPermissionChecks(uuid, userName, permissions);
            sendResponse(exchange, 200, gson.toJson(result));
        }

        /**
         * 核心鉴权逻辑：支持在线玩家和离线 LuckPerms 检查，支持批量列表
         */
        private Map<String, Object> performPermissionChecks(UUID uuid, String userName, List<String> permissions) {
            // 1. 尝试在线检查 (主线程)
            CompletableFuture<Map<String, Object>> onlineCheckFuture = new CompletableFuture<>();

            Bukkit.getScheduler().runTask(plugin, () -> {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    Map<String, Object> res = new HashMap<>();
                    Map<String, Boolean> permResults = new HashMap<>();

                    // 批量检查在线玩家权限
                    for (String perm : permissions) {
                        permResults.put(perm, player.hasPermission(perm));
                    }

                    res.put("results", permResults); // POST 模式下的结果集
                    res.put("source", "online_player");
                    res.put("name", player.getName());
                    res.put("uuid", player.getUniqueId());
                    onlineCheckFuture.complete(res);
                } else {
                    onlineCheckFuture.complete(null); // 玩家不在线
                }
            });

            Map<String, Object> result;
            try {
                result = onlineCheckFuture.join();
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error waiting for main thread check", e);
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Internal server error during online check");
                return error;
            }

            // 如果在线检查成功，直接返回
            if (result != null) {
                return result;
            }

            // 2. 玩家离线，尝试 LuckPerms 检查
            result = new HashMap<>();
            if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
                try {
                    // 尝试通过 UUID 加载
                    User user = LuckPermsProvider.get().getUserManager().loadUser(uuid).join();

                    // 如果 UUID 没找到，尝试通过用户名查找 UUID 再加载
                    if (user == null && userName != null && !userName.isEmpty()) {
                        UUID userNameUUID = LuckPermsProvider.get().getUserManager().lookupUniqueId(userName).join();
                        if (userNameUUID != null) {
                            user = LuckPermsProvider.get().getUserManager().loadUser(userNameUUID).join();
                        }
                    }

                    if (user != null) {
                        Map<String, Boolean> permResults = new HashMap<>();
                        // 批量检查 LuckPerms 权限
                        for (String perm : permissions) {
                            boolean hasPerm = user.getCachedData().getPermissionData().checkPermission(perm).asBoolean();
                            permResults.put(perm, hasPerm);
                        }

                        result.put("results", permResults);
                        result.put("source", "luckperms");
                        result.put("name", user.getUsername());
                        result.put("uuid", user.getUniqueId());
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

            return result;
        }

        private void sendJsonError(HttpExchange exchange, int statusCode, String message) throws IOException {
            Map<String, Object> error = new HashMap<>();
            error.put("error", message);
            sendResponse(exchange, statusCode, gson.toJson(error));
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