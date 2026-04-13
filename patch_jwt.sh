cat << 'INNER_EOF' > patch.diff
--- backend/src/main/java/com/jackyblackson/idunntemplates/backend/util/JwtUtil.java
+++ backend/src/main/java/com/jackyblackson/idunntemplates/backend/util/JwtUtil.java
@@ -31,16 +31,32 @@
     public String generateToken(UserContext userContext) {
         Map<String, Object> claims = new HashMap<>();
         claims.put("uuid", userContext.getUuid());
-        return createToken(claims, userContext.getUsername());
+        return createToken(claims, userContext.getUsername(), jwtExpiration);
     }

-    private String createToken(Map<String, Object> claims, String subject) {
+    public String generateRegisterToken(UserContext userContext) {
+        Map<String, Object> claims = new HashMap<>();
+        claims.put("uuid", userContext.getUuid());
+        claims.put("type", "register");
+        // 15 minutes = 15 * 60 * 1000 = 900000 ms
+        return createToken(claims, userContext.getUsername(), 900000);
+    }
+
+    public String generateLoginLinkToken(UserContext userContext) {
+        Map<String, Object> claims = new HashMap<>();
+        claims.put("uuid", userContext.getUuid());
+        claims.put("type", "login_link");
+        // 5 minutes = 5 * 60 * 1000 = 300000 ms
+        return createToken(claims, userContext.getUsername(), 300000);
+    }
+
+    private String createToken(Map<String, Object> claims, String subject, long expirationTime) {
         return Jwts.builder()
                 .setClaims(claims)
                 .setSubject(subject)
                 .setIssuedAt(new Date(System.currentTimeMillis()))
-                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
+                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                 .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                 .compact();
     }

+    private String createToken(Map<String, Object> claims, String subject) {
+        return createToken(claims, subject, jwtExpiration);
+    }
INNER_EOF
patch backend/src/main/java/com/jackyblackson/idunntemplates/backend/util/JwtUtil.java < patch.diff
