package com.jackyblackson.idunntemplates.backend.dto;

import com.jackyblackson.idunntemplates.backend.domain.TrustedServer;
import lombok.Data;

@Data
public class TrustedServerDto {
    private Long id;
    private String name;
    private String remarks;
    private String token;
    private Long createdAt;
    private String createdByUsername;

    public static TrustedServerDto fromEntity(TrustedServer server) {
        TrustedServerDto dto = new TrustedServerDto();
        dto.setId(server.getId());
        dto.setName(server.getName());
        dto.setRemarks(server.getRemarks());
        dto.setToken(server.getToken());
        dto.setCreatedAt(server.getCreatedAt());
        dto.setCreatedByUsername(server.getCreatedByUsername());
        return dto;
    }
}
