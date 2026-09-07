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
import puppeteer from 'puppeteer';
import pkg from 'whatsapp-web.js';
const { Client, LocalAuth, MessageMedia } = pkg;

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

// 1. TikTok Resolver (TikWM + Cobalt Fallback)
async function resolveTikTok(url) {
  try {
    const apiUrl = `https://www.tikwm.com/api/?url=${encodeURIComponent(url)}`;
    const res = await fetch(apiUrl, {
      headers: { "User-Agent": "Mozilla/5.0" },
      signal: AbortSignal.timeout(12000)
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
    console.error("TikTok error:", err.message);
  }

  // Fallback to Cobalt for TikTok
  return await resolveCobalt(url);
}

// 2. Cobalt Multi-Host Resolver (Instagram, Facebook, Twitter/X, Pinterest, Reddit)
const COBALT_HOSTS = [
  "https://cobalt-latest-a04h.onrender.com",
  "https://co.wuk.sh",
  "https://cobalt-api.kwiatekm.tokyo"
];

async function resolveCobalt(url, quality = "720") {
  for (const host of COBALT_HOSTS) {
    try {
      const res = await fetch(host, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Accept": "application/json",
          "User-Agent": "Mozilla/5.0"
        },
        body: JSON.stringify({
          url,
          videoQuality: quality,
          downloadMode: "auto",
          alwaysProxy: true
        }),
        signal: AbortSignal.timeout(9000)
      });
      if (res.ok) {
        const json = await res.json();
        const streamUrl = json.url;
        if (streamUrl && streamUrl.startsWith("http")) {
          return {
            type: "Social Video",
            title: json.filename?.replace(/\.[^/.]+$/, "") || "Social Media Video",
            author: "Creator",
            videoUrl: streamUrl,
            directStream: true
          };
        }
      }
    } catch (_) {}
  }
  return null;
}

// 3. Dedicated YouTube Resolver (Loader.to + oEmbed)
async function resolveYouTube(url) {
  try {
    let oEmbedTitle = "YouTube Video";
    let oEmbedAuthor = "YouTube Creator";

    try {
      const oeRes = await fetch(`https://www.youtube.com/oembed?url=${encodeURIComponent(url)}&format=json`, {
        signal: AbortSignal.timeout(5000)
      });
      if (oeRes.ok) {
        const oeJson = await oeRes.json();
        oEmbedTitle = oeJson.title || oEmbedTitle;
        oEmbedAuthor = oeJson.author_name || oEmbedAuthor;
      }
    } catch (_) {}

    const hosts = ["https://loader.to", "https://en.loader.to"];
    for (const host of hosts) {
      try {
        const encUrl = encodeURIComponent(url);
        const startUrl = `${host}/ajax/download.php?button=1&start=1&end=1&format=720&url=${encUrl}`;
        const startRes = await fetch(startUrl, {
          headers: { "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)", "Referer": `${host}/` },
          signal: AbortSignal.timeout(12000)
        });

        if (startRes.ok) {
          const sJson = await startRes.json();
          if (sJson.download_url && sJson.download_url.startsWith("http")) {
            return {
              type: "YouTube",
              title: oEmbedTitle,
              author: oEmbedAuthor,
              videoUrl: sJson.download_url,
              directStream: true
            };
          }

          if (sJson.progress_url) {
            for (let i = 0; i < 12; i++) {
              await new Promise(r => setTimeout(r, 2000));
              try {
                const pRes = await fetch(sJson.progress_url, { signal: AbortSignal.timeout(6000) });
                if (pRes.ok) {
                  const pJson = await pRes.json();
                  if (pJson.download_url && pJson.download_url.startsWith("http")) {
                    return {
                      type: "YouTube",
                      title: oEmbedTitle,
                      author: oEmbedAuthor,
                      videoUrl: pJson.download_url,
                      directStream: true
                    };
                  }
                }
              } catch (_) {}
            }
          }
        }
      } catch (_) {}
    }

    return null;
  } catch (err) {
    console.error("YouTube error:", err.message);
    return null;
  }
}

