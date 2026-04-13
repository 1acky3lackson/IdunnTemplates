cat << 'INNER_EOF' > patch.diff
--- backend/src/main/java/com/jackyblackson/idunntemplates/backend/util/JwtUtil.java
+++ backend/src/main/java/com/jackyblackson/idunntemplates/backend/util/JwtUtil.java
@@ -109,7 +109,7 @@
         claims.put("angle", angle);
         // Set a short expiration for this specific token if needed, but createToken uses default expiration
         // We can override expiration if we want, but using default is fine as per prompt "short-term" (config dependent)
-        return createToken(claims, "thumbnail_upload");
+        return createToken(claims, "thumbnail_upload", jwtExpiration);
     }

     public Map<String, Object> extractThumbnailClaims(String token) {
INNER_EOF
patch backend/src/main/java/com/jackyblackson/idunntemplates/backend/util/JwtUtil.java < patch.diff
