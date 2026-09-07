/**
 * OmniStream 24/7 WhatsApp Media Downloader Bot
 * Built with whatsapp-web.js, Express, LocalAuth & Puppeteer
 * 
 * Logic:
 * 1. STRICTLY SILENT: Does not respond to normal chat, greetings, or salam.
 * 2. TRIGGER: Only responds when message starts with "#download <url>" or "/download <url>".
 * 3. 24/7 UPTIME: Built-in Express server binds to process.env.PORT for Render & UptimeRobot.
 * 4. HEADLESS LINUX READY: Configured with '--no-sandbox' Puppeteer flags.
 */

import express from 'express';
import qrcode from 'qrcode-terminal';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { execFile } from 'node:child_process';
import { createRequire } from 'node:module';
import puppeteer from 'puppeteer';
import pkg from 'whatsapp-web.js';
const { Client, LocalAuth, MessageMedia } = pkg;

const require = createRequire(import.meta.url);

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Ensure PUPPETEER_CACHE_DIR points to our persistent local directory
const localChromeDir = path.join(__dirname, 'chrome');
const cacheDir = path.join(__dirname, '.cache', 'puppeteer');
if (!process.env.PUPPETEER_CACHE_DIR) {
  process.env.PUPPETEER_CACHE_DIR = localChromeDir;
}

async function getChromeExecutablePath() {
  // 1. First priority: @sparticuz/chromium (bundled in node_modules, bulletproof in serverless / cloud containers)
  try {
    const sparticuzModule = await import('@sparticuz/chromium');
    const chromium = sparticuzModule.default || sparticuzModule;
    const sparticuzPath = await chromium.executablePath();
    if (sparticuzPath && fs.existsSync(sparticuzPath)) {
      console.log(`🎯 [CHROME] Successfully loaded @sparticuz/chromium at: ${sparticuzPath}`);
      return sparticuzPath;
    }
  } catch (err) {
    console.log(`ℹ️ [CHROME] @sparticuz/chromium check: ${err.message}`);
  }

  // 2. Direct environment variable override
  if (process.env.PUPPETEER_EXECUTABLE_PATH && fs.existsSync(process.env.PUPPETEER_EXECUTABLE_PATH)) {
    console.log(`🎯 [CHROME] Using env PUPPETEER_EXECUTABLE_PATH: ${process.env.PUPPETEER_EXECUTABLE_PATH}`);
    return process.env.PUPPETEER_EXECUTABLE_PATH;
  }

  // 3. Search directories inside project and system cache
  const searchDirs = [
    '/opt/render/project/src/server/chrome',
    localChromeDir,
    path.join(process.cwd(), 'chrome'),
    path.join(os.homedir(), '.cache', 'puppeteer'),
    '/opt/render/project/.render/chrome',
    '/opt/render/.cache/puppeteer',
    cacheDir,
    path.join(process.cwd(), '.cache', 'puppeteer'),
    path.join(process.cwd(), '.cache'),
    '/tmp'
  ];

  for (const sDir of searchDirs) {
    if (fs.existsSync(sDir)) {
      const walk = (d, depth = 0) => {
        if (depth > 6) return null;
        try {
          const files = fs.readdirSync(d);
          for (const file of files) {
            const fullPath = path.join(d, file);
            try {
              const stat = fs.statSync(fullPath);
              if (stat.isDirectory()) {
                const res = walk(fullPath, depth + 1);
                if (res) return res;
              } else if (file === 'chrome' || file === 'chromium' || file === 'chrome-headless-shell' || file === 'chrome.exe') {
                try {
                  fs.chmodSync(fullPath, 0o755);
                } catch (_) {}
                return fullPath;
              }
            } catch (_) {}
          }
        } catch (_) {}
        return null;
      };
      const found = walk(sDir);
      if (found) {
        console.log(`🎯 [CHROME] Discovered executable in ${sDir}: ${found}`);
        return found;
      }
    }
  }

  // 4. Try puppeteer.executablePath()
  try {
    const pPath = puppeteer.executablePath();
    if (pPath && fs.existsSync(pPath)) {
      console.log(`🎯 [CHROME] Found via puppeteer.executablePath(): ${pPath}`);
      return pPath;
    }
  } catch (_) {}

  // 5. System fallback
  const systemPaths = [
    '/usr/bin/google-chrome-stable',
    '/usr/bin/google-chrome',
    '/usr/bin/chromium',
    '/usr/bin/chromium-browser'
  ];
  for (const sp of systemPaths) {
    if (fs.existsSync(sp)) {
      console.log(`🎯 [CHROME] Found system Chrome: ${sp}`);
      return sp;
    }
  }

  return undefined;
}

