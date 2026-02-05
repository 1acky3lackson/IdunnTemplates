import express from 'express';
import puppeteer from 'puppeteer';
import path from 'path';
import { fileURLToPath } from 'url';
import fs from 'fs';
import sharp from 'sharp';
import axios from 'axios';

// ES Module 环境配置
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT = 3000;
const isDev = process.env.NODE_ENV === 'development';

// 允许解析较大的 JSON (用于 Base64 传输)
app.use(express.json({ limit: '50mb' }));

// === 辅助函数：格式化日志 ===
const log = {
    info: (msg) => console.log(`\x1b[36m%s\x1b[0m`, `ℹ️  ${msg}`),
    success: (msg) => console.log(`\x1b[32m%s\x1b[0m`, `✅ ${msg}`),
    warn: (msg) => console.log(`\x1b[33m%s\x1b[0m`, `⚠️  ${msg}`),
    error: (msg) => console.log(`\x1b[31m%s\x1b[0m`, `❌ ${msg}`),
    step: (msg) => console.log(`\x1b[35m%s\x1b[0m`, `⚡ ${msg}`),
    timer: (label) => {
        const start = Date.now();
        return {
            stop: () => `${((Date.now() - start) / 1000).toFixed(2)}s`
        };
    }
};

async function startServer() {
    console.log("\n" + "=".repeat(50));
    log.info(`Initializing Render Service on Port ${PORT}...`);
    console.log("=".repeat(50));

    // 1. 初始化 Vite 中间件 (仅开发模式)
    if (isDev) {
        log.warn("Running in Development Mode (Vite Middleware)");
        const { createServer } = await import('vite');
        const vite = await createServer({
            server: { middlewareMode: true },
            appType: 'spa',
        });
        app.use(vite.middlewares);
    } else {
        log.success("Running in Production Mode (Serving static dist)");
        app.use(express.static(path.join(__dirname, 'dist')));
    }

    // 托管公共资源
    app.use(express.static(path.join(__dirname, 'public')));

    // 2. 启动浏览器
    let browser;
    const browserTimer = log.timer();
    try {
        browser = await puppeteer.launch({
            headless: "new",
            protocolTimeout: 0, // 设置为 0 表示禁用超时，或者设置为 120000 (2分钟)
            args: [
                '--no-sandbox', 
                '--disable-setuid-sandbox',
                '--use-gl=swiftshader',
                '--hide-scrollbars'
            ]
        });
        log.success(`Headless Browser Launched in ${browserTimer.stop()}`);
    } catch (err) {
        log.error(`Failed to launch browser: ${err.message}`);
        process.exit(1);
    }

    // 3. 渲染接口
    app.post('/api/render', async (req, res) => {
        const { schematicUrl, width = 800, height = 600, alpha, beta, radius } = req.body;
        const requestTimer = log.timer();
        
        log.info(`New Request: [${width}x${height}] URL: ${schematicUrl.substring(0, 50)}...`);

        if (!schematicUrl) {
            log.error("Missing schematicUrl in request body");
            return res.status(400).json({ error: 'Missing schematicUrl' });
        }

        let page;
        try {
            // 步骤 A: 抓取文件并转码
            log.step("Fetching schematic file from source...");
            const fetchTimer = log.timer();
            const response = await axios.get(schematicUrl, { responseType: 'arraybuffer' });
            const base64SchemFileStr = Buffer.from(response.data).toString('base64');
            log.success(`File fetched and base64 encoded (${fetchTimer.stop()})`);

            // 步骤 B: 创建页面并加载环境
            log.step("Opening browser page...");
            page = await browser.newPage();
            await page.setViewport({ width, height });
            
            await page.goto(`http://localhost:${PORT}/index.html?headless=true`, { 
                waitUntil: 'networkidle0' 
            });
            log.success("Render environment loaded");

            // 步骤 C: 执行前端渲染脚本
            log.step("Executing WebGL rendering...");
            const renderTimer = log.timer();
            await page.evaluate((b64, w, h, opts) => {
                return window.renderSchematicFromBase64(b64, w, h, opts);
            }, base64SchemFileStr, width, height, { alpha, beta, radius });

            // 等待前端标记 ready
            await page.waitForSelector('body[data-status="ready"]', { timeout: 3000000 });
            log.success(`WebGL Render complete (${renderTimer.stop()})`);

            // 步骤 D: 截图
            log.step("Capturing screenshot...");
            const rawBuffer = await page.screenshot({ type: 'png', omitBackground: false });

            // 步骤 E: Sharp 图像后期处理 (扣除背景)
            log.step("Post-processing: Removing background alpha...");
            const postTimer = log.timer();
            const image = sharp(rawBuffer);
            const { data, info } = await image.ensureAlpha().raw().toBuffer({ resolveWithObject: true });

            const pixelCount = data.length / 4;
            // 匹配前端的 0x012345 (R:1, G:35, B:69)
            const targetR = 255, targetG = 255, targetB = 255;

            for (let i = 0; i < pixelCount; i++) {
                const offset = i * 4;
                if (data[offset] === targetR && data[offset+1] === targetG && data[offset+2] === targetB) {
                    data[offset + 3] = 0; // 设置为全透明
                }
            }

            const finalBuffer = await sharp(data, {
                raw: { width: info.width, height: info.height, channels: 4 }
            }).png().toBuffer();
            log.success(`Image processed (${postTimer.stop()})`);

            // 4. 返回结果
            res.set('Content-Type', 'image/png');
            res.send(finalBuffer);
            log.success(`✨ Request finished successfully in ${requestTimer.stop()}!`);

        } catch (error) {
            log.error(`Render Failed: ${error.message}`);
            res.status(500).json({ error: error.message });
        } finally {
            if (page) {
                await page.close();
                log.info("Browser page closed.");
            }
        }
    });

    // 启动服务
    app.listen(PORT, () => {
        console.log("\n" + "-".repeat(50));
        log.success(`🚀 SERVICE READY AT http://localhost:${PORT}`);
        if(isDev) log.info(`Debug UI: http://localhost:${PORT}/index.html`);
        console.log("-".repeat(50) + "\n");
    });
}

startServer();