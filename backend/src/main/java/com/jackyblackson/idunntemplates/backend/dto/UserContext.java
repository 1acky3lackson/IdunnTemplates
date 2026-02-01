package com.jackyblackson.idunntemplates.backend.dto;

import java.util.UUID;

public class UserContext {
    private String username;
    private UUID uuid;

    public UserContext() {
    }

    public UserContext(String username, String uuid) {
        this.username = username;
        this.uuid = parseTrimmedUuid(uuid);
    }

    public String getUsername() {
        return username;
    }

//    public void setUsername(String username) {
//        this.username = username;
//    }

    public String getUuid() {
        return uuid.toString();
    }

//    public void setUuid(String uuid) {
//        this.uuid = parseTrimmedUuid(uuid);
//    }

    /**
     * 将 32 位无中划线的十六进制字符串转换为 UUID 对象
     * @param trimmedUuid 32位字符串 (例如: "500d59d184a83207932ce62f8a543ef7")
     * @return UUID 对象
     */
    public static UUID parseTrimmedUuid(String trimmedUuid) {
        if (trimmedUuid == null) {
            throw new IllegalArgumentException("UUID 为空（null）！");
        }
        if (trimmedUuid.length() != 32) {
            try {
                return UUID.fromString(trimmedUuid);
            } catch (Exception e) {
                throw new IllegalArgumentException("无效的 UUID 字符串，长度必须为 32 位");
            }
        }

        // 解析前 16 位作为高位 (Most Significant Bits)
        long mostSigBits = Long.parseUnsignedLong(trimmedUuid.substring(0, 16), 16);
        // 解析后 16 位作为低位 (Least Significant Bits)
        long leastSigBits = Long.parseUnsignedLong(trimmedUuid.substring(16), 16);

        return new UUID(mostSigBits, leastSigBits);
    }
}