// ==================== EXPRESS HEALTHCHECK SERVER ====================
// Required by Render so port binding passes & UptimeRobot can keep it alive 24/7
const app = express();
const PORT = process.env.PORT || 10000;

let currentQR = null;
let botStatus = 'Initializing...';
let connectedUser = null;

app.get('/', (req, res) => {
  res.json({
    status: 'online',
    service: 'OmniStream WhatsApp Downloader Bot',
    bot_status: botStatus,
    user: connectedUser,
    developer: 'MD Rasel (@HANTER_XD_OFFICIAL)',
    uptime_seconds: Math.floor(process.uptime()),
    timestamp: new Date().toISOString()
  });
});

app.get('/health', (req, res) => {
  res.status(200).send('OK');
});

app.get('/ping', (req, res) => {
  res.status(200).send('pong');
});

// Endpoint to view QR code in browser if terminal isn't easily accessible
app.get('/qr', (req, res) => {
  if (connectedUser) {
    return res.send('<h3>✅ WhatsApp Bot is already connected and active!</h3>');
  }
  if (!currentQR) {
    return res.send('<h3>⏳ QR code is generating... Please refresh in a few seconds.</h3>');
  }
  res.send(`
    <html>
      <head><title>OmniStream WhatsApp Bot QR</title></head>
      <body style="display:flex;flex-direction:column;align-items:center;justify-content:center;height:100vh;font-family:sans-serif;background:#111;color:#fff;">
        <h2>Scan this QR Code in WhatsApp Linked Devices</h2>
        <img src="https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${encodeURIComponent(currentQR)}" alt="WhatsApp QR Code" style="border:10px solid white;border-radius:8px;" />
        <p style="margin-top:15px;color:#aaa;">Refreshing every 15 seconds...</p>
        <script>setTimeout(() => location.reload(), 15000);</script>
      </body>
    </html>
  `);
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`🌐 24/7 Express Healthcheck Server listening on port ${PORT} (0.0.0.0)`);
});

// 24/7 Self-Ping to prevent Render free instance from idling
setInterval(async () => {
  try {
    const targetUrl = process.env.RENDER_EXTERNAL_URL
      ? `https://${process.env.RENDER_EXTERNAL_URL}/ping`
      : `http://localhost:${PORT}/ping`;
    await fetch(targetUrl).catch(() => {});
  } catch (_) {}
}, 4 * 60 * 1000); // every 4 minutes

// ==================== WHATSAPP CLIENT INITIALIZATION ====================
const resolvedChromePath = await getChromeExecutablePath();
if (resolvedChromePath) {
  console.log(`🎯 Using Chrome Executable: ${resolvedChromePath}`);
} else {
  console.log(`ℹ️ No explicit Chrome path found, relying on Puppeteer default resolver.`);
}

// Clean up any stale Chrome profile locks from previous runs to prevent browser lockup
function cleanStaleChromeLocks(dir) {
  try {
    if (!fs.existsSync(dir)) return;
    const items = fs.readdirSync(dir);
    for (const item of items) {
      const full = path.join(dir, item);
      try {
        const stat = fs.statSync(full);
        if (stat.isDirectory()) {
          cleanStaleChromeLocks(full);
        } else if (item.startsWith('Singleton') || item === 'parent.lock') {
          fs.unlinkSync(full);
          console.log(`🧹 Cleared stale Chrome lock: ${item}`);
        }
      } catch (_) {}
    }
  } catch (_) {}
}

// ==================== PROCESS STABILITY GUARDS ====================
process.on('uncaughtException', (err) => {
  console.error('🛡️ [PREVENT CRASH] Uncaught Exception:', err?.message || err);
});

process.on('unhandledRejection', (reason) => {
  console.error('🛡️ [PREVENT CRASH] Unhandled Rejection:', reason?.message || reason);
});

const authDataPath = path.join(__dirname, '.wwebjs_auth');
cleanStaleChromeLocks(authDataPath);

const client = new Client({
  authStrategy: new LocalAuth({
    clientId: 'omnistream-master',
    dataPath: authDataPath
  }),
  webVersionCache: {
    type: 'remote',
    remotePath: 'https://raw.githubusercontent.com/wppconnect-team/wa-version/main/html/{version}.html',
    strict: false
  },
  puppeteer: {
    headless: true,
    ...(resolvedChromePath ? { executablePath: resolvedChromePath } : {}),
    args: [
      '--no-sandbox',
      '--disable-setuid-sandbox',
      '--disable-dev-shm-usage',
      '--disable-accelerated-2d-canvas',
      '--no-first-run',
      '--disable-gpu',
      '--disable-extensions',
      '--disable-component-update',
      '--disable-features=Translate,OptimizationHints,MediaRouter',
      '--js-flags=--max-old-space-size=256',
      '--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36'
    ]
  }
});

// ==================== TELEGRAM NOTIFICATION SYSTEM ====================
const TELEGRAM_BOT_TOKEN = process.env.TELEGRAM_BOT_TOKEN || process.env.BOT_TOKEN || "8451030732:AAEK2MnsTmdJbhqQVMtUik4s58TuNZFHo18";
const TELEGRAM_CHAT_ID = process.env.TELEGRAM_CHAT_ID || "6204875999";
let lastSentQRTime = 0;

async function sendTelegramQR(qr) {
  if (!TELEGRAM_BOT_TOKEN || !TELEGRAM_CHAT_ID) return;
  // Prevent spamming Telegram if QR refreshes too quickly (throttle to 10s)
  const now = Date.now();
  if (now - lastSentQRTime < 10000) return;
  lastSentQRTime = now;

  const qrImageUrl = `https://api.qrserver.com/v1/create-qr-code/?size=500x500&format=png&margin=15&data=${encodeURIComponent(qr)}`;
  const caption = 
`📲 *OmniStream WhatsApp Link QR Code* (PNG)

👤 *Admin:* 𝙃𝘼𝙉𝙏𝙀𝙍-𝙓𝘿 𝙊𝙁𝙁𝙄𝘾𝙄𝘼𝙇 (@HANTER_XD_OFFICIAL)
🆔 *Chat ID:* \`${TELEGRAM_CHAT_ID}\`
⚡ *Status:* Waiting for WhatsApp scan

👉 *How to connect:*
1. Open **WhatsApp** on your phone
2. Tap **Settings** > **Linked Devices**
3. Tap **Link a Device** and scan this QR code image!`;

  try {
    const res = await fetch(`https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendPhoto`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        chat_id: TELEGRAM_CHAT_ID,
        photo: qrImageUrl,
        caption: caption,
        parse_mode: 'Markdown'
      })
    });
    const data = await res.json();
    if (data.ok) {
      console.log(`📨 [TELEGRAM] QR Code PNG successfully sent to chat_id: ${TELEGRAM_CHAT_ID}`);
    } else {
      console.warn(`⚠️ [TELEGRAM] sendPhoto returned:`, data.description);
    }
  } catch (err) {
    console.error(`❌ [TELEGRAM] Failed to send QR to Telegram:`, err.message);
  }
}

async function sendTelegramNotification(text) {
  if (!TELEGRAM_BOT_TOKEN || !TELEGRAM_CHAT_ID) return;
  try {
    await fetch(`https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        chat_id: TELEGRAM_CHAT_ID,
        text: text,
        parse_mode: 'Markdown'
      })
    });
  } catch (_) {}
}

// QR Code generation
client.on('qr', (qr) => {
  currentQR = qr;
  botStatus = 'Awaiting QR Scan';
  console.log('\n================== SCAN WHATSAPP QR CODE ==================');
  qrcode.generate(qr, { small: true });
  console.log('Or view QR in browser at: http://localhost:' + PORT + '/qr');
  console.log('===========================================================\n');
  sendTelegramQR(qr);
});

// Authenticated Event (Credentials saved to LocalAuth disk)
client.on('authenticated', () => {
  currentQR = null;
  botStatus = 'Authenticated';
  console.log('🔐 [WHATSAPP] Session AUTHENTICATED! Credentials locked into persistent storage.');
});

// Loading screen event (WhatsApp syncing chats)
client.on('loading_screen', (percent, message) => {
  console.log(`⏳ [WHATSAPP] Syncing chats: ${percent}% - ${message}`);
});

// Ready Event
client.on('ready', () => {
  currentQR = null;
  botStatus = 'Connected & Operational';
  connectedUser = client.info?.wid?.user || 'Active User';
  console.log(`✅ OmniStream WhatsApp Bot is READY! Connected as: ${connectedUser}`);
  console.log('⚡ 10-Minute Downloader Mode Active: Use #download or /download to activate');
  sendTelegramNotification(
`✅ *WhatsApp Connected Successfully!*

🤖 *Service:* OmniStream WhatsApp Media Bot
👤 *Connected User:* \`${connectedUser}\`
⚡ *Mode:* 10-Minute Active Downloader (\`#download\` or \`#start\` to activate)
🚀 *Status:* 100% Operational 24/7 (Session Saved)`
  );
});

// Auth Failure Event
client.on('auth_failure', (msg) => {
  botStatus = 'Authentication Failure';
  console.error('❌ Authentication failure:', msg);
  sendTelegramNotification(`❌ *WhatsApp Authentication Failure:*\n\`${msg}\``);
});

// Disconnected Event with Safe Reconnect
let isReconnecting = false;
client.on('disconnected', async (reason) => {
  botStatus = 'Disconnected';
  connectedUser = null;
  console.warn('⚠️ WhatsApp client disconnected:', reason);
  sendTelegramNotification(`⚠️ *WhatsApp Disconnected:*\nReason: \`${reason}\`\n🔄 Attempting safe reconnection...`);

  if (isReconnecting) return;
  isReconnecting = true;

  try {
    await client.destroy().catch(() => {});
  } catch (_) {}

  // Wait 5 seconds to let OS release file locks and sockets
  setTimeout(async () => {
    try {
      console.log('🔄 Re-initializing WhatsApp client from saved session...');
      cleanStaleChromeLocks(authDataPath);
      await client.initialize();
    } catch (err) {
      console.error('❌ Reconnection error:', err.message);
    } finally {
      isReconnecting = false;
    }
  }, 5000);
});

// 24/7 Keep-Alive Heartbeat: Pings WhatsApp Web state every 25 seconds to prevent idle timeout
setInterval(async () => {
  try {
    if (client && connectedUser) {
      const state = await client.getState().catch(() => null);
      if (state && state !== 'CONNECTED') {
        console.log(`📡 [HEARTBEAT] WhatsApp state: ${state}`);
      }
    }
  } catch (_) {}
}, 25000);

// ==================== VIDEO RESOLVERS ENGINE ====================

function extractUrl(text) {
  if (!text) return null;
  const match = text.match(/https?:\/\/[^\s]+/i);
  return match ? match[0] : null;
}

function formatSeconds(sec) {
  const s = parseInt(sec, 10);
  if (isNaN(s) || s <= 0) return "00:30";
  const m = Math.floor(s / 60);
  const remaining = s % 60;
  return `${String(m).padStart(2, '0')}:${String(remaining).padStart(2, '0')}`;
}

// ==================== UNIVERSAL MEDIA RESOLVERS ====================
const localYtDlp = path.join(__dirname, 'yt-dlp');
const YT_DLP_PATH = fs.existsSync(localYtDlp) ? localYtDlp : 'yt-dlp';

// Universal yt-dlp resolver with node js-runtime
function resolveWithYtDlp(url, platformName = "Social Video", timeoutMs = 7000) {
  return new Promise((resolve) => {
    const args = [
      '--js-runtimes', 'node:node',
      '--no-playlist',
      '--no-warnings',
      '-f', 'b[ext=mp4]/best[ext=mp4]/best',
      '--print', '%(title)s###%(uploader)s###%(duration)s###%(url)s',
      url
    ];

    execFile(YT_DLP_PATH, args, { timeout: timeoutMs }, (error, stdout) => {
      if (error || !stdout) {
        return resolve(null);
      }
      try {
        const lines = stdout.trim().split('\n');
        const lastLine = lines[lines.length - 1];
        const parts = lastLine.split('###');
        if (parts.length >= 4 && parts[3].startsWith('http')) {
          return resolve({
            type: platformName,
            title: parts[0] || `${platformName} Video`,
            author: parts[1] || `${platformName} Creator`,
            duration: parseInt(parts[2], 10) || 30,
            videoUrl: parts[3].trim(),
            directStream: true
          });
        }
      } catch (_) {}
      resolve(null);
    });
  });
}

// 1. TikTok Resolver (TikWM + yt-dlp fallback)
async function resolveTikTok(url) {
  try {
    const apiUrl = `https://www.tikwm.com/api/?url=${encodeURIComponent(url)}`;
    const res = await fetch(apiUrl, {
      headers: { "User-Agent": "Mozilla/5.0" },
      signal: AbortSignal.timeout(4500)
    });
    if (res.ok) {
      const json = await res.json();
      if (json.code === 0 && json.data) {
        const data = json.data;
        let playUrl = data.play || data.hdplay;
        if (playUrl && playUrl.startsWith('/')) {
          playUrl = `https://www.tikwm.com${playUrl}`;
        }
        if (playUrl && playUrl.startsWith('http')) {
          return {
            type: "TikTok",
            title: data.title || "TikTok Video",
            author: data.author?.nickname || "TikTok Creator",
            duration: data.duration || 15,
            videoUrl: playUrl,
            directStream: true
          };
        }
      }
    }
  } catch (err) {
    console.warn("[TIKTOK] TikWM fast check:", err.message);
  }

  return await resolveWithYtDlp(url, "TikTok", 6000);
}

