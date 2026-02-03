import express from 'express';
import puppeteer from 'puppeteer';
import path from 'path';
import { fileURLToPath } from 'url';
import fs from 'fs';
import sharp from 'sharp';

// ES Module 环境下获取 __dirname
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT = 3000;

// 判断是否为开发模式
const isDev = process.env.NODE_ENV === 'development';

app.use(express.json());

// === 核心修改开始 ===
async function startServer() {
    
    if (isDev) {
        console.log("⚠️ Running in Development Mode (Vite Middleware) ⚠️");
        console.log("   - Source maps enabled");
        console.log("   - No build required");
        
        // 动态引入 vite (仅在开发模式下需要)
        const { createServer } = await import('vite');
        
        // 创建 Vite 服务实例 (以中间件模式)
        const vite = await createServer({
            server: { middlewareMode: true },
            appType: 'spa', // 关键：让 Vite 处理 index.html 的加载
        });

        // 使用 Vite 的中间件
        app.use(vite.middlewares);
    } else {
        console.log("✅ Running in Production Mode (Static Dist)");
        // 生产模式：托管构建后的 dist 目录
        app.use(express.static(path.join(__dirname, 'dist')));
    }

    // 托管 public 目录 (无论是开发还是生产，public 里的 test.schem 等资源都需要)
    app.use(express.static(path.join(__dirname, 'public')));

    // ... Puppeteer 逻辑 (保持不变) ...
    let browser;
    async function initBrowser() {
        browser = await puppeteer.launch({
            headless: "new",
            args: [
                '--no-sandbox', 
                '--disable-setuid-sandbox',
                '--use-gl=swiftshader',
                '--hide-scrollbars'
            ]
        });
        console.log("Headless Browser Launched");
    }

    app.post('/api/render', async (req, res) => {
        const { 
            schematicUrl, 
            width = 800, 
            height = 600,
            alpha,  // Optional
            beta,   // Optional
            radius  // Optional
        } = req.body;

        console.log("Requested to generate, url=" + schematicUrl + ", " + width + "x" + height + ", a=" + alpha + ", b=" + beta + ", r=" + radius);

        if (!schematicUrl) {
            console.error("    -> Error! Missing schematic URL")
            return res.status(400).json({error: 'Missing schematicUrl'})
        }

        let page;
        try {
            page = await browser.newPage();
            await page.setViewport({ width, height, deviceScaleFactor: 1 });

            // 访问页面
            // 注意：在 Dev 模式下，Vite 会拦截这个请求并返回原始的 index.html
            await page.goto(`http://localhost:${PORT}/index.html?headless=true`, { 
                waitUntil: 'networkidle0' 
            });

            // 执行渲染
            await page.evaluate((url, w, h, opts) => {
                return window.renderSchematic(url, w, h, opts);
            }, schematicUrl, width, height, { alpha, beta, radius });

            await page.waitForSelector('body[data-status="ready"]', { timeout: 60000 });

            // 2. 【修改】截图设置
        // omitBackground: false -> 我们需要把洋红色背景截下来
        const rawBuffer = await page.screenshot({ 
            type: 'png', 
            omitBackground: false 
        });

        console.log("    -> Respond SUCCESS for url=" + schematicUrl + ", " + width + "x" + height + ", a=" + alpha + ", b=" + beta + ", r=" + radius);

        // 3. 【核心】使用 Sharp 进行抠图处理
        // 获取原始像素数据
        const image = sharp(rawBuffer);
        const { data, info } = await image
            .ensureAlpha() // 确保有 Alpha 通道
            .raw()         // 获取原始 Buffer (R, G, B, A, R, G, B, A...)
            .toBuffer({ resolveWithObject: true });

        // 遍历所有像素 (每4个字节代表一个像素: R, G, B, A)
        const pixelCount = data.length / 4;
        
        // 定义我们要剔除的颜色：洋红色 (255, 0, 255)
        const targetR = 255;
        const targetG = 255;
        const targetB = 255;

        // 如果你坚持要用白色，把上面改成 255, 255, 255

        for (let i = 0; i < pixelCount; i++) {
            const offset = i * 4;
            const r = data[offset];
            const g = data[offset + 1];
            const b = data[offset + 2];

            // 简单判断：如果是纯洋红色，将 Alpha (offset+3) 设为 0
            if (r === targetR && g === targetG && b === targetB) {
                data[offset + 3] = 0; // 透明
            }
        }

        // 将处理后的 Buffer 转回 PNG
        const finalBuffer = await sharp(data, {
            raw: {
                width: info.width,
                height: info.height,
                channels: 4
            }
        })
        .png()
        .toBuffer();

        console.log("    -> Post processing SUCCESS for url=" + schematicUrl + ", " + width + "x" + height + ", a=" + alpha + ", b=" + beta + ", r=" + radius);

        // 4. 返回处理后的图片
        res.set('Content-Type', 'image/png');
        res.send(finalBuffer);
        console.log("    -> Respond send for url=" + schematicUrl + ", " + width + "x" + height + ", a=" + alpha + ", b=" + beta + ", r=" + radius);
        } catch (error) {
            console.error("    -> Render Error:", error);
            res.status(500).json({ error: error.message });
        } finally {
            if (page) await page.close();
        }
    });

    await initBrowser();
    
    app.listen(PORT, () => {
        console.log(`Render Service running at http://localhost:${PORT}`);
        if(isDev) {
            console.log(`👉 Visit http://localhost:${PORT}/index.html to inspect source code`);
        }
    });
}

startServer();
// === 核心修改结束 ===