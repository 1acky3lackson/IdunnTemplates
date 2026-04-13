cat << 'INNER_EOF' > patch.diff
--- frontend/app/pages/login/login-page.tsx
+++ frontend/app/pages/login/login-page.tsx
@@ -1,4 +1,4 @@
-import { useState } from "react";
+import { useState, useEffect } from "react";
 import { useForm } from "react-hook-form";
 import { zodResolver } from "@hookform/resolvers/zod";
 import * as z from "zod";
@@ -32,6 +32,7 @@
 import MeshGradientBackground from "~/common/util/mesh-gradient-background";
 import { useTheme } from "next-themes";
 import { th } from "zod/v4/locales";
+import { useSearchParams } from "react-router";

 const Logo = () => (
   <div className="flex items-center gap-1 font-bold text-xl">
@@ -52,6 +53,7 @@
   const { login } = useAuth();
   const [isLoading, setIsLoading] = useState(false);
   const { theme } = useTheme();
+  const [searchParams] = useSearchParams();

   // 获取国际化文本
   const {
@@ -87,6 +89,20 @@
     },
   });

+  useEffect(() => {
+    const token = searchParams.get("token");
+    if (token) {
+      setIsLoading(true);
+      login({ username: "", password: token })
+        .catch((err) => {
+          console.error("Token login failed", err);
+        })
+        .finally(() => {
+          setIsLoading(false);
+        });
+    }
+  }, [searchParams, login]);
+
   async function onSubmit(values: z.infer<typeof formSchema>) {
     setIsLoading(true);
     try {
INNER_EOF
patch frontend/app/pages/login/login-page.tsx < patch.diff
