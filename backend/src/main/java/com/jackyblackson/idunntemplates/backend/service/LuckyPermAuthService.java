package com.jackyblackson.idunntemplates.backend.service;

import com.jackyblackson.idunntemplates.core.utils.NullGettable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LuckyPermAuthService {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    // 构造注入 RestTemplate 和 配置项
    public LuckyPermAuthService(RestTemplate restTemplate,
                                @Value("${app.service.idunn-auth-base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * 单个权限检查 (GET)
     *
     * @param uuid 玩家 UUID
     * @param username 玩家用户名 (可选，用于离线查找)
     * @param permission 需要检查的权限节点
     * @return 是否拥有权限，如果请求失败默认返回 false
     */
    public boolean checkPermission(String uuid, String username, String permission) {
        // 构建 URL: /?uuid=...&username=...&permission=...
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("uuid", uuid)
                .queryParam("username", username != null ? username : "")
                .queryParam("permission", permission)
                .toUriString();

        try {
            // 发送 GET 请求
            // 响应结构: { "result": true, "source": "...", ... }
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("result")) {
                Object result = body.get("result");
                return result instanceof Boolean && (Boolean) result;
            }
        } catch (RestClientException e) {
            System.err.println("Permission check failed: " + e.getMessage());
            // 根据业务需求，这里可以选择抛出异常或者返回 false (Fail-Closed)
        }
        return false;
    }

    /**
     * 批量权限检查 (POST)
     *
     * @param uuid 玩家 UUID
     * @param username 玩家用户名 (可选)
     * @param permissions 权限节点列表
     * @return 权限结果 Map，Key为权限节点，Value为是否拥有。如果出错返回空 Map。
     */
    public Map<String, Boolean> batchCheckPermissions(String uuid, String username, List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyMap();
        }

        // 构建 URL: /?uuid=...&username=...
        // 注意：POST 请求的 permission 参数不在 URL 里，而在 Body 里
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("uuid", uuid)
                .queryParam("username", username != null ? username : "")
                .toUriString();

        // 构建 Header 和 Body
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<String>> requestEntity = new HttpEntity<>(permissions, headers);

        try {
            // 发送 POST 请求
            // 响应结构: { "results": { "perm.A": true, "perm.B": false }, "source": "..." }
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("results")) {
                // 安全地转换结果 Map
                Object resultsObj = body.get("results");
                if (resultsObj instanceof Map) {
                    return (Map<String, Boolean>) resultsObj;
                }
            }
        } catch (RestClientException e) {
            System.err.println("Batch permission check failed: " + e.getMessage());
        }
        return Collections.emptyMap();
    }

    /**
     * 泛型列表过滤：批量鉴权并返回有权限的子集 (默认行为：返回新列表，不修改原列表，过滤掉无权限项)
     *
     * @param uuid             玩家 UUID
     * @param username         玩家用户名
     * @param sourceList       原始数据列表
     * @param permissionMapper 从对象 T 中提取权限字符串的映射函数
     * @param <T>              列表项的类型
     * @return 一个新的列表，包含鉴权通过的项
     */
    public <T> List<T> filterList(String uuid, String username, List<T> sourceList, Function<T, String> permissionMapper) {
        // 调用全参数方法，默认 inPlace=false, replaceByNull=false
        return filterList(uuid, username, sourceList, permissionMapper, false);
    }

    /**
     * 泛型列表过滤：批量鉴权并构建新列表
     *
     * @param uuid             玩家 UUID
     * @param username         玩家用户名
     * @param sourceList       原始数据列表 (不会被修改)
     * @param permissionMapper 从对象 T 中提取权限字符串的映射函数
     * @param replaceByNull    是否用 null (或 NullObject) 替换无权限的项。
     * true: 保持列表大小不变，无权限项替换为 NullInstance；
     * false: 仅返回有权限的项，列表大小可能变小。
     * @param <T>              列表项的类型
     * @return 过滤后的新列表
     */
    public <T> List<T> filterList(String uuid, String username, List<T> sourceList, Function<T, String> permissionMapper, boolean replaceByNull) {
        if (sourceList == null) {
            return Collections.emptyList();
        }
        if (sourceList.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 提取所有需要检查的权限节点
        // 使用 Set 去重，减少 API 调用开销
        Set<String> permissionsToCheck = sourceList.stream()
                .filter(Objects::nonNull) // 防止列表里本身就有 null 导致 mapper 报错
                .map(permissionMapper)
                .filter(perm -> perm != null && !perm.isEmpty())
                .collect(Collectors.toSet());

        // 2. 批量鉴权
        Map<String, Boolean> authResults;
        if (permissionsToCheck.isEmpty()) {
            authResults = Collections.emptyMap();
        } else {
            // 调用批量接口 (假设 batchCheckPermissions 接受 List)
            authResults = batchCheckPermissions(uuid, username, new ArrayList<>(permissionsToCheck));
        }

        // 3. 构建新列表
        // 预设容量为原列表大小，避免扩容开销
        List<T> resultList = new ArrayList<>(sourceList.size());

        for (T item : sourceList) {
            boolean allowed = checkItemAuth(item, permissionMapper, authResults);

            if (allowed) {
                // 鉴权通过：直接添加
                resultList.add(item);
            } else if (replaceByNull) {
                // 鉴权失败且需要占位：获取空对象或 null 添加进去
                // 这样能保证 resultList 的 size 和 sourceList 一致
//                T nullObject = getNullObject(item);
//                resultList.add(nullObject);
                resultList.add(null);
            }
            // replaceByNull=false 时：直接跳过，不添加到结果列表中
        }

        return resultList;
    }

    /**
     * 辅助方法：获取对象的 Null 替代品
     * 如果对象实现了 NullGettable，调用其方法；否则返回 null
     */
    @SuppressWarnings("unchecked")
    private <T> T getNullObject(T item) {
        if (item instanceof NullGettable) {
            try {
                return ((NullGettable<T>) item).getNullInstance();
            } catch (Exception e) {
                // 防止业务代码实现 getNullInstance 时出错导致整个流程崩溃
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    /**
     * 辅助方法：检查单个对象的权限
     */
    private <T> boolean checkItemAuth(T item, Function<T, String> permissionMapper, Map<String, Boolean> authResults) {
        if (item == null) return false; // 如果列表里本身就有 null，视为无权限（或者保留原样？通常 null 不需要鉴权，视业务而定，这里默认 fail）

        String perm = permissionMapper.apply(item);

        // 1. mapper 返回 null 或 空字符串 -> 视为无权限 直接返回 true
        if (perm == null || perm.isEmpty()) {
            return true;
        }

        // 2. 权限结果 Map 中包含该 key 且 value 为 true -> 通过
        return authResults.getOrDefault(perm, false);
    }
}