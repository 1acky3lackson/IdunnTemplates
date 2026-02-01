package com.jackyblackson.idunntemplates.backend.util;

import com.jackyblackson.idunntemplates.backend.dto.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "thisIsASecretKeyThatIsLongEnoughForHmacSha256TestingPurposes");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 3600000L); // 1 hour
    }

    @Test
    void generateAndValidateToken() {
        UserContext user = new UserContext("testUser", "test-uuid");
        String token = jwtUtil.generateToken(user);

        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token, user));
        assertEquals("testUser", jwtUtil.extractUsername(token));
        assertEquals("test-uuid", jwtUtil.extractUuid(token));
    }

    @Test
    void validateToken_InvalidUser() {
        UserContext user = new UserContext("testUser", "test-uuid");
        String token = jwtUtil.generateToken(user);

        UserContext otherUser = new UserContext("otherUser", "other-uuid");
        assertFalse(jwtUtil.validateToken(token, otherUser));
    }
}
