package com.jackyblackson.idunntemplates.backend.service;

import com.jackyblackson.idunntemplates.backend.dto.UserContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class YggdrasilService {

    @Value("${yggdrasil.api.url}")
    private String yggdrasilUrl;

    private final RestTemplate restTemplate;

    public YggdrasilService() {
        this.restTemplate = new RestTemplate();
    }

    public UserContext authenticate(String username, String password) {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> agent = new HashMap<>();
        agent.put("name", "Minecraft");
        agent.put("version", 1);

        payload.put("agent", agent);
        payload.put("username", username);
        payload.put("password", password);
        // requesting user: true to get the userid if needed, but standard auth returns profile
        payload.put("requestUser", true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(yggdrasilUrl, request, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                Map<String, Object> selectedProfile = (Map<String, Object>) body.get("selectedProfile");

                if (selectedProfile != null) {
                    String name = (String) selectedProfile.get("name");
                    String id = (String) selectedProfile.get("id");
                    return new UserContext(name, id);
                }
            }
        } catch (Exception e) {
            // Log error or handle specific exceptions
            e.printStackTrace();
        }
        return null;
    }
}
