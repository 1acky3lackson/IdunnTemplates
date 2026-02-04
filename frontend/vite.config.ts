import { intlayer, intlayerCompiler } from 'vite-intlayer'; // Add the plugin to the Vite plugin list
import { reactRouter } from "@react-router/dev/vite";
import tailwindcss from "@tailwindcss/vite";
import path from "path";
import { defineConfig } from "vite";
import tsconfigPaths from "vite-tsconfig-paths";

export default defineConfig({
  plugins: [intlayer(), intlayerCompiler(), tailwindcss(), reactRouter(), tsconfigPaths(),],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './app')
    }
  },
  server:{
    proxy: {
      // 匹配所有以 /api 开头的请求
      '/api': {
        target: 'https://idunn.taixue.cc', // 后端真实地址
        changeOrigin: true, // 关键：欺骗后端，让后端觉得请求是来自 idunn.taixue.cc
        secure: false, // 如果后端是 https 但证书有问题，可以设为 false
        
        // 核心步骤：重写 Cookie 的 Domain
        // 这样浏览器收到的 Cookie Domain 就会变成 localhost，从而成功写入
        cookieDomainRewrite: {
          "idunn.taixue.cc": "localhost" 
        }
      }
    }
  }
});
