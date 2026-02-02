import { defineConfig } from 'vite';
import path from 'path';

export default defineConfig({
    // 强制 Vite 预构建 buffer 包，防止开发模式下转换出错
    optimizeDeps: {
        include: ['buffer'],
    },
    define: {
        // 某些库可能还需要 global 变量
        global: 'window',
    },
    // 构建输出配置
    build: {
        outDir: 'dist',
        emptyOutDir: true,
    },
    // 路径别名 (可选)
    resolve: {
        alias: {
            '@': path.resolve(__dirname, './src'),
            buffer: 'buffer/',
        },
    },
});