package com.jackyblackson.idunntemplates.backend.service;

import com.jackyblackson.idunntemplates.backend.dto.LuckyUserInfo;
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

@Service
public class LuckyPermUserInfoService {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public LuckyPermUserInfoService(RestTemplate restTemplate,
                                    @Value("${app.service.idunn-user-info-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * 批量查询玩家信息 (POST)
     * * @param uuids 玩家 UUID 字符串列表
     * @return 玩家信息列表。顺序与输入一致，查询不到的项为 null。
     */
    public List<LuckyUserInfo> getBulkUserInfo(List<String> uuids) {
        if (uuids == null || uuids.isEmpty()) {
            return Collections.emptyList();
        }

        // 构建 URL (该接口不需要 Query Params，参数在 Body)
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl).toUriString();

        // 构建 Header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 将 List<String> 作为 Body 发送
        HttpEntity<List<String>> requestEntity = new HttpEntity<>(uuids, headers);

        try {
            // 发送 POST 请求
            // 响应结构示例: [ {"name": "Jacky", "uuid": "..."}, null ]
            ResponseEntity<List<LuckyUserInfo>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<List<LuckyUserInfo>>() {}
            );

            return response.getBody() != null ? response.getBody() : Collections.emptyList();

        } catch (RestClientException e) {
            System.err.println("Bulk user info lookup failed: " + e.getMessage());
            // 出错时返回与输入等长的 null 列表，或者空列表，视业务容错而定
            return Collections.emptyList();
        }
    }

    /**
     * 查询单个玩家信息
     * 基于批量接口的封装
     */
    public LuckyUserInfo getUserInfo(String uuid) {
        List<LuckyUserInfo> results = getBulkUserInfo(Collections.singletonList(uuid));
        if (results != null && !results.isEmpty()) {
            return results.get(0);
        }
        return null;
    }
}