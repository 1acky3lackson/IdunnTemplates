cat << 'INNER_EOF' > patch.diff
--- frontend/app/routes.ts
+++ frontend/app/routes.ts
@@ -8,6 +8,8 @@
     // ---------------------------------------------------------
     // 这里的 /:lang? 确保登录页也支持类似 /zh-CN/login 的 URL
     route("/:lang?/login", "pages/login/login-page.tsx"),
+    // 注册页
+    route("/:lang?/register", "pages/register/register-page.tsx"),

     // ---------------------------------------------------------
     // B. 需要 Topbar 的页面 (嵌套在 Topbar Layout 中)
INNER_EOF
patch frontend/app/routes.ts < patch.diff
