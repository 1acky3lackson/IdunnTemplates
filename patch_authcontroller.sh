cat << 'INNER_EOF' > patch.diff
--- backend/src/main/java/com/jackyblackson/idunntemplates/backend/controller/AuthController.java
+++ backend/src/main/java/com/jackyblackson/idunntemplates/backend/controller/AuthController.java
@@ -4,6 +4,7 @@
 import com.jackyblackson.idunntemplates.backend.dto.LoginRequest;
 import com.jackyblackson.idunntemplates.backend.dto.LoginResponseDto;
 import com.jackyblackson.idunntemplates.backend.dto.UserContext;
+import com.jackyblackson.idunntemplates.backend.service.LocalAuthService;
 import com.jackyblackson.idunntemplates.backend.service.YggdrasilService;
 import com.jackyblackson.idunntemplates.backend.util.JwtUtil;
 import jakarta.servlet.http.Cookie;
@@ -14,6 +15,10 @@
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.bind.annotation.*;

+import java.util.HashMap;
+import java.util.Map;
+import java.util.Optional;
+
 @RestController
 @RequestMapping("/api/auth")
 public class AuthController {
@@ -21,18 +26,20 @@

     private final YggdrasilService yggdrasilService;
     private final JwtUtil jwtUtil;
+    private final LocalAuthService localAuthService;

     @Value("${jwt.expiration}")
     private long jwtExpiration;

     @Autowired
-    public AuthController(YggdrasilService yggdrasilService, JwtUtil jwtUtil) {
+    public AuthController(YggdrasilService yggdrasilService, JwtUtil jwtUtil, LocalAuthService localAuthService) {
         this.yggdrasilService = yggdrasilService;
         this.jwtUtil = jwtUtil;
+        this.localAuthService = localAuthService;
     }

     @PostMapping("/login")
     public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
-        UserContext user = yggdrasilService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());
+        UserContext user = localAuthService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());
         if (user != null) {
             String token = jwtUtil.generateToken(user);
             Cookie cookie = new Cookie("auth_token", token);
@@ -50,6 +57,59 @@
         }
     }

+    @PostMapping("/link")
+    @AuthRequired(allowServerToken = true)
+    public ResponseEntity<?> generateAuthLink(@RequestBody UserContext userContext) {
+        boolean registered = localAuthService.isRegistered(userContext.getUsername());
+        Map<String, Object> response = new HashMap<>();
+
+        if (registered) {
+            String token = jwtUtil.generateLoginLinkToken(userContext);
+            response.put("registered", true);
+            // Front end route for login link
+            response.put("link", "/login?token=" + token);
+        } else {
+            String token = jwtUtil.generateRegisterToken(userContext);
+            response.put("registered", false);
+            // Front end route for register link
+            response.put("link", "/register?token=" + token);
+        }
+
+        return ResponseEntity.ok(response);
+    }
+
+    @PostMapping("/register")
+    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
+        String token = request.get("token");
+        String password = request.get("password");
+
+        if (token == null || password == null) {
+            return ResponseEntity.badRequest().body("Token and password are required");
+        }
+
+        if (!jwtUtil.validateToken(token)) {
+            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
+        }
+
+        String type = jwtUtil.extractClaim(token, claims -> claims.get("type", String.class));
+        if (!"register".equals(type)) {
+            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token type");
+        }
+
+        String username = jwtUtil.extractUsername(token);
+        String uuid = jwtUtil.extractUuid(token);
+
+        if (localAuthService.isRegistered(username)) {
+            return ResponseEntity.badRequest().body("User already registered");
+        }
+
+        localAuthService.registerUser(uuid, username, password);
+
+        return ResponseEntity.ok("Successfully registered");
+    }
+
     @GetMapping("/me")
     @AuthRequired
     public ResponseEntity<UserContext> getCurrentUser(UserContext user) {
INNER_EOF
git checkout backend/src/main/java/com/jackyblackson/idunntemplates/backend/controller/AuthController.java
patch backend/src/main/java/com/jackyblackson/idunntemplates/backend/controller/AuthController.java < patch.diff
