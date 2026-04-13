cat << 'INNER_EOF' > patch.diff
--- frontend/app/pages/login/login-page.content.ts
+++ frontend/app/pages/login/login-page.content.ts
@@ -10,17 +10,17 @@
       [Locales.CHINESE_TRADITIONAL]: "登入 Idunn 模板",
     }),
     description: t({
-      [Locales.ENGLISH]: "Please enter your username and password in ",
-      [Locales.CHINESE]: "请输入您在 ",
-      [Locales.CHINESE_TRADITIONAL]: "請輸入您在 ",
+      [Locales.ENGLISH]: "Please enter your username and password",
+      [Locales.CHINESE]: "请输入您的",
+      [Locales.CHINESE_TRADITIONAL]: "請輸入您的",
     }),
     skinServerName: t({
-      [Locales.ENGLISH]: "Taixue Skin",
-      [Locales.CHINESE]: "太学皮肤站",
-      [Locales.CHINESE_TRADITIONAL]: "太學皮膚站",
+      [Locales.ENGLISH]: "",
+      [Locales.CHINESE]: "",
+      [Locales.CHINESE_TRADITIONAL]: "",
     }),
     description2: t({
-      [Locales.ENGLISH]: "to access protected template data.",
+      [Locales.ENGLISH]: " to access protected template data.",
       [Locales.CHINESE]: "的账号密码以访问受保护的模板数据。",
       [Locales.CHINESE_TRADITIONAL]: "的帳號密碼以訪問受保護的模板數據。",
     }),
@@ -66,9 +66,9 @@
     // 底部文字
     footerText: t({
       [Locales.ENGLISH]:
-        "Your username and password are the same as those you use to log in to Taixue Skin.",
-      [Locales.CHINESE]: "账号和密码就是你登录皮肤站的账号密码",
-      [Locales.CHINESE_TRADITIONAL]: "帳號和密碼就是你登入皮膚站的帳號密碼",
+        "You can generate a login link using /idunn auth in game.",
+      [Locales.CHINESE]: "你可以通过游戏内的 /idunn auth 命令获取登录链接",
+      [Locales.CHINESE_TRADITIONAL]: "你可以透過遊戲內的 /idunn auth 命令獲取登入連結",
     }),
   },
 } satisfies Dictionary;
INNER_EOF
patch frontend/app/pages/login/login-page.content.ts < patch.diff
