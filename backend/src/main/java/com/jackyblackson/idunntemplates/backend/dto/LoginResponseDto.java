package com.jackyblackson.idunntemplates.backend.dto;

import java.util.UUID;

public record LoginResponseDto(
        String username,
        String uuid,
        String JWT,
        long expiresTime
) {
    public static LoginResponseDto fromUserContext(
            UserContext context,
            String JWT,
            long expiresTime
    ) {
        return new LoginResponseDto(
                context.getUsername(),
                context.getUuid(),
                JWT,
                expiresTime
        );
    }
}