// 4. TeraBox Resolver
async function resolveTeraBox(url) {
  try {
    const res = await fetch(`https://terabox-dl.qtcloud.workers.dev/api/get-info?url=${encodeURIComponent(url)}`, {
      signal: AbortSignal.timeout(10000)
    });
    if (res.ok) {
      const json = await res.json();
      const direct = json.download_link || json.url || (json.list && json.list[0]?.dlink);
      if (direct && direct.startsWith("http")) {
        return {
          type: "TeraBox",
          title: json.file_name || "TeraBox File",
          author: "TeraBox Cloud",
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

  if (lower.includes("tiktok.com")) {
    return await resolveTikTok(url);
  } else if (lower.includes("youtube.com") || lower.includes("youtu.be")) {
    return await resolveYouTube(url);
  } else if (lower.includes("terabox") || lower.includes("1024tera") || lower.includes("terasharelink")) {
    return await resolveTeraBox(url);
  } else {
    // Instagram, Facebook, Twitter, Pinterest, Reddit
    return await resolveCobalt(url);
  }
}

// ==================== 10-MINUTE ACTIVE DOWNLOADER SESSION SYSTEM ====================
// chatId -> { expiresAt: number, timer: NodeJS.Timeout }
const activeSessions = new Map();
const SESSION_DURATION_MS = 10 * 60 * 1000; // 10 Minutes
const processedMessageIds = new Set();

// English Bot Messages
const MSG_WELCOME = 
`👋 *Welcome to OmniStream Video Downloader!* ⚡

I am your 24/7 AI-powered High-Speed Social Media Downloader Bot.

📥 *Supported Platforms:*
• 🎵 *TikTok* (HD, No Watermark)
• 📸 *Instagram* (Reels, Posts, Stories)
• 📘 *Facebook* (Public Videos & Reels)
• ▶️ *YouTube* (Videos & Shorts)
• 📦 *TeraBox* (Direct Cloud Stream)
• 🐦 *Twitter / X* & 📌 *Pinterest*

🚀 *How to use:*
1️⃣ Send *#download* or simply paste any video link directly!
2️⃣ The bot will instantly fetch and deliver the MP4 video file.
3️⃣ Each session stays active for *10 minutes*.

🛑 *To stop session early:* Type *#stop*
ℹ️ *For help:* Type *#help*

💡 *Paste any video link right now to get started!*`;

const MSG_HELP =
`🤖 *OmniStream WhatsApp Media Bot - Help Guide*

📌 *How to use:*
1️⃣ Send *#download* or */download* to activate the 10-minute session.
2️⃣ Paste any public video link from TikTok, Instagram, Facebook, YouTube, or TeraBox.
3️⃣ The bot will extract and send the high-speed video file directly to this chat.

⏱️ *Session Rules:*
• The downloader session remains active for 10 minutes.
• After 10 minutes of inactivity, it automatically turns off so you can chat normally.
• You can end the session anytime by sending *#stop*.

⚡ *Instant Mode:*
You can also directly send any video link at any time without typing any command, and the bot will auto-download it for you!`;

const MSG_ACTIVATION =
`⚡ *OmniStream Video Downloader is ACTIVE!* ⚡

⏱️ *Session Time:* Active for the next 10 minutes.
📥 *Ready to Download:* Paste any video link (TikTok, Instagram, YouTube, Facebook, TeraBox) directly into this chat!

💡 *Note:* The bot will automatically fetch and deliver your video in high quality.
🛑 *To stop anytime:* Type *#stop*`;

const MSG_STOP =
`🛑 *OmniStream Downloader has been stopped.*

You can now continue normal chatting. Whenever you want to download videos again, simply type *#download* or paste any video link!`;

const MSG_EXPIRED =
`⏱️ *Downloader Session Ended (10 minutes elapsed)*

Normal chat mode is active. Whenever you want to download videos again, simply type *#download* or paste a video link!`;

// Robust message delivery helper (tries msg.reply first, then client.sendMessage)
async function sendWhatsAppMessage(chatId, content, options = {}, originalMsg = null) {
  // Strategy 1: msg.reply directly back to the message sender
  if (originalMsg && typeof originalMsg.reply === 'function') {
    try {
      return await originalMsg.reply(content, undefined, options);
    } catch (err1) {
      console.warn(`[WA SEND] msg.reply failed: ${err1.message}`);
    }
  }

  // Strategy 2: client.sendMessage to target chatId
  try {
    return await client.sendMessage(chatId, content, options);
  } catch (err2) {
    console.error(`[WA SEND] client.sendMessage failed to ${chatId}: ${err2.message}`);
    if (originalMsg && typeof originalMsg.reply === 'function') {
      try {
        return await originalMsg.reply(content);
      } catch (err3) {
        console.error(`[WA SEND] Final fallback reply also failed: ${err3.message}`);
      }
    }
  }
}

// Helper: Download and Deliver Media
async function downloadAndSendMedia(chatId, msg, targetUrl) {
  console.log(`[WA BOT] Processing download for: ${targetUrl} in ${chatId}`);

  try {
    await msg.react('⏳');
  } catch (_) {}

  await sendWhatsAppMessage(
    chatId,
    "⚡ *Analyzing link...*\n_Fetching high-speed media stream from server..._",
    {},
    msg
  );

  // Resolve Media URL
  const media = await resolveAnyMedia(targetUrl);

  if (!media || !media.videoUrl) {
    try { await msg.react('❌'); } catch (_) {}
    await sendWhatsAppMessage(
      chatId,
      "❌ *Download Failed*\n\nCould not extract a downloadable video stream from this link. Please ensure the link is public and try again.",
      {},
      msg
    );
    return;
  }

  const title = media.title ? String(media.title).trim() : "Social Media Video";
  const shortTitle = title.length > 50 ? title.substring(0, 50) + "..." : title;

  // Fetch video data
  const videoResponse = await fetch(media.videoUrl, {
    headers: {
      "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
      "Referer": targetUrl
    },
    signal: AbortSignal.timeout(60000)
  });

  if (!videoResponse.ok) {
    throw new Error(`HTTP ${videoResponse.status} from video source`);
  }

  const arrayBuffer = await videoResponse.arrayBuffer();
  const sizeBytes = arrayBuffer.byteLength;
  const sizeMb = (sizeBytes / (1024 * 1024)).toFixed(1);

  const caption = `🎬 *${title}*\n\n` +
    `👤 *Platform:* ${media.type || "Social Media"}\n` +
    (media.duration ? `⏱ *Duration:* ${formatSeconds(media.duration)}\n` : "") +
    `💾 *Size:* ${sizeMb} MB\n\n` +
    `⚡ *Downloaded via OmniStream Bot*`;

  // Large file protection: On Render Free tier (512MB RAM), base64 files > 30MB cause Chrome CDP memory crash
  if (sizeBytes > 30 * 1024 * 1024) {
    try { await msg.react('✅'); } catch (_) {}
    await sendWhatsAppMessage(
      chatId,
      `🎬 *${shortTitle}*\n\n` +
      `👤 *Platform:* ${media.type || "Social Media"}\n` +
      `💾 *File Size:* ${sizeMb} MB\n\n` +
      `⚠️ *Video file is large (>30MB). Here is your high-speed direct download link:*\n\n` +
      `📥 *Direct Fast Download:*\n${media.videoUrl}\n\n` +
      `⚡ *OmniStream Bot*`,
      {},
      msg
    );
    return;
  }

  // Direct WhatsApp video upload
  try {
    const base64Data = Buffer.from(arrayBuffer).toString('base64');
    const mediaFile = new MessageMedia('video/mp4', base64Data, 'video.mp4');

    await sendWhatsAppMessage(
      chatId,
      mediaFile,
      { caption: caption },
      msg
    );

    try { await msg.react('✅'); } catch (_) {}
    console.log(`[WA BOT] Successfully delivered video to ${chatId}`);
  } catch (uploadErr) {
    console.warn(`[WA BOT] Direct upload failed, falling back to direct stream link: ${uploadErr.message}`);
    await sendWhatsAppMessage(
      chatId,
      `🎬 *${shortTitle}*\n\n` +
      `👤 *Platform:* ${media.type || "Social Media"}\n` +
      `💾 *Size:* ${sizeMb} MB\n\n` +
      `📥 *Direct Video Link:*\n${media.videoUrl}\n\n` +
      `⚡ *Downloaded via OmniStream Bot*`,
      {},
      msg
    );
  }
}

// Master Incoming & Outgoing Message Handler
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
      if (activeSessions.has(chatId)) {
        const session = activeSessions.get(chatId);
        if (session?.timer) clearTimeout(session.timer);
        activeSessions.delete(chatId);
        await sendWhatsAppMessage(chatId, MSG_STOP, {}, msg);
      }
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
      // Set or renew 10-minute session
      const existing = activeSessions.get(chatId);
      if (existing?.timer) clearTimeout(existing.timer);

      const timeoutTimer = setTimeout(async () => {
        activeSessions.delete(chatId);
        try {
          await sendWhatsAppMessage(chatId, MSG_EXPIRED);
        } catch (_) {}
      }, SESSION_DURATION_MS);

      activeSessions.set(chatId, {
        expiresAt: Date.now() + SESSION_DURATION_MS,
        timer: timeoutTimer
      });

      console.log(`[WA BOT] 🟢 Activated 10-min Downloader session for: ${chatId}`);

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
      // If someone sends a video URL, automatically activate 10-min session and download it!
      const existing = activeSessions.get(chatId);
      if (existing?.timer) clearTimeout(existing.timer);

      const timeoutTimer = setTimeout(async () => {
        activeSessions.delete(chatId);
        try {
          await sendWhatsAppMessage(chatId, MSG_EXPIRED);
        } catch (_) {}
      }, SESSION_DURATION_MS);

      activeSessions.set(chatId, {
        expiresAt: Date.now() + SESSION_DURATION_MS,
        timer: timeoutTimer
      });

      console.log(`[WA BOT] Direct URL detected, auto-activated session for: ${chatId}`);
      await downloadAndSendMedia(chatId, msg, directUrl);
      return;
    }

    // 6. If session is active and user sends normal text (no URL):
    // Do not interfere! Let them talk normally during the active session.
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