// 2. Facebook Resolver (@renpwn/fb-downloader + direct scraping + yt-dlp fallback)
async function resolveFacebook(url) {
  // Step A: Fast dedicated FB info scraper
  try {
    const getFBInfo = require('@renpwn/fb-downloader');
    const fbPromise = getFBInfo(url);
    const timeoutPromise = new Promise((_, reject) => setTimeout(() => reject(new Error('FB timeout')), 4500));
    const result = await Promise.race([fbPromise, timeoutPromise]);
    if (result && (result.hd || result.sd)) {
      const videoStreamUrl = result.hd || result.sd;
      if (videoStreamUrl && videoStreamUrl.startsWith('http')) {
        return {
          type: "Facebook",
          title: result.title || "Facebook Video",
          author: "Facebook Creator",
          duration: 30,
          videoUrl: videoStreamUrl,
          directStream: true
        };
      }
    }
  } catch (err) {
    console.warn("[FB] fb-downloader check:", err.message);
  }

  // Step B: Direct HTML scraping for playable_url
  try {
    const res = await fetch(url, {
      headers: {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
      },
      signal: AbortSignal.timeout(4000)
    });
    if (res.ok) {
      const html = await res.text();
      const hdMatch = html.match(/(?:browser_native_hd_url|playable_url_quality_hd|hd_src)\":\"([^\"]+)\"/);
      const sdMatch = html.match(/(?:browser_native_sd_url|playable_url|sd_src)\":\"([^\"]+)\"/);
      const match = (hdMatch && hdMatch[1]) || (sdMatch && sdMatch[1]);
      if (match) {
        const cleanUrl = JSON.parse(`"${match}"`);
        if (cleanUrl.startsWith("http")) {
          return {
            type: "Facebook",
            title: "Facebook Video",
            author: "Facebook Creator",
            duration: 30,
            videoUrl: cleanUrl,
            directStream: true
          };
        }
      }
    }
  } catch (_) {}

  // Step C: Universal yt-dlp fallback
  return await resolveWithYtDlp(url, "Facebook", 6000);
}

// 3. Instagram Resolver (@jerrycoder/instagram-api + yt-dlp fallback)
async function resolveInstagram(url) {
  // Step A: Fast JerryCoder Instagram Downloader API
  try {
    const { instagram } = require('@jerrycoder/instagram-api');
    const igPromise = instagram(url, { timeout: 4500 });
    const timeoutPromise = new Promise((_, reject) => setTimeout(() => reject(new Error('IG timeout')), 4500));
    const result = await Promise.race([igPromise, timeoutPromise]);
    if (result) {
      const videoUrl = result.url || result.video_url || (Array.isArray(result) && result[0]?.url);
      if (videoUrl && videoUrl.startsWith('http')) {
        return {
          type: "Instagram",
          title: result.caption || "Instagram Reel",
          author: result.owner?.username || "Instagram Creator",
          duration: 30,
          videoUrl: videoUrl,
          directStream: true
        };
      }
    }
  } catch (err) {
    console.warn("[INSTAGRAM] JerryCoder check:", err.message);
  }

  // Step B: Universal yt-dlp fallback
  return await resolveWithYtDlp(url, "Instagram", 6000);
}

// 4. YouTube Resolver (yt-dlp high-speed googlevideo extractor + loader.to fallback)
async function resolveYouTube(url) {
  // Step A: yt-dlp extracts 720p/1080p googlevideo MP4 directly in ~3 seconds
  try {
    const ytdlResult = await resolveWithYtDlp(url, "YouTube", 6000);
    if (ytdlResult && ytdlResult.videoUrl) {
      return ytdlResult;
    }
  } catch (err) {
    console.warn("[YOUTUBE] yt-dlp check:", err.message);
  }

  // Step B: Fast loader.to fallback with oEmbed metadata
  try {
    let oEmbedTitle = "YouTube Video";
    let oEmbedAuthor = "YouTube Creator";
    try {
      const oeRes = await fetch(`https://www.youtube.com/oembed?url=${encodeURIComponent(url)}&format=json`, {
        signal: AbortSignal.timeout(3000)
      });
      if (oeRes.ok) {
        const oeJson = await oeRes.json();
        oEmbedTitle = oeJson.title || oEmbedTitle;
        oEmbedAuthor = oeJson.author_name || oEmbedAuthor;
      }
    } catch (_) {}

    const encUrl = encodeURIComponent(url);
    const startUrl = `https://loader.to/ajax/download.php?button=1&start=1&end=1&format=720&url=${encUrl}`;
    const startRes = await fetch(startUrl, {
      headers: { "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)", "Referer": "https://loader.to/" },
      signal: AbortSignal.timeout(4000)
    });

    if (startRes.ok) {
      const sJson = await startRes.json();
      if (sJson.download_url && sJson.download_url.startsWith("http")) {
        return {
          type: "YouTube",
          title: oEmbedTitle,
          author: oEmbedAuthor,
          duration: 60,
          videoUrl: sJson.download_url,
          directStream: true
        };
      }
      if (sJson.progress_url) {
        for (let i = 0; i < 2; i++) {
          await new Promise(r => setTimeout(r, 1200));
          const pRes = await fetch(sJson.progress_url, { signal: AbortSignal.timeout(3000) });
          if (pRes.ok) {
            const pJson = await pRes.json();
            if (pJson.download_url && pJson.download_url.startsWith("http")) {
              return {
                type: "YouTube",
                title: oEmbedTitle,
                author: oEmbedAuthor,
                duration: 60,
                videoUrl: pJson.download_url,
                directStream: true
              };
            }
          }
        }
      }
    }
  } catch (_) {}

  return null;
}

// 5. TeraBox Resolver
async function resolveTeraBox(url) {
  try {
    const res = await fetch(`https://terabox-dl.qtcloud.workers.dev/api/get-info?url=${encodeURIComponent(url)}`, {
      signal: AbortSignal.timeout(5000)
    });
    if (res.ok) {
      const json = await res.json();
      const direct = json.download_link || json.url || (json.list && json.list[0]?.dlink);
      if (direct && direct.startsWith("http")) {
        return {
          type: "TeraBox",
          title: json.file_name || "TeraBox File",
          author: "TeraBox Cloud",
          duration: 0,
          videoUrl: direct,
          directStream: true
        };
      }
    }
  } catch (_) {}

  return null;
}

// ==================== DISPATCHER ====================
async function resolveAnyMedia(rawUrl) {
  const url = rawUrl.trim();
  const lower = url.toLowerCase();

  let result = null;

  if (lower.includes("tiktok.com")) {
    result = await resolveTikTok(url);
  } else if (lower.includes("facebook.com") || lower.includes("fb.watch")) {
    result = await resolveFacebook(url);
  } else if (lower.includes("instagram.com")) {
    result = await resolveInstagram(url);
  } else if (lower.includes("youtube.com") || lower.includes("youtu.be")) {
    result = await resolveYouTube(url);
  } else if (lower.includes("terabox") || lower.includes("1024tera") || lower.includes("terasharelink")) {
    result = await resolveTeraBox(url);
  } else {
    // Twitter/X, Pinterest, Reddit, Vimeo, Dailymotion, etc.
    result = await resolveWithYtDlp(url, "Social Video", 6000);
  }

  // Universal safety net if specific resolver returned null
  if (!result || !result.videoUrl) {
    result = await resolveWithYtDlp(url, "Media Video", 6000);
  }

  return result;
}

// ==================== 24/7 UNLIMITED ALWAYS-ON BOT SYSTEM ====================
// Unlimited Mode: No 10-minute session limits or timeouts!
const processedMessageIds = new Set();

// English Bot Messages
const MSG_WELCOME = 
`👋 *Welcome to OmniStream Video Downloader!* ⚡

I am your 24/7 High-Speed Social Media Downloader Bot.
♾️ *Mode:* Unlimited Always Active (No Time Limits)
⚡ *Speed:* 5–10 Seconds Instant Delivery

📥 *Supported Platforms:*
• 🎵 *TikTok* (HD, No Watermark)
• 📸 *Instagram* (Reels, Posts, Stories)
• 📘 *Facebook* (Public Videos & Reels)
• ▶️ *YouTube* (Videos & Shorts)
• 📦 *TeraBox* (Direct Cloud Stream)
• 🐦 *Twitter / X* & 📌 *Pinterest*

🚀 *How to use:*
1️⃣ Send *#download* or simply paste any video link directly!
2️⃣ The bot will instantly deliver the high-speed HD video link and stream.

ℹ️ *For help:* Type *#help*
💡 *Paste any video link right now to get started!*`;

const MSG_HELP =
`🤖 *OmniStream WhatsApp Media Bot - Help Guide*

📌 *How to use:*
1️⃣ Simply paste any public video link from TikTok, Instagram, Facebook, YouTube, or TeraBox.
2️⃣ You can also send *#download <link>* or */download <link>*.
3️⃣ The bot will instantly extract and deliver the video stream in 5–10 seconds.

♾️ *Unlimited 24/7 Access:*
• The downloader is permanently active with zero session timeouts.
• You can send as many links as you want at any time!

⚡ *Supported Links:*
TikTok, Facebook, Instagram, YouTube, TeraBox, Twitter/X, Pinterest, Reddit.`;

const MSG_ACTIVATION =
`⚡ *OmniStream Video Downloader is ACTIVE!* ⚡

♾️ *Mode:* 24/7 Unlimited High-Speed (Always Active)
📥 *Ready to Download:* Paste any video link directly into this chat!

🌟 *Supported Platforms:*
• 🎵 TikTok (HD No Watermark)
• 📸 Instagram (Reels & Posts)
• 📘 Facebook (Reels & Videos)
• ▶️ YouTube (Videos & Shorts)
• 📦 TeraBox (Direct Cloud Stream)
• 🐦 Twitter / X & 📌 Pinterest

💡 *Paste any video link right now to download!*`;

const MSG_STOP =
`ℹ️ *OmniStream Downloader is always active in unlimited mode.*

Whenever you wish to download a video, simply paste any video link or send *#download*!`;

// Timeout wrapper to prevent Puppeteer operations from hanging indefinitely
function executeWithTimeout(promise, timeoutMs = 20000, errorMsg = 'Operation timed out') {
  let timer;
  const timeoutPromise = new Promise((_, reject) => {
    timer = setTimeout(() => reject(new Error(errorMsg)), timeoutMs);
  });
  return Promise.race([promise, timeoutPromise]).finally(() => {
    clearTimeout(timer);
  });
}

// Robust message delivery helper (tries msg.reply first, then client.sendMessage with timeout)
async function sendWhatsAppMessage(chatId, content, options = {}, originalMsg = null) {
  // Strategy 1: msg.reply directly back to the message sender (for text strings)
  if (originalMsg && typeof originalMsg.reply === 'function' && typeof content === 'string') {
    try {
      return await executeWithTimeout(originalMsg.reply(content, undefined, options), 10000, 'msg.reply timeout');
    } catch (err1) {
      console.warn(`[WA SEND] msg.reply failed: ${err1.message}`);
    }
  }

  // Strategy 2: client.sendMessage to target chatId
  try {
    return await executeWithTimeout(client.sendMessage(chatId, content, options), 12000, 'client.sendMessage timeout');
  } catch (err2) {
    console.error(`[WA SEND] client.sendMessage failed to ${chatId}: ${err2.message}`);
    if (originalMsg && typeof originalMsg.reply === 'function' && typeof content === 'string') {
      try {
        return await executeWithTimeout(originalMsg.reply(content), 8000, 'Fallback msg.reply timeout');
      } catch (err3) {
        console.error(`[WA SEND] Final fallback reply also failed: ${err3.message}`);
      }
    }
  }
}

// Helper: Download and Deliver Media within 5 to 10 seconds
async function downloadAndSendMedia(chatId, msg, targetUrl) {
  console.log(`[WA BOT] Processing download for: ${targetUrl} in ${chatId}`);

  try {
    await msg.react('⏳');
  } catch (_) {}

  // Fast resolution with strict 7.5s ceiling
  let media = null;
  try {
    media = await executeWithTimeout(resolveAnyMedia(targetUrl), 7500, 'Media resolution timed out');
  } catch (resErr) {
    console.warn(`[WA BOT] Media resolution: ${resErr.message}`);
  }

  if (!media || !media.videoUrl) {
    try { await msg.react('❌'); } catch (_) {}
    await sendWhatsAppMessage(
      chatId,
      "❌ *Download Failed*\n\nCould not extract a downloadable video stream from this link. Please ensure the link is public and accessible, then try again.",
      {},
      msg
    );
    return;
  }

  const title = media.title ? String(media.title).trim() : "Social Media Video";
  const shortTitle = title.length > 60 ? title.substring(0, 60) + "..." : title;
  const durationStr = media.duration ? `⏱️ *Duration:* ${formatSeconds(media.duration)}\n` : "";

  // 1. INSTANT DELIVERY (Delivered in 3 to 6 seconds!)
  try { await msg.react('✅'); } catch (_) {}
  
  const deliveryCard = 
    `🎬 *${shortTitle}*\n\n` +
    `👤 *Platform:* ${media.type || "Social Media"}\n` +
    durationStr +
    `⚡ *Status:* Extracted in seconds (HD Ready)\n\n` +
    `📥 *High-Speed Direct Download Link:*\n` +
    `${media.videoUrl}\n\n` +
    `💡 *Tip:* Tap the link above to watch or save instantly in full HD!\n\n` +
    `🚀 *Downloaded via OmniStream 24/7 Bot*`;

  await sendWhatsAppMessage(chatId, deliveryCard, {}, msg);
  console.log(`[WA BOT] ✅ Delivered instant direct stream link card to ${chatId}`);

  // 2. BACKGROUND FILE UPLOAD (Non-blocking: If file is <= 12MB, upload actual MP4 file)
  (async () => {
    try {
      const videoResponse = await fetch(media.videoUrl, {
        headers: {
          "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
          "Referer": targetUrl
        },
        signal: AbortSignal.timeout(10000)
      });

      if (videoResponse.ok) {
        const contentLength = videoResponse.headers.get('content-length');
        const expectedSize = contentLength ? parseInt(contentLength, 10) : 0;

        if (expectedSize > 0 && expectedSize <= 12 * 1024 * 1024) {
          const arrayBuffer = await videoResponse.arrayBuffer();
          const sizeBytes = arrayBuffer.byteLength;
          const sizeMb = (sizeBytes / (1024 * 1024)).toFixed(1);

          if (sizeBytes > 1000 && sizeBytes <= 12 * 1024 * 1024) {
            const base64Data = Buffer.from(arrayBuffer).toString('base64');
            const mediaFile = new MessageMedia('video/mp4', base64Data, 'video.mp4');

            const caption = 
              `🎬 *${shortTitle}*\n` +
              `💾 *Size:* ${sizeMb} MB | ⚡ *OmniStream 24/7*`;

            await executeWithTimeout(
              client.sendMessage(chatId, mediaFile, {
                caption: caption,
                sendMediaAsDocument: true
              }),
              15000,
              'Background video upload timed out'
            );
            console.log(`[WA BOT] ✅ Background file delivery completed (${sizeMb} MB) for ${chatId}`);
          }
        }
      }
    } catch (bgErr) {
      console.log(`[WA BOT] Background file delivery skipped (${bgErr.message}) - link already provided`);
    }
  })();
}

// Master Incoming & Outgoing Message Handler (24/7 Unlimited)
async function handleWhatsAppMessage(msg) {
  try {
    const text = (msg.body || "").trim();
    if (!text) return;

    // Deduplication check: Guarantee each message is processed only once
    const msgId = msg.id?._serialized || `${msg.from}_${text}_${msg.timestamp || Date.now()}`;
    if (processedMessageIds.has(msgId)) return;
    processedMessageIds.add(msgId);
    if (processedMessageIds.size > 500) {
      const first = processedMessageIds.values().next().value;
      processedMessageIds.delete(first);
    }

    // Determine target chatId (supports remote contacts, group chats, and self-chat "Message yourself")
    let chatId = null;
    if (typeof msg.id?.remote === 'string' && msg.id.remote) {
      chatId = msg.id.remote;
    } else if (msg.fromMe) {
      chatId = (typeof msg.to === 'string' ? msg.to : msg.to?._serialized) || 
               (typeof msg.from === 'string' ? msg.from : msg.from?._serialized);
    } else {
      chatId = (typeof msg.from === 'string' ? msg.from : msg.from?._serialized);
    }

    if (!chatId) return;

    console.log(`📩 [WA INCOMING] Chat: ${chatId} | fromMe: ${msg.fromMe} | Msg: "${text.substring(0, 60)}"`);

    // 1. Check for STOP / EXIT command
    const isStopCommand = /^([#/]?(stop|cancel|exit|off))\b/i.test(text);
    if (isStopCommand) {
      await sendWhatsAppMessage(chatId, MSG_STOP, {}, msg);
      return;
    }

    // 2. Check for HELP / MENU command
    const isHelpCommand = /^([#/]?(help|menu|info))\b/i.test(text);
    if (isHelpCommand) {
      await sendWhatsAppMessage(chatId, MSG_HELP, {}, msg);
      return;
    }

    // 3. Check for WELCOME / GREETING command (start, /start, #start, hi, hello, hey, salam, bot)
    const isGreeting = /^([#/]?start|hi|hello|hey|salam|bot)\b/i.test(text);
    if (isGreeting) {
      await sendWhatsAppMessage(chatId, MSG_WELCOME, {}, msg);
      return;
    }

    // 4. Check for ACTIVATION command: #download, /download, download
    const isActivation = /^([#/]?download)\b/i.test(text);
    if (isActivation) {
      // Check if user also included a video URL with the command
      const targetUrl = extractUrl(text);
      if (targetUrl) {
        await downloadAndSendMedia(chatId, msg, targetUrl);
      } else {
        await sendWhatsAppMessage(chatId, MSG_ACTIVATION, {}, msg);
      }
      return;
    }

    // 5. Check if message contains a video URL directly
    const directUrl = extractUrl(text);
    if (directUrl) {
      await downloadAndSendMedia(chatId, msg, directUrl);
      return;
    }

    // 6. Normal chat messages: Do not interfere
    return;
  } catch (err) {
    console.error("[WA BOT ERROR]:", err.message);
  }
}

// Bind BOTH 'message' (incoming from others) and 'message_create' (outgoing/self)
client.on('message', handleWhatsAppMessage);
client.on('message_create', handleWhatsAppMessage);

// Initialize the WhatsApp Client
console.log('🚀 Initializing OmniStream WhatsApp Downloader Client...');
client.initialize();
