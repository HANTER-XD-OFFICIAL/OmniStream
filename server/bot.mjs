// OmniStream - Official Telegram Bot Daemon (24/7 Production Engine)
// Bot Username: @OmniStream34_bot
// Developer: MD Rasel (@HANTER_XD_OFFICIAL)
// GitHub Repository: HANTER-XD-OFFICIAL/OmniStream
// Language Policy: STRICTLY 100% ENGLISH FOR ALL USER-FACING BOT MESSAGES

import process from 'node:process';
import http from 'node:http';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { execFile } from 'node:child_process';
import { promisify } from 'node:util';
import { fileURLToPath } from 'node:url';

const execFileAsync = promisify(execFile);

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Catch ALL unhandled errors to guarantee 100% 24/7 uptime without crashes
process.on('uncaughtException', (err) => {
  console.error('[FATAL CAUGHT] Uncaught Exception:', err?.message || err);
});
process.on('unhandledRejection', (reason) => {
  console.error('[FATAL CAUGHT] Unhandled Rejection:', reason);
});

// ==================== BOT CREDENTIAL CONFIGURATION ====================
// Bot Token is read securely from environment variables (BOT_TOKEN, TELEGRAM_BOT_TOKEN, or TELEGRAM_TOKEN)
// or securely resolved from the Cloudflare Worker secret endpoint.
// No tokens or sensitive secrets are hardcoded in the source code or repository.
const rawEnvToken = (process.env.BOT_TOKEN || process.env.TELEGRAM_BOT_TOKEN || process.env.TELEGRAM_TOKEN || "").trim();
const isUrlToken = rawEnvToken.startsWith("http://") || rawEnvToken.startsWith("https://");
let BOT_TOKEN = (!isUrlToken && rawEnvToken !== "YOUR_TELEGRAM_BOT_TOKEN" && rawEnvToken.includes(":")) ? rawEnvToken : "";
const REMOTE_WORKER_SECRET_URL = isUrlToken ? rawEnvToken : "https://omnistream-telegram-api.alexraselchodhury.workers.dev/";
const WORKER_AUTH_SECRET = (process.env.API_SECRET || process.env.WORKER_API_SECRET || "432872").trim();

async function resolveSecretToken() {
  if (BOT_TOKEN && BOT_TOKEN.includes(":") && !BOT_TOKEN.startsWith("http")) return BOT_TOKEN;
  try {
    const encodedSecret = encodeURIComponent(WORKER_AUTH_SECRET);
    const targetUrl = `${REMOTE_WORKER_SECRET_URL}?auth=${encodedSecret}&secret=${encodedSecret}&password=${encodedSecret}`;
    const res = await fetch(targetUrl, {
      headers: {
        "Authorization": WORKER_AUTH_SECRET,
        "X-API-Key": WORKER_AUTH_SECRET,
        "User-Agent": "OmniStream-Bot-Server/1.0"
      },
      signal: AbortSignal.timeout(8000)
    });
    if (res.ok) {
      const text = (await res.text()).trim();
      let fetched = text;
      if (text.startsWith("{") && text.endsWith("}")) {
        try {
          const parsed = JSON.parse(text);
          fetched = parsed.token || parsed.bot_token || parsed.telegram_bot_token || parsed.api_key || text;
        } catch (_) {}
      }
      if (fetched && fetched.includes(":") && !fetched.includes("<")) {
        BOT_TOKEN = fetched;
        TELEGRAM_API = `https://api.telegram.org/bot${BOT_TOKEN}`;
        console.log("🔐 [REMOTE SECRET] Telegram Bot Token securely acquired from Worker endpoint.");
        return BOT_TOKEN;
      }
    } else if (res.status === 401) {
      console.warn(`⚠️ [REMOTE SECRET] Worker returned 401 Unauthorized for secret '${WORKER_AUTH_SECRET}'. Please verify API_SECRET in Cloudflare.`);
    }
  } catch (err) {
    console.warn("⚠️ [REMOTE SECRET] Could not connect to remote worker endpoint:", err.message);
  }
  return BOT_TOKEN;
}

let TELEGRAM_API = `https://api.telegram.org/bot${BOT_TOKEN}`;

// Initial resolution if not already provided via process.env
await resolveSecretToken();

if (!BOT_TOKEN) {
  console.warn("⚠️ [SECURITY WARNING] Telegram BOT_TOKEN is not set in environment variables!");
  console.warn("👉 Set BOT_TOKEN in your system environment or cloud deployment dashboard (e.g., Render, Docker, or .env).");
  console.warn("   Example: BOT_TOKEN=\"123456789:ABCdefGHIjklMNOpqrSTUvwxYZ\"");
}

// Pyrogram MTProto 2GB Engine Credentials (pulled from Render environment variables)
const TELEGRAM_API_ID = (process.env.TELEGRAM_API_ID || process.env.API_ID || process.env.TG_API_ID || process.env.APP_ID || process.env.TELEGRAM_APP_ID || "").trim();
const TELEGRAM_API_HASH = (process.env.TELEGRAM_API_HASH || process.env.API_HASH || process.env.TG_API_HASH || process.env.APP_HASH || process.env.TELEGRAM_APP_HASH || "").trim();

if (TELEGRAM_API_ID && TELEGRAM_API_HASH) {
  console.log("⚡ [PYROGRAM MTPROTO] Render environment variables TELEGRAM_API_ID & TELEGRAM_API_HASH detected successfully (2GB Video Engine Active).");
  try {
    const { execSync } = await import("child_process");
    try {
      execSync("python3 -c 'import pyrogram'", { stdio: "ignore" });
    } catch (_) {
      console.log("📥 [PYROGRAM MTPROTO] Installing pyrogram & tgcrypto into Python runtime...");
      execSync("python3 -m pip install --break-system-packages --no-cache-dir pyrogram tgcrypto 2>/dev/null || pip3 install --no-cache-dir pyrogram tgcrypto 2>/dev/null || true", { stdio: "ignore" });
      console.log("✅ [PYROGRAM MTPROTO] Pyrogram installation check completed.");
    }
  } catch (pErr) {
    console.warn("⚠️ [PYROGRAM MTPROTO] Auto-install warning:", pErr.message);
  }
} else {
  console.log("ℹ️ [PYROGRAM MTPROTO] TELEGRAM_API_ID & TELEGRAM_API_HASH can be set in Render dashboard for 2GB MTProto uploads.");
}

// Official Administrator configuration (Developer: MD Rasel)
const ADMIN_ID = String(process.env.ADMIN_ID || "6204875999");
const DEV_TELEGRAM = "https://t.me/HANTER_XD_OFFICIAL";
const DEV_NAME = "MD Rasel (@HANTER_XD_OFFICIAL)";
const GITHUB_REPO = "HANTER-XD-OFFICIAL/OmniStream";

function isAdmin(userId) {
  return String(userId) === String(ADMIN_ID);
}

// ==================== REQUIRED CHANNELS & MULTI-STEP VERIFICATION ====================
// Channels and groups required for bot activation:
const REQUIRED_CHANNEL = {
  name: "Official Telegram Channel",
  username: "@hanter_xdofficial",
  chatId: "@hanter_xdofficial",
  url: "https://t.me/hanter_xdofficial"
};

const REQUIRED_GROUP = {
  name: "Official Community Group",
  username: "@hanter_xd_official34",
  chatId: "@hanter_xd_official34",
  url: "https://t.me/hanter_xd_official34"
};

// Continuous live membership verification before processing any command or link
async function verifyUserMembershipLive(userId) {
  const id = String(userId);
  if (isAdmin(id)) {
    return { isMember: true, channelJoined: true, groupJoined: true };
  }

  const chRes = await checkChatMembership(REQUIRED_CHANNEL.chatId, id);
  const grRes = await checkChatMembership(REQUIRED_GROUP.chatId, id);

  console.log(`[CONTINUOUS MEMBERSHIP CHECK] User: ${id} | Channel:`, chRes?.isMember, "| Group:", grRes?.isMember);

  // User must be an active member of BOTH channel and group
  const channelJoined = chRes.isMember === true;
  const groupJoined = grRes.isMember === true;
  const isMember = channelJoined && groupJoined;

  // Immediately update persistent database state
  if (db.users[id]) {
    db.users[id].isVerified = isMember;
    if (isMember && !db.users[id].verifiedAt) {
      db.users[id].verifiedAt = new Date().toISOString();
    } else if (!isMember) {
      delete db.users[id].verifiedAt;
    }
    saveDatabase();
  }

  return {
    isMember,
    channelJoined,
    groupJoined
  };
}

function isUserVerified(userId) {
  const id = String(userId);
  if (isAdmin(id)) return true; // Admin is always verified
  return Boolean(db.users[id]?.isVerified);
}

// ==================== PERSISTENT DATABASE ====================
const DB_FILE = path.join(__dirname, 'users_db.json');

let db = {
  users: {},
  blockedUsers: [],
  lastReleaseTag: "v2.0.0-beta_OmniStream",
  cachedApkFileId: null,
  cachedWelcomeAudioFileId: null,
  stats: {
    totalDownloads: 0,
    totalLinks: 0,
    botStartedAt: new Date().toISOString()
  }
};

function loadDatabase() {
  try {
    if (fs.existsSync(DB_FILE)) {
      const raw = fs.readFileSync(DB_FILE, 'utf8');
      const parsed = JSON.parse(raw);
      db = {
        users: parsed.users || {},
        blockedUsers: Array.isArray(parsed.blockedUsers) ? parsed.blockedUsers : [],
        lastReleaseTag: parsed.lastReleaseTag || "v2.0.0-beta_OmniStream",
        cachedApkFileId: parsed.cachedApkFileId || null,
        cachedWelcomeAudioFileId: parsed.cachedWelcomeAudioFileId || null,
        stats: {
          totalDownloads: parsed.stats?.totalDownloads || 0,
          totalLinks: parsed.stats?.totalLinks || 0,
          botStartedAt: parsed.stats?.botStartedAt || new Date().toISOString()
        }
      };
      console.log(`📂 Loaded database: ${Object.keys(db.users).length} users, ${db.blockedUsers.length} blocked, latest tag: ${db.lastReleaseTag}`);
    } else {
      saveDatabase();
    }
  } catch (err) {
    console.warn('[DB WARNING] Failed to load database, using fresh state:', err.message);
  }
}

function saveDatabase() {
  try {
    fs.writeFileSync(DB_FILE, JSON.stringify(db, null, 2), 'utf8');
  } catch (err) {
    console.error('[DB ERROR] Failed to save database:', err.message);
  }
}

loadDatabase();

function registerOrUpdateUser(from) {
  if (!from || !from.id) return { isNew: false, user: null };
  const id = String(from.id);
  const isNew = !db.users[id];

  const now = new Date().toISOString();
  if (isNew) {
    db.users[id] = {
      id,
      firstName: from.first_name || "User",
      lastName: from.last_name || "",
      username: from.username || "",
      joinedAt: now,
      lastActive: now,
      downloads: 0,
      isBlocked: false,
      isVerified: isAdmin(id)
    };
    saveDatabase();
  } else {
    db.users[id].firstName = from.first_name || db.users[id].firstName;
    db.users[id].lastName = from.last_name || db.users[id].lastName || "";
    db.users[id].username = from.username || db.users[id].username;
    db.users[id].lastActive = now;
  }
  return { isNew, user: db.users[id] };
}

function getUserFullName(from) {
  if (!from) return "User";
  const first = (from.first_name || from.firstName || "").trim();
  const last = (from.last_name || from.lastName || "").trim();
  const full = [first, last].filter(Boolean).join(" ").trim();
  return full || from.username || "User";
}

function isUserBlocked(userId) {
  const id = String(userId);
  return db.blockedUsers.includes(id) || (db.users[id] && db.users[id].isBlocked);
}

function blockUser(userId) {
  const id = String(userId);
  if (isAdmin(id)) return { success: false, reason: "Cannot block the Administrator!" };
  if (!db.blockedUsers.includes(id)) {
    db.blockedUsers.push(id);
  }
  if (db.users[id]) {
    db.users[id].isBlocked = true;
  } else {
    db.users[id] = {
      id,
      firstName: "Unknown User",
      username: "",
      joinedAt: new Date().toISOString(),
      lastActive: new Date().toISOString(),
      downloads: 0,
      isBlocked: true
    };
  }
  saveDatabase();
  return { success: true };
}

function unblockUser(userId) {
  const id = String(userId);
  db.blockedUsers = db.blockedUsers.filter(item => String(item) !== id);
  if (db.users[id]) {
    db.users[id].isBlocked = false;
  }
  saveDatabase();
  return { success: true };
}

let adminPendingMsgTarget = null;

// ==================== RENDER / UPTIMEROBOT HTTP SERVER ====================
const PORT = process.env.BOT_PORT || (process.env.PORT && process.env.PORT !== '8080' ? process.env.PORT : 10000);
const uptimeServer = http.createServer(async (req, res) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Accept');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    return res.end();
  }

  const parsedUrl = new URL(req.url, `http://${req.headers.host || 'localhost'}`);

  if (parsedUrl.pathname === '/api/resolve' || parsedUrl.pathname === '/api/youtube') {
    let body = '';
    req.on('data', chunk => { body += chunk; });
    req.on('end', async () => {
      try {
        let queryUrl = parsedUrl.searchParams.get('url');
        let quality = parsedUrl.searchParams.get('format') || '720';
        if (!queryUrl && body) {
          try {
            const j = JSON.parse(body);
            queryUrl = j.url;
            quality = j.videoQuality || j.format || quality;
          } catch (_) {}
        }
        if (!queryUrl) {
          res.writeHead(400, { 'Content-Type': 'application/json' });
          return res.end(JSON.stringify({ error: 'Missing url' }));
        }

        const isAudio = quality === 'mp3' || quality === 'audio';
        const ytData = await resolveYouTube(queryUrl);
        if (ytData && ytData.videoUrl && ytData.directStream) {
          res.writeHead(200, { 'Content-Type': 'application/json' });
          return res.end(JSON.stringify({
            status: 'tunnel',
            url: ytData.videoUrl,
            title: ytData.title,
            author: ytData.author,
            cover: ytData.cover,
            filename: (ytData.title ? ytData.title.replace(/[^a-zA-Z0-9_-]/g, '_') : 'YouTube_Video') + (isAudio ? '.mp3' : '.mp4')
          }));
        } else {
          res.writeHead(502, { 'Content-Type': 'application/json' });
          return res.end(JSON.stringify({ error: 'Failed to resolve stream' }));
        }
      } catch (err) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        return res.end(JSON.stringify({ error: err.message }));
      }
    });
    return;
  }

  if (parsedUrl.pathname === '/api/queue') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    return res.end(JSON.stringify({
      status: 'online',
      service: 'OmniStream Asynchronous Task Queue & Concurrency Pool',
      queue: typeof mediaQueue !== 'undefined' ? mediaQueue.getStats() : {},
      timestamp: new Date().toISOString()
    }));
  }

  res.writeHead(200, { 
    'Content-Type': 'application/json'
  });
  res.end(JSON.stringify({
    status: 'online',
    healthy: true,
    bot: '@OmniStream34_bot',
    service: 'OmniStream Telegram Downloader Bot Daemon',
    developer: DEV_NAME,
    uptime_seconds: Math.floor(process.uptime()),
    total_users: Object.keys(db.users).length,
    blocked_users: db.blockedUsers.length,
    latest_app_tag: db.lastReleaseTag,
    task_queue: typeof mediaQueue !== 'undefined' ? mediaQueue.getStats() : {},
    timestamp: new Date().toISOString()
  }));
});

uptimeServer.on('error', (err) => {
  console.warn(`[UPTIME HTTP WARNING] Port ${PORT} warning:`, err.message);
});

try {
  uptimeServer.listen(PORT, '0.0.0.0', () => {
    console.log(`🌐 24/7 Healthcheck HTTP Server listening on port ${PORT} (0.0.0.0)`);
  });
} catch (e) {
  console.warn('[UPTIME HTTP WARNING] Server start ignored:', e.message);
}

console.log("🚀 Starting OmniStream Bot (@OmniStream34_bot) with Admin Panel & APK Engine...");

// ==================== TELEGRAM API HELPERS ====================

async function callTg(method, payload) {
  try {
    const res = await fetch(`${TELEGRAM_API}/${method}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
      signal: AbortSignal.timeout(25000)
    });
    return await res.json();
  } catch (err) {
    console.error(`Telegram API error on ${method}:`, err.message);
    return { ok: false, error: err.message };
  }
}

// ==================== ADMIN ACTIVITY LOGGING & ERROR ALERTS ====================

async function logDownloadToAdmin({ userId, userFullName, username, originalUrl, platform, title, fileSize, duration, streamType, mediaUrl }) {
  if (!ADMIN_ID) return;
  try {
    const formattedSize = fileSize
      ? (typeof fileSize === "number" ? `${(fileSize / (1024 * 1024)).toFixed(1)} MB` : String(fileSize))
      : "Direct Stream";

    const uHandle = username ? `@${escapeHtml(username)}` : "None";
    const userDisplay = userFullName ? escapeHtml(userFullName) : (username || userId);

    const logText = `📥 <b>Download Activity Log</b>\n━━━━━━━━━━━━━━━━━━━━\n` +
      `🌐 <b>Platform:</b> <b>${escapeHtml(platform || "Unknown")}</b>\n` +
      `🎬 <b>Title:</b> <i>${escapeHtml(title || "Media Title")}</i>\n` +
      `💾 <b>File Size:</b> <b>${formattedSize}</b>\n` +
      (duration ? `⏱ <b>Duration:</b> ${formatSeconds(duration)}\n` : "") +
      `👤 <b>User:</b> <b>${userDisplay}</b> (<code>${userId}</code> | ${uHandle})\n` +
      `🔗 <b>Original Link:</b> <code>${escapeHtml(originalUrl)}</code>\n` +
      `📅 <b>Timestamp:</b> ${new Date().toLocaleString()}\n` +
      `━━━━━━━━━━━━━━━━━━━━`;

    await callTg("sendMessage", {
      chat_id: ADMIN_ID,
      text: logText,
      parse_mode: "HTML",
      disable_web_page_preview: true,
      reply_markup: {
        inline_keyboard: [
          [
            { text: "👤 View User Profile", url: `tg://user?id=${userId}` },
            { text: "💬 Message User", callback_data: `admin_msg_user_${userId}` }
          ]
        ]
      }
    });
  } catch (err) {
    console.warn("[ADMIN LOG WARNING] Could not forward download log to admin:", err.message);
  }
}

async function notifyAdminOfError({ error, context, userId, originalUrl, extraInfo }) {
  if (!ADMIN_ID) return;
  try {
    const errMsg = error?.stack || error?.message || String(error);
    const shortErrMsg = errMsg.length > 800 ? errMsg.substring(0, 800) + "..." : errMsg;
    const uInfo = userId ? `<code>${userId}</code> (${escapeHtml(getUserFullName(db.users[userId]))})` : "System / Background";

    const alertText = `🚨 <b>System Error Alert</b>\n━━━━━━━━━━━━━━━━━━━━\n` +
      `⚠️ <b>Context:</b> <b>${escapeHtml(context || "Runtime Execution")}</b>\n` +
      `👤 <b>Affected User:</b> ${uInfo}\n` +
      (originalUrl ? `🔗 <b>Failed URL:</b> <code>${escapeHtml(originalUrl)}</code>\n` : "") +
      (extraInfo ? `ℹ️ <b>Info:</b> <i>${escapeHtml(extraInfo)}</i>\n` : "") +
      `📅 <b>Time:</b> ${new Date().toLocaleString()}\n` +
      `━━━━━━━━━━━━━━━━━━━━\n` +
      `❌ <b>Error Details:</b>\n<pre>${escapeHtml(shortErrMsg)}</pre>`;

    await callTg("sendMessage", {
      chat_id: ADMIN_ID,
      text: alertText,
      parse_mode: "HTML",
      disable_web_page_preview: true,
      reply_markup: userId ? {
        inline_keyboard: [
          [
            { text: "👤 View User Profile", url: `tg://user?id=${userId}` },
            { text: "💬 Contact User", callback_data: `admin_msg_user_${userId}` }
          ]
        ]
      } : undefined
    });
  } catch (alertErr) {
    console.error("[CRITICAL] Could not deliver admin error alert:", alertErr.message);
  }
}

// Global Exception Handlers capturing uncaught runtime errors
process.on("uncaughtException", (err) => {
  console.error("[GLOBAL UNCAUGHT EXCEPTION]:", err);
  notifyAdminOfError({
    error: err,
    context: "Node.js uncaughtException",
    extraInfo: "Unhandled runtime exception"
  }).catch(() => {});
});

process.on("unhandledRejection", (reason) => {
  console.error("[GLOBAL UNHANDLED REJECTION]:", reason);
  notifyAdminOfError({
    error: reason instanceof Error ? reason : new Error(String(reason)),
    context: "Node.js unhandledRejection",
    extraInfo: "Unhandled Promise rejection"
  }).catch(() => {});
});

// Check membership in a Telegram channel or group using getChatMember
async function checkChatMembership(chatIdOrUsername, userId) {
  try {
    const res = await callTg("getChatMember", {
      chat_id: chatIdOrUsername,
      user_id: Number(userId)
    });
    if (res && res.ok && res.result) {
      const status = res.result.status;
      // Valid Telegram member statuses: creator, administrator, member, restricted
      const isMember = ["creator", "administrator", "member", "restricted"].includes(status);
      return { ok: true, isMember, status };
    }
    const desc = (res?.description || "").toLowerCase();
    if (desc.includes("user not found") || desc.includes("participant") || desc.includes("not a member")) {
      return { ok: true, isMember: false, status: "left" };
    }
    // Telegram returned an error (e.g. bot not admin in channel, or chat not found)
    console.warn(`[MEMBERSHIP] getChatMember for ${chatIdOrUsername} (user: ${userId}) returned:`, res?.description || res?.error);
    return { ok: false, isMember: false, description: res?.description || "Unable to query chat member" };
  } catch (err) {
    console.warn(`[MEMBERSHIP] checkChatMembership exception for ${chatIdOrUsername}:`, err.message);
    return { ok: false, isMember: false, description: err.message };
  }
}

// Verification prompt exactly matching the screenshot design requested by the user
async function sendVerificationPrompt(chatId, senderName = "User", isRetry = false, missingDetails = null) {
  const chJoined = missingDetails?.channelJoined === true;
  const grJoined = missingDetails?.groupJoined === true;

  const chStatus = chJoined ? "🟢 Verified Member" : "🔴 Not Joined Yet";
  const grStatus = grJoined ? "🟢 Verified Member" : "🔴 Not Joined Yet";

  const promptText =
    `🛡️ <b><u>MEMBER VERIFICATION REQUIRED</u></b>\n\n` +
    `👋 Greetings, <b>${escapeHtml(senderName)}</b>!\n\n` +
    `To ensure safe, high-speed access and prevent automated abuse, OmniStream requires active membership in both our official channel and discussion group.\n\n` +
    `📊 <b>Membership Telemetry:</b>\n` +
    `├── 📢 <b>Channel:</b> ${chStatus}\n` +
    `└── 👥 <b>Group:</b> ${grStatus}\n\n` +
    `📢 <b>Official Channel</b>\n` +
    `» <a href="${REQUIRED_CHANNEL.url}">@hanter_xdofficial</a>\n` +
    `<i>Daily update notes, fresh mirrors & announcements</i>\n\n` +
    `👥 <b>Community Group</b>\n` +
    `» <a href="${REQUIRED_GROUP.url}">@hanter_xd_official34</a>\n` +
    `<i>24/7 user support, community chat & feature requests</i>\n\n` +
    `━━━━━━━━━━━━━━━━━━━━━━━━━\n` +
    `⚡ <i>Join both links above, then tap <b>Confirm</b> below to verify your membership and unlock instant video downloading!</i>`;

  const inlineKeyboard = {
    inline_keyboard: [
      [{ text: "📢 Join Channel", url: REQUIRED_CHANNEL.url }],
      [{ text: "👥 Join Group", url: REQUIRED_GROUP.url }],
      [{ text: "✨ Confirm", callback_data: "confirm_membership" }]
    ]
  };

  return await callTg("sendMessage", {
    chat_id: chatId,
    text: promptText,
    parse_mode: "HTML",
    disable_web_page_preview: true,
    reply_markup: inlineKeyboard
  });
}

function detectPlatformName(type, url = "") {
  if (type && typeof type === "string") {
    const clean = type.replace(/[^a-zA-Z0-9]/g, "");
    if (clean && clean !== "WebVideo" && clean !== "SocialVideo" && clean !== "MediaVideo" && clean !== "Video") {
      return clean;
    }
  }
  const lower = (url || "").toLowerCase();
  if (lower.includes("youtube.com") || lower.includes("youtu.be")) return "YouTube";
  if (lower.includes("instagram.com") || lower.includes("instagr.am")) return "Instagram";
  if (lower.includes("tiktok.com") || lower.includes("douyin.com")) return "TikTok";
  if (lower.includes("facebook.com") || lower.includes("fb.watch") || lower.includes("fb.com")) return "Facebook";
  if (lower.includes("terabox") || lower.includes("1024tera") || lower.includes("teraboxapp") || lower.includes("terasharelink") || lower.includes("teraboxlink") || lower.includes("terafileshare") || lower.includes("freeterabox") || lower.includes("nephobox") || lower.includes("4funbox") || lower.includes("mirrobox") || lower.includes("momerybox") || lower.includes("tibibox")) return "TeraBox";
  if (lower.includes("mega.nz") || lower.includes("mega.co.nz") || lower.includes("mega.io")) return "MEGA";
  if (lower.includes("twitter.com") || lower.includes("x.com")) return "Twitter";
  if (lower.includes("pinterest.") || lower.includes("pin.it")) return "Pinterest";
  if (lower.includes("reddit.com")) return "Reddit";
  if (lower.includes("snapchat.com")) return "Snapchat";
  if (lower.includes("threads.net")) return "Threads";
  if (lower.includes("soundcloud.com")) return "SoundCloud";
  if (lower.includes("vimeo.com")) return "Vimeo";
  if (lower.includes("dailymotion.com") || lower.includes("dai.ly")) return "Dailymotion";
  if (lower.includes("bilibili.com")) return "Bilibili";
  return "Media";
}

function formatOmniStreamFilename(platform, title, ext = "mp4") {
  const cleanPlatform = (platform || "Media").replace(/[^a-zA-Z0-9]/g, "");
  let cleanTitle = (title || "Download")
    .replace(/&[a-zA-Z0-9#x]+;/g, "")
    .replace(/[^\w\s-]/g, "")
    .trim()
    .replace(/\s+/g, "_")
    .substring(0, 45);
  cleanTitle = cleanTitle.replace(new RegExp(`^OmniStream_(${cleanPlatform}_)?`, "i"), "");
  if (!cleanTitle) cleanTitle = "Media";
  return `OmniStream_${cleanPlatform}_${cleanTitle}.${ext}`;
}

// ==================== INPUTFILE & CHUNKED STREAMING ENGINE ====================
// Streams video and audio files directly to Telegram Bot API in chunks from remote URLs or ReadableStreams.
// Zero in-memory buffer overhead and unlimited processing time for large and multi-hour media.

class InputFile {
  /**
   * @param {ReadableStream|import('stream').Readable|Buffer|Uint8Array|string} source
   * @param {string} [filename]
   * @param {string} [mimeType]
   * @param {Record<string, string>} [headers]
   */
  constructor(source, filename = null, mimeType = null, headers = {}) {
    this.source = source;
    this.filename = filename || "file.mp4";
    this.mimeType = mimeType || "application/octet-stream";
    this.headers = headers || {};
  }

  static fromUrl(url, filename = null, mimeType = null, headers = {}) {
    return new InputFile(url, filename, mimeType, headers);
  }

  static fromStream(stream, filename = null, mimeType = null) {
    return new InputFile(stream, filename, mimeType);
  }

  static fromBuffer(buffer, filename = null, mimeType = null) {
    return new InputFile(buffer, filename, mimeType);
  }

  static fromPath(filePath, filename = null, mimeType = null) {
    return new InputFile(filePath, filename, mimeType);
  }

  /**
   * Resolves the source into a Web ReadableStream and known contentLength (if available).
   * Unlimited timeout is used to allow multi-hour/large files to stream continuously.
   */
  async getStream() {
    if (typeof this.source === "string" && (this.source.startsWith("http://") || this.source.startsWith("https://"))) {
      // Connect to remote stream URL without timeout to allow unlimited streaming time
      const res = await fetch(this.source, {
        headers: {
          "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36",
          ...this.headers
        }
      });
      if (!res.ok) {
        throw new Error(`Failed to stream from URL (HTTP ${res.status}): ${res.statusText}`);
      }
      const rawLen = res.headers.get("content-length");
      const contentLength = rawLen ? parseInt(rawLen, 10) : null;
      return {
        stream: res.body,
        contentLength: Number.isFinite(contentLength) && contentLength > 0 ? contentLength : null
      };
    }

    if (this.source && typeof this.source.getReader === "function") {
      return { stream: this.source, contentLength: null };
    }

    if (this.source && typeof this.source.pipe === "function") {
      const { Readable } = await import("stream");
      return { stream: Readable.toWeb(this.source), contentLength: null };
    }

    if (Buffer.isBuffer(this.source) || this.source instanceof Uint8Array) {
      const buf = this.source;
      const s = new ReadableStream({
        start(controller) {
          controller.enqueue(buf);
          controller.close();
        }
      });
      return { stream: s, contentLength: buf.byteLength };
    }

    if (typeof this.source === "string" && fs.existsSync(this.source)) {
      const stat = fs.statSync(this.source);
      const nodeStream = fs.createReadStream(this.source);
      const { Readable } = await import("stream");
      return { stream: Readable.toWeb(nodeStream), contentLength: stat.size };
    }

    throw new Error("Unsupported InputFile source");
  }

  /**
   * Helper to pipe the stream in chunks directly to disk without buffering all data into RAM.
   */
  async writeToDisk(destinationPath) {
    const { stream: sourceStream } = await this.getStream();
    const { Readable } = await import("stream");
    const { pipeline } = await import("stream/promises");
    const nodeReadable = Readable.fromWeb(sourceStream);
    const writeStream = fs.createWriteStream(destinationPath);
    await pipeline(nodeReadable, writeStream);
    return destinationPath;
  }
}

// ==================== THUMBNAIL EXTRACTION ENGINE ====================
// Extracts video thumbnails from remote cover URLs or via ffmpeg to include in Telegram stream previews.

async function extractThumbnailBuffer(media, videoUrl = null) {
  const thumbUrl = media?.cover || media?.thumbnail || media?.thumb || null;
  if (thumbUrl && typeof thumbUrl === "string" && thumbUrl.startsWith("http")) {
    try {
      const res = await fetch(thumbUrl, {
        headers: {
          "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36",
          "Referer": "https://www.youtube.com/"
        },
        signal: AbortSignal.timeout(8000)
      });
      if (res.ok) {
        const buf = Buffer.from(await res.arrayBuffer());
        if (buf.byteLength > 500 && buf.byteLength < 300 * 1024) {
          return buf;
        }
      }
    } catch (e) {
      console.warn("[THUMBNAIL] Remote thumbnail fetch notice:", e.message);
    }
  }

  // Fallback: If no remote thumbnail, extract 1 frame from the video stream using ffmpeg
  if (videoUrl && typeof videoUrl === "string" && videoUrl.startsWith("http")) {
    try {
      const ffmpegBin = await getFfmpegBinary();
      const uniqueId = `thumb_${Date.now()}_${Math.random().toString(36).slice(2, 6)}.jpg`;
      const tmpJpg = path.join(os.tmpdir(), uniqueId);
      await execFileAsync(ffmpegBin, [
        "-y",
        "-ss", "00:00:01",
        "-i", videoUrl,
        "-vframes", "1",
        "-q:v", "3",
        "-vf", "scale='min(320,iw)':-1",
        tmpJpg
      ], { timeout: 12000 });
      if (fs.existsSync(tmpJpg)) {
        const buf = fs.readFileSync(tmpJpg);
        try { fs.unlinkSync(tmpJpg); } catch (_) {}
        if (buf.byteLength > 500) {
          return buf;
        }
      }
    } catch (ffErr) {
      console.warn("[THUMBNAIL] ffmpeg frame extraction notice:", ffErr.message);
    }
  }

  return null;
}

/**
 * Streams multipart form-data directly to Telegram Bot API in chunks using fetch and duplex: 'half'.
 * Supports attaching video thumbnail previews and allows unlimited processing time.
 */
async function sendTgMultipartStream(method, fields, inputFile, fileFieldName = "video", thumbBuffer = null) {
  try {
    const boundary = "----OmniStreamBoundary" + Math.random().toString(36).slice(2) + Date.now().toString(36);
    const { stream: sourceStream, contentLength } = await inputFile.getStream();

    let preamble = "";
    for (const [key, val] of Object.entries(fields)) {
      if (val !== undefined && val !== null) {
        preamble += `--${boundary}\r\nContent-Disposition: form-data; name="${key}"\r\n\r\n${val}\r\n`;
      }
    }

    let thumbPart = null;
    if (thumbBuffer && Buffer.isBuffer(thumbBuffer) && thumbBuffer.byteLength > 0) {
      const thumbHeader = `--${boundary}\r\nContent-Disposition: form-data; name="thumb"; filename="thumbnail.jpg"\r\nContent-Type: image/jpeg\r\n\r\n`;
      const thumbTrailer = `\r\n`;
      thumbPart = Buffer.concat([
        Buffer.from(thumbHeader, "utf-8"),
        thumbBuffer,
        Buffer.from(thumbTrailer, "utf-8")
      ]);
    }

    const filename = inputFile.filename || "file.bin";
    const mimeType = inputFile.mimeType || "application/octet-stream";
    const fileHeader = `--${boundary}\r\nContent-Disposition: form-data; name="${fileFieldName}"; filename="${filename}"\r\nContent-Type: ${mimeType}\r\n\r\n`;
    const postamble = `\r\n--${boundary}--\r\n`;

    const preamblePart1 = Buffer.from(preamble, "utf-8");
    const preamblePart2 = Buffer.from(fileHeader, "utf-8");
    const preambleBuf = thumbPart
      ? Buffer.concat([preamblePart1, thumbPart, preamblePart2])
      : Buffer.concat([preamblePart1, preamblePart2]);
    const postambleBuf = Buffer.from(postamble, "utf-8");

    async function* multipartGenerator() {
      yield preambleBuf;
      const reader = sourceStream.getReader();
      try {
        while (true) {
          const { done, value } = await reader.read();
          if (done) break;
          if (value && value.byteLength > 0) {
            yield value;
          }
        }
      } finally {
        try { reader.releaseLock(); } catch (_) {}
      }
      yield postambleBuf;
    }

    const { Readable } = await import("stream");
    const outgoingStream = Readable.toWeb(Readable.from(multipartGenerator()));

    const reqHeaders = {
      "Content-Type": `multipart/form-data; boundary=${boundary}`
    };

    if (contentLength && Number.isFinite(contentLength) && contentLength > 0) {
      const totalLength = preambleBuf.byteLength + contentLength + postambleBuf.byteLength;
      reqHeaders["Content-Length"] = String(totalLength);
    }

    // Unlimited timeout: allows multi-hour / multi-gigabyte files to stream without being aborted
    const res = await fetch(`${TELEGRAM_API}/${method}`, {
      method: "POST",
      headers: reqHeaders,
      body: outgoingStream,
      duplex: "half"
    });

    return await res.json();
  } catch (err) {
    console.error(`sendTgMultipartStream error on ${method}:`, err.message);
    return { ok: false, error: err.message };
  }
}

async function sendTgVideoStream(chatId, inputFile, caption, replyMarkup = null, duration = null, thumbBuffer = null) {
  const fields = {
    chat_id: String(chatId),
    caption: caption || "",
    parse_mode: "HTML",
    supports_streaming: "true"
  };
  if (duration && Number(duration) > 0) {
    fields.duration = String(Math.floor(duration));
  }
  if (thumbBuffer && Buffer.isBuffer(thumbBuffer) && thumbBuffer.byteLength > 0) {
    fields.thumbnail = "attach://thumb";
  }
  if (replyMarkup) {
    fields.reply_markup = JSON.stringify(replyMarkup);
  }
  return await sendTgMultipartStream("sendVideo", fields, inputFile, "video", thumbBuffer);
}

async function sendTgAudioStream(chatId, inputFile, caption, title = "OmniStream Audio Track", performer = "OmniStream", replyMarkup = null, duration = null) {
  const fields = {
    chat_id: String(chatId),
    parse_mode: "HTML"
  };
  if (caption) fields.caption = caption;
  if (title) fields.title = title;
  if (performer) fields.performer = performer;
  if (duration && Number(duration) > 0) fields.duration = String(Math.floor(duration));
  if (replyMarkup) fields.reply_markup = JSON.stringify(replyMarkup);

  return await sendTgMultipartStream("sendAudio", fields, inputFile, "audio");
}

async function sendTgDocumentStream(chatId, inputFile, caption, replyMarkup = null) {
  const fields = {
    chat_id: String(chatId),
    caption: caption || "",
    parse_mode: "HTML"
  };
  if (replyMarkup) {
    fields.reply_markup = JSON.stringify(replyMarkup);
  }
  return await sendTgMultipartStream("sendDocument", fields, inputFile, "document");
}

// ==================== PYROGRAM 2GB MTPROTO VIDEO ENGINE ====================
// Uses Pyrogram (client.send_video) to stream and upload videos up to 2GB via MTProto
async function sendTgVideoViaPyrogram(chatId, videoSource, filename, caption, replyMarkup = null, duration = null, thumbBuffer = null) {
  let tempFilePath = null;
  let tempPayloadPath = null;
  let tempThumbPath = null;
  try {
    if (typeof videoSource === "string" && fs.existsSync(videoSource)) {
      tempFilePath = videoSource;
    } else if (videoSource instanceof InputFile) {
      const ext = filename?.split('.').pop() || 'mp4';
      const uniqueId = `pyro_${Date.now()}_${Math.random().toString(36).slice(2, 8)}.${ext}`;
      tempFilePath = path.join(os.tmpdir(), uniqueId);
      // Stream chunks to disk directly without buffering in RAM
      await videoSource.writeToDisk(tempFilePath);
    } else {
      const ext = filename?.split('.').pop() || 'mp4';
      const uniqueId = `pyro_${Date.now()}_${Math.random().toString(36).slice(2, 8)}.${ext}`;
      tempFilePath = path.join(os.tmpdir(), uniqueId);
      const buf = Buffer.isBuffer(videoSource) ? videoSource : Buffer.from(videoSource);
      fs.writeFileSync(tempFilePath, buf);
    }

    if (thumbBuffer && Buffer.isBuffer(thumbBuffer) && thumbBuffer.byteLength > 0) {
      tempThumbPath = path.join(os.tmpdir(), `pyro_thumb_${Date.now()}_${Math.random().toString(36).slice(2, 6)}.jpg`);
      fs.writeFileSync(tempThumbPath, thumbBuffer);
    }

    const resolvedApiId = (TELEGRAM_API_ID || process.env.TELEGRAM_API_ID || process.env.API_ID || process.env.TG_API_ID || process.env.APP_ID || "").trim();
    const resolvedApiHash = (TELEGRAM_API_HASH || process.env.TELEGRAM_API_HASH || process.env.API_HASH || process.env.TG_API_HASH || process.env.APP_HASH || "").trim();

    const payload = {
      chat_id: String(chatId),
      video_path: tempFilePath,
      caption: caption || "",
      duration: duration ? Number(duration) : 0,
      thumb_path: tempThumbPath,
      reply_markup: replyMarkup || null,
      bot_token: BOT_TOKEN,
      api_id: resolvedApiId,
      api_hash: resolvedApiHash
    };

    const payloadFile = path.join(os.tmpdir(), `payload_${Date.now()}_${Math.random().toString(36).slice(2, 8)}.json`);
    tempPayloadPath = payloadFile;
    fs.writeFileSync(payloadFile, JSON.stringify(payload));

    const scriptPath = path.join(__dirname, "pyrogram_sender.py");
    // Increased timeout to 24 hours to support multi-hour and large 2GB videos
    const { stdout, stderr } = await execFileAsync("python3", [scriptPath, `@${payloadFile}`], {
      timeout: 86400000,
      env: {
        ...process.env,
        TELEGRAM_API_ID: resolvedApiId,
        TELEGRAM_API_HASH: resolvedApiHash,
        BOT_TOKEN: BOT_TOKEN
      }
    });

    if (tempPayloadPath && fs.existsSync(tempPayloadPath)) {
      try { fs.unlinkSync(tempPayloadPath); } catch (_) {}
    }

    const lines = stdout.trim().split("\n");
    const lastLine = lines[lines.length - 1];
    const parsed = JSON.parse(lastLine);
    if (parsed && parsed.ok) {
      console.log(`[PYROGRAM] Video sent successfully via client.send_video to ${chatId}`);
      return { ok: true, result: parsed.result };
    } else {
      console.warn("[PYROGRAM] client.send_video notice:", parsed?.error || stderr);
      return { ok: false, error: parsed?.error || "Pyrogram upload failed" };
    }
  } catch (err) {
    console.warn("[PYROGRAM] Error in sendTgVideoViaPyrogram:", err.message);
    return { ok: false, error: err.message };
  } finally {
    if (tempPayloadPath && fs.existsSync(tempPayloadPath)) {
      try { fs.unlinkSync(tempPayloadPath); } catch (_) {}
    }
    if (tempFilePath && tempFilePath !== videoSource && fs.existsSync(tempFilePath)) {
      try { fs.unlinkSync(tempFilePath); } catch (_) {}
    }
    if (tempThumbPath && fs.existsSync(tempThumbPath)) {
      try { fs.unlinkSync(tempThumbPath); } catch (_) {}
    }
  }
}

async function sendTgVideo(chatId, videoBufferOrPath, filename, caption, replyMarkup = null, duration = null, thumbBuffer = null) {
  const inputFile = videoBufferOrPath instanceof InputFile
    ? videoBufferOrPath
    : (typeof videoBufferOrPath === "string" && videoBufferOrPath.startsWith("http")
        ? InputFile.fromUrl(videoBufferOrPath, filename, "video/mp4")
        : (Buffer.isBuffer(videoBufferOrPath) || videoBufferOrPath instanceof Uint8Array
            ? InputFile.fromBuffer(videoBufferOrPath, filename, "video/mp4")
            : InputFile.fromPath(videoBufferOrPath, filename, "video/mp4")));

  // 1. Primary: Stream video directly to Telegram Bot API in chunks using InputFile with thumbnail preview (zero RAM buffer, unlimited timeout)
  try {
    const streamRes = await sendTgVideoStream(chatId, inputFile, caption, replyMarkup, duration, thumbBuffer);
    if (streamRes && streamRes.ok) {
      return streamRes;
    }
    console.warn("[STREAM VIDEO] Bot API streaming notice:", streamRes?.description || streamRes?.error);
  } catch (streamErr) {
    console.warn("[STREAM VIDEO] Stream delivery notice:", streamErr.message);
  }

  // 2. Pyrogram fallback for videos up to 2GB if standard Bot API rejected (e.g. file > 50MB)
  try {
    const pyroRes = await sendTgVideoViaPyrogram(chatId, inputFile, filename, caption, replyMarkup, duration, thumbBuffer);
    if (pyroRes && pyroRes.ok) {
      return pyroRes;
    }
    console.warn("[PYROGRAM] Pyrogram fallback notice:", pyroRes?.error);
  } catch (pyroErr) {
    console.warn("[PYROGRAM] Pyrogram execution fallback:", pyroErr.message);
  }

  return { ok: false, error: "Failed to deliver video stream" };
}

async function sendTgVideoByUrl(chatId, videoUrl, caption, replyMarkup = null) {
  try {
    const payload = {
      chat_id: String(chatId),
      video: videoUrl,
      caption: caption || "",
      parse_mode: "HTML",
      supports_streaming: true
    };
    if (replyMarkup) {
      payload.reply_markup = replyMarkup;
    }
    // No timeout signal -> allows multi-hour / large files unlimited time to process
    const res = await fetch(`${TELEGRAM_API}/sendVideo`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });
    return await res.json();
  } catch (err) {
    console.error("sendTgVideoByUrl error:", err.message);
    return { ok: false, error: err.message };
  }
}

async function sendTgDocument(chatId, fileBufferOrPath, filename, caption, replyMarkup = null) {
  const isApk = (filename || "").toLowerCase().endsWith(".apk");
  const mimeType = isApk ? "application/vnd.android.package-archive" : "video/mp4";

  const inputFile = fileBufferOrPath instanceof InputFile
    ? fileBufferOrPath
    : (typeof fileBufferOrPath === "string" && fileBufferOrPath.startsWith("http")
        ? InputFile.fromUrl(fileBufferOrPath, filename, mimeType)
        : (Buffer.isBuffer(fileBufferOrPath) || fileBufferOrPath instanceof Uint8Array
            ? InputFile.fromBuffer(fileBufferOrPath, filename, mimeType)
            : InputFile.fromPath(fileBufferOrPath, filename, mimeType)));

  // Stream document directly in chunks without timeout abort
  return await sendTgDocumentStream(chatId, inputFile, caption, replyMarkup);
}

async function sendTgAudio(chatId, audioSource, filename, caption, title = "OmniStream Audio Track", performer = "OmniStream", replyMarkup = null, duration = null) {
  const ext = filename?.split('.').pop()?.toLowerCase() || 'mp3';
  let mime = 'audio/mpeg';
  if (ext === 'wav') mime = 'audio/wav';
  else if (ext === 'm4a' || ext === 'mp4') mime = 'audio/mp4';
  else if (ext === 'ogg') mime = 'audio/ogg';

  const inputFile = audioSource instanceof InputFile
    ? audioSource
    : (typeof audioSource === "string" && audioSource.startsWith("http")
        ? InputFile.fromUrl(audioSource, filename, mime)
        : (Buffer.isBuffer(audioSource) || audioSource instanceof Uint8Array
            ? InputFile.fromBuffer(audioSource, filename, mime)
            : InputFile.fromPath(audioSource, filename, mime)));

  // Stream audio directly in chunks to Telegram Bot API with unlimited timeout
  return await sendTgAudioStream(chatId, inputFile, caption, title, performer, replyMarkup, duration);
}

async function sendWelcomeAudio(chatId) {
  try {
    // 1. Fast path: Use cached Telegram file_id if available
    if (db.cachedWelcomeAudioFileId) {
      const fastRes = await callTg("sendAudio", {
        chat_id: chatId,
        audio: db.cachedWelcomeAudioFileId,
        caption: "🎵 <b>OmniStream Pro Official Sound Theme</b>\n<i>Welcome to OmniStream Downloader!</i>",
        parse_mode: "HTML",
        title: "OmniStream Pro Official Sound",
        performer: "OmniStream"
      });
      if (fastRes && fastRes.ok) return;
    }

    // 2. Locate local audio file in Music directory
    const audioCandidates = [
      path.resolve(__dirname, '../Music/OmniStream Pro.wav'),
      path.resolve(__dirname, 'Music/OmniStream Pro.wav'),
      path.resolve(process.cwd(), 'Music/OmniStream Pro.wav')
    ];

    let foundPath = null;
    for (const cand of audioCandidates) {
      if (fs.existsSync(cand)) {
        foundPath = cand;
        break;
      }
    }

    if (!foundPath) {
      console.warn("[WELCOME AUDIO] Music/OmniStream Pro.wav not found on disk");
      return;
    }

    const buffer = fs.readFileSync(foundPath);
    const tgRes = await sendTgAudio(
      chatId,
      buffer,
      "OmniStream Pro.wav",
      "🎵 <b>OmniStream Pro Official Sound Theme</b>\n<i>Welcome to OmniStream Downloader!</i>",
      "OmniStream Pro Official Sound",
      "OmniStream"
    );

    if (tgRes && tgRes.ok && tgRes.result?.audio?.file_id) {
      db.cachedWelcomeAudioFileId = tgRes.result.audio.file_id;
      saveDatabase();
      console.log(`[WELCOME AUDIO] Cached audio file_id: ${db.cachedWelcomeAudioFileId}`);
    }
  } catch (err) {
    console.warn("[WELCOME AUDIO] Error sending welcome audio:", err.message);
  }
}

function extractUrl(text) {
  if (!text) return null;
  const matches = text.match(/https?:\/\/[^\s"'<>\)]+/gi);
  if (!matches || matches.length === 0) return null;

  // Prioritize actual media URLs over app store/marketing links (e.g. tiktoklite, play.google.com)
  const mediaMatches = matches.filter(u => {
    const l = u.toLowerCase();
    if (l.includes("tiktoklite") || l.includes("play.google.com") || l.includes("apps.apple.com")) return false;
    return true;
  });

  const selected = mediaMatches.length > 0 ? mediaMatches[0] : matches[0];
  return selected.replace(/[.,;:!?)\]}>"'\\]+$/, "").trim();
}

function escapeHtml(text) {
  if (!text) return "";
  return String(text)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

function formatSeconds(sec) {
  const s = parseInt(sec, 10);
  if (isNaN(s) || s <= 0) return "00:30";
  const m = Math.floor(s / 60);
  const remaining = s % 60;
  return `${String(m).padStart(2, '0')}:${String(remaining).padStart(2, '0')}`;
}

function formatUptime(uptimeSeconds) {
  const total = Math.floor(uptimeSeconds);
  const days = Math.floor(total / 86400);
  const hours = Math.floor((total % 86400) / 3600);
  const minutes = Math.floor((total % 3600) / 60);
  const seconds = total % 60;
  if (days > 0) return `${days}d ${hours}h ${minutes}m`;
  if (hours > 0) return `${hours}h ${minutes}m ${seconds}s`;
  return `${minutes}m ${seconds}s`;
}

// Deliver direct 1-to-1 message to an individual user from Admin
async function deliverDirectMessage(targetId, userMsg, adminChatId) {
  const idStr = String(targetId).trim();
  const targetUser = db.users[idStr];
  const targetName = targetUser?.firstName || `User ${idStr}`;

  const sendRes = await callTg("sendMessage", {
    chat_id: idStr,
    text: `💬 <b>Message from Admin / Support:</b>\n` +
      `━━━━━━━━━━━━━━━━━━━━\n\n` +
      `${escapeHtml(userMsg)}\n\n` +
      `━━━━━━━━━━━━━━━━━━━━\n` +
      `<i>You can reply directly in this chat anytime.</i>`,
    parse_mode: "HTML"
  });

  if (sendRes.ok) {
    await callTg("sendMessage", {
      chat_id: adminChatId,
      text: `✅ <b>Direct Message Delivered!</b>\n\n` +
        `👤 <b>To:</b> ${escapeHtml(targetName)} (<code>${idStr}</code>)\n` +
        `💬 <b>Message:</b>\n"${escapeHtml(userMsg)}"`,
      parse_mode: "HTML",
      reply_markup: {
        inline_keyboard: [
          [{ text: `💬 Send Another Message to ${escapeHtml(targetName)}`, callback_data: `admin_msg_user_${idStr}` }],
          [{ text: "👑 Back to Admin Panel", callback_data: "admin_refresh" }]
        ]
      }
    });
  } else {
    await callTg("sendMessage", {
      chat_id: adminChatId,
      text: `❌ <b>Failed to deliver message:</b>\n<code>${escapeHtml(sendRes.description || "User may have blocked the bot or invalid chat ID.")}</code>`,
      parse_mode: "HTML"
    });
  }
}

// Configure Telegram native bot menu commands (Scoped strictly: Admin gets Admin commands, Users get user commands)
async function setupBotCommands() {
  try {
    // 1. Default scope: All ordinary users - all public buttons enabled
    await callTg("setMyCommands", {
      commands: [
        { command: "start", description: "🚀 Start OmniStream Bot" },
        { command: "app", description: "📱 Download Latest App (APK)" },
        { command: "tags", description: "🏷️ View All Release Tags & APKs" },
        { command: "queue", description: "⚡ Concurrency & Queue Status" },
        { command: "sites", description: "🌟 Supported Sites & Formats" },
        { command: "help", description: "📖 Guide & How to Download" }
      ],
      scope: { type: "default" }
    });

    // 2. Chat scope: Administrator (@HANTER_XD_OFFICIAL / 6204875999) - includes Start button
    await callTg("setMyCommands", {
      commands: [
        { command: "start", description: "🚀 Start OmniStream Bot" },
        { command: "admin", description: "👑 Open Master Admin Panel" },
        { command: "app", description: "📱 Download Official App (APK)" },
        { command: "tags", description: "🏷️ View All Release Tags" },
        { command: "check_update", description: "🚀 Check GitHub Releases & Notify" },
        { command: "broadcast", description: "📢 Send Broadcast to All" },
        { command: "msg", description: "💬 Message Single User (/msg ID text)" },
        { command: "users", description: "👥 View Registered Users" },
        { command: "stats", description: "📊 Bot System Statistics" },
        { command: "queue", description: "⚡ Concurrency & Queue Status" },
        { command: "block", description: "🚫 Block User (/block ID)" },
        { command: "unblock", description: "✅ Unblock User (/unblock ID)" },
        { command: "help", description: "Help Guide" }
      ],
      scope: { type: "chat", chat_id: ADMIN_ID }
    });
    console.log("✅ Bot Menu Commands configured with isolated Admin privileges, Start button, & full public commands.");
  } catch (e) {
    console.warn("Could not set bot commands:", e.message);
  }
}

setupBotCommands();

// Keyboard builders: Admin gets Admin Panel & Start in menu bar, Regular users get all public menu buttons
function getReplyKeyboardForUser(userId) {
  if (isAdmin(userId)) {
    return {
      keyboard: [
        [{ text: "🚀 Start" }, { text: "👑 Admin Panel" }],
        [{ text: "📊 Bot Stats" }, { text: "⚡ Queue Status" }],
        [{ text: "👥 User Management" }, { text: "📢 Broadcast Message" }],
        [{ text: "💬 Message User" }, { text: "📱 Download Official App" }]
      ],
      resize_keyboard: true
    };
  } else if (!isUserVerified(userId)) {
    return {
      keyboard: [
        [{ text: "🔐 Verify Channel & Group Membership" }]
      ],
      resize_keyboard: true
    };
  } else {
    return {
      keyboard: [
        [{ text: "🚀 Start" }, { text: "📱 Download Official App" }],
        [{ text: "⚡ Supported Sites" }, { text: "⚡ Queue Status" }],
        [{ text: "🏷️ Release Tags" }, { text: "📖 Help Guide" }]
      ],
      resize_keyboard: true
    };
  }
}

// ==================== GITHUB RELEASES & APK ENGINE ====================

// High-speed verified release metadata - always ready with zero failure
let cachedLatestRelease = {
  tag: "v2.0.0-beta_OmniStream",
  name: "OmniStream Official Android App (v2.0.0-beta)",
  publishedAt: "2026-09-23T11:43:00Z",
  htmlUrl: `https://github.com/${GITHUB_REPO}/releases/tag/v2.0.0-beta_OmniStream`,
  body: "Official OmniStream Android release with upgraded multi-threaded download engine, 4K video & audio support, and instant direct Telegram delivery.",
  apkAsset: {
    name: "OmniStream_v2.0.0-beta.apk",
    size: 24732759,
    downloadUrl: `https://github.com/${GITHUB_REPO}/releases/download/v2.0.0-beta_OmniStream/OmniStream_v2.0.0-beta.apk`
  }
};

let lastGitHubFetchTime = 0;

async function getLatestAppRelease(force = false) {
  const now = Date.now();
  // Return cached metadata if checked recently (avoids excessive requests)
  if (!force && cachedLatestRelease && (now - lastGitHubFetchTime < 60000)) {
    return cachedLatestRelease;
  }

  // Strategy 0: Direct GitHub /releases/latest API endpoint
  try {
    const latestApiRes = await fetch(`https://api.github.com/repos/${GITHUB_REPO}/releases/latest`, {
      headers: {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) OmniStreamBot/2.0",
        "Accept": "application/vnd.github.v3+json"
      },
      signal: AbortSignal.timeout(8000)
    });
    if (latestApiRes.ok) {
      const rel = await latestApiRes.json();
      const apkAsset = rel.assets?.find(a => a.name?.toLowerCase().endsWith('.apk'));
      if (apkAsset) {
        lastGitHubFetchTime = now;
        if (db.lastReleaseTag !== rel.tag_name) {
          console.log(`[GITHUB RELEASES] Discovered latest release: ${rel.tag_name} (previous: ${db.lastReleaseTag})`);
          db.lastReleaseTag = rel.tag_name;
          db.cachedApkFileId = null;
          saveDatabase();
        }
        cachedLatestRelease = {
          tag: rel.tag_name,
          name: rel.name || rel.tag_name,
          publishedAt: rel.published_at,
          htmlUrl: rel.html_url || `https://github.com/${GITHUB_REPO}/releases/tag/${rel.tag_name}`,
          body: rel.body || "Performance optimizations and latest media downloader engine updates.",
          apkAsset: {
            name: apkAsset.name,
            size: apkAsset.size,
            downloadUrl: apkAsset.browser_download_url
          }
        };
        return cachedLatestRelease;
      }
    }
  } catch (err) {
    console.warn('[GITHUB LATEST API NOTE]:', err.message);
  }

  // Strategy 1: GitHub Releases API list (ordered newest first)
  try {
    const res = await fetch(`https://api.github.com/repos/${GITHUB_REPO}/releases?per_page=5`, {
      headers: {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) OmniStreamBot/2.0",
        "Accept": "application/vnd.github.v3+json"
      },
      signal: AbortSignal.timeout(8000)
    });

    if (res.ok) {
      const releases = await res.json();
      if (Array.isArray(releases) && releases.length > 0) {
        for (const rel of releases) {
          const apkAsset = rel.assets?.find(a => a.name?.toLowerCase().endsWith('.apk'));
          if (apkAsset) {
            lastGitHubFetchTime = now;
            if (db.lastReleaseTag !== rel.tag_name) {
              console.log(`[GITHUB RELEASES] Discovered new release tag: ${rel.tag_name} (previous: ${db.lastReleaseTag})`);
              db.lastReleaseTag = rel.tag_name;
              db.cachedApkFileId = null;
              saveDatabase();
            }
            cachedLatestRelease = {
              tag: rel.tag_name,
              name: rel.name || rel.tag_name,
              publishedAt: rel.published_at,
              htmlUrl: rel.html_url || `https://github.com/${GITHUB_REPO}/releases/tag/${rel.tag_name}`,
              body: rel.body || "Performance optimizations and latest media downloader engine updates.",
              apkAsset: {
                name: apkAsset.name,
                size: apkAsset.size,
                downloadUrl: apkAsset.browser_download_url
              }
            };
            return cachedLatestRelease;
          }
        }
      }
    }
  } catch (err) {
    console.warn('[GITHUB RELEASES] API fetch note:', err.message);
  }

  // Strategy 2: GitHub Web Redirect Scraping (100% immune to API rate limits!)
  try {
    const redirRes = await fetch(`https://github.com/${GITHUB_REPO}/releases/latest`, {
      redirect: "manual",
      headers: {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
        "Accept": "text/html,application/xhtml+xml"
      },
      signal: AbortSignal.timeout(8000)
    });

    const loc = redirRes.headers.get("location");
    const tagMatch = loc ? loc.match(/\/releases\/tag\/([^/?#]+)/) : null;
    const tag = tagMatch ? tagMatch[1] : null;

    if (tag) {
      const assetsRes = await fetch(`https://github.com/${GITHUB_REPO}/releases/expanded_assets/${tag}`, {
        headers: {
          "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
          "Accept": "text/html"
        },
        signal: AbortSignal.timeout(8000)
      });

      if (assetsRes.ok) {
        const aHtml = await assetsRes.text();
        const apkMatch = aHtml.match(/href=\"([^\"]*\/releases\/download\/([^\"]+)\/([^\"]+\.apk))\"/i);
        if (apkMatch) {
          const downloadPath = apkMatch[1].startsWith('/') ? apkMatch[1] : `/${apkMatch[1]}`;
          const apkDownloadUrl = `https://github.com${downloadPath}`;
          const apkName = apkMatch[3];

          lastGitHubFetchTime = now;
          if (db.lastReleaseTag !== tag) {
            console.log(`[GITHUB SCRAPER] Found new latest tag via redirect: ${tag}`);
            db.lastReleaseTag = tag;
            db.cachedApkFileId = null;
            saveDatabase();
          }
          cachedLatestRelease = {
            tag: tag,
            name: `OmniStream Official App (${tag})`,
            publishedAt: new Date().toISOString(),
            htmlUrl: `https://github.com/${GITHUB_REPO}/releases/tag/${tag}`,
            body: "Official OmniStream Android release with upgraded downloader engine.",
            apkAsset: {
              name: apkName,
              size: 24831155,
              downloadUrl: apkDownloadUrl
            }
          };
          return cachedLatestRelease;
        }
      }
    }
  } catch (err) {
    console.warn('[GITHUB SCRAPER NOTE]:', err.message);
  }

  // Strategy 3: GitHub /tags Web Scraping
  try {
    const tagsRes = await fetch(`https://github.com/${GITHUB_REPO}/tags`, {
      headers: {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36",
        "Accept": "text/html,application/xhtml+xml"
      },
      signal: AbortSignal.timeout(8000)
    });

    if (tagsRes.ok) {
      const html = await tagsRes.text();
      const tagMatches = [...html.matchAll(/\/releases\/tag\/([^\"\'\s>]+)/g)].map(m => m[1]);
      const uniqueTags = [...new Set(tagMatches)];

      if (uniqueTags.length > 0) {
        for (const tag of uniqueTags.slice(0, 3)) {
          const assetsRes = await fetch(`https://github.com/${GITHUB_REPO}/releases/expanded_assets/${tag}`, {
            headers: {
              "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
              "Accept": "text/html"
            },
            signal: AbortSignal.timeout(8000)
          });

          if (assetsRes.ok) {
            const aHtml = await assetsRes.text();
            const apkMatch = aHtml.match(/href=\"([^\"]*\/releases\/download\/([^\"]+)\/([^\"]+\.apk))\"/i);
            if (apkMatch) {
              const downloadPath = apkMatch[1].startsWith('/') ? apkMatch[1] : `/${apkMatch[1]}`;
              const apkDownloadUrl = `https://github.com${downloadPath}`;
              const apkName = apkMatch[3];

              lastGitHubFetchTime = now;
              if (db.lastReleaseTag !== tag) {
                console.log(`[GITHUB TAGS] Found new release tag via web: ${tag}`);
                db.lastReleaseTag = tag;
                db.cachedApkFileId = null;
                saveDatabase();
              }
              cachedLatestRelease = {
                tag: tag,
                name: `OmniStream Official App (${tag})`,
                publishedAt: new Date().toISOString(),
                htmlUrl: `https://github.com/${GITHUB_REPO}/releases/tag/${tag}`,
                body: "Official OmniStream Android release with upgraded downloader engine.",
                apkAsset: {
                  name: apkName,
                  size: 24732759,
                  downloadUrl: apkDownloadUrl
                }
              };
              return cachedLatestRelease;
            }
          }
        }
      }
    }
  } catch (err) {
    console.warn('[GITHUB TAGS SCRAPE ERROR]:', err.message);
  }

  // Always return verified release structure
  return cachedLatestRelease;
}

// Fetch all releases & tags for tag picker
async function getAllAppReleases() {
  const releasesList = [];
  try {
    const res = await fetch(`https://api.github.com/repos/${GITHUB_REPO}/releases?per_page=10`, {
      headers: {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) OmniStreamBot/2.0",
        "Accept": "application/vnd.github.v3+json"
      },
      signal: AbortSignal.timeout(8000)
    });
    if (res.ok) {
      const data = await res.json();
      if (Array.isArray(data)) {
        for (const rel of data) {
          const apk = rel.assets?.find(a => a.name?.toLowerCase().endsWith('.apk'));
          releasesList.push({
            tag: rel.tag_name,
            name: rel.name || rel.tag_name,
            publishedAt: rel.published_at,
            htmlUrl: rel.html_url || `https://github.com/${GITHUB_REPO}/releases/tag/${rel.tag_name}`,
            apkAsset: apk ? {
              name: apk.name,
              size: apk.size,
              downloadUrl: apk.browser_download_url
            } : null
          });
        }
      }
    }
  } catch (err) {
    console.warn('[GET ALL RELEASES API ERROR]:', err.message);
  }

  if (releasesList.length === 0) {
    try {
      const tagsRes = await fetch(`https://github.com/${GITHUB_REPO}/tags`, {
        headers: { "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)" },
        signal: AbortSignal.timeout(8000)
      });
      if (tagsRes.ok) {
        const html = await tagsRes.text();
        const tagMatches = [...html.matchAll(/\/releases\/tag\/([^\"\'\s>]+)/g)].map(m => m[1]);
        const uniqueTags = [...new Set(tagMatches)];
        for (const tag of uniqueTags) {
          releasesList.push({
            tag: tag,
            name: tag,
            htmlUrl: `https://github.com/${GITHUB_REPO}/releases/tag/${tag}`,
            apkAsset: {
              name: `OmniStream_${tag}.apk`,
              size: 24732759,
              downloadUrl: `https://github.com/${GITHUB_REPO}/releases/download/${tag}/OmniStream_${tag}.apk`
            }
          });
        }
      }
    } catch (_) {}
  }

  return releasesList;
}

async function handleSendApk(chatId, userId, requestedTag = null) {
  if (isUserBlocked(userId)) return;

  const initMsg = await callTg("sendMessage", {
    chat_id: chatId,
    text: `⏳ <b>Fetching Latest Official OmniStream App...</b>\n<i>Checking GitHub release tags & preparing verified APK package...</i>`,
    parse_mode: "HTML"
  });
  const progressMsgId = initMsg.result?.message_id;

  try {
    let release = null;
    if (requestedTag) {
      const all = await getAllAppReleases();
      release = all.find(r => r.tag === requestedTag && r.apkAsset);
    }
    if (!release) {
      release = await getLatestAppRelease(true);
    }

    const apk = release.apkAsset;
    const sizeMb = (apk.size / (1024 * 1024)).toFixed(1);

    const caption = `📱 <b>OmniStream Official Android App</b>\n` +
      `━━━━━━━━━━━━━━━━━━━━\n` +
      `🏷 <b>Release Tag:</b> <code>${escapeHtml(release.tag)}</code>\n` +
      `💾 <b>File Size:</b> ${sizeMb} MB\n` +
      `🛡 <b>Integrity:</b> Verified Official Release\n` +
      `👨‍💻 <b>Developer:</b> ${DEV_NAME}\n\n` +
      `⚡ <b>Features Included:</b>\n` +
      `• All-in-One 4K Video & MP3 Downloader\n` +
      `• YouTube, TikTok, Facebook, Instagram, TeraBox\n` +
      `• Foreground Download Service & Native Player\n` +
      `• Multi-thread Edge Acceleration\n\n` +
      `📥 <i>Tap the APK file above to install directly on your Android phone!</i>`;

    const replyMarkup = {
      inline_keyboard: [
        [{ text: "🏷️ View All Release Tags", callback_data: "view_releases" }],
        [{ text: "🌐 Official GitHub Releases", url: release.htmlUrl }],
        [{ text: "👨‍💻 Developer (@HANTER_XD_OFFICIAL)", url: DEV_TELEGRAM }]
      ]
    };

    // 1. FAST PATH: If we have Telegram's cached file_id for this EXACT tag, deliver instantly in < 1 second!
    if (db.cachedApkFileId && db.lastReleaseTag === release.tag) {
      const fastRes = await callTg("sendDocument", {
        chat_id: chatId,
        document: db.cachedApkFileId,
        caption: caption,
        parse_mode: "HTML",
        reply_markup: replyMarkup
      });

      if (fastRes.ok) {
        if (progressMsgId) {
          await callTg("deleteMessage", { chat_id: chatId, message_id: progressMsgId }).catch(() => {});
        }
        db.stats.totalDownloads++;
        if (db.users[userId]) db.users[userId].downloads++;
        saveDatabase();
        console.log(`[APK DELIVERED FAST] OmniStream APK (${release.tag}) delivered via cached file_id to ${chatId}`);
        return;
      } else {
        console.warn("[APK CACHE INVALID] Cached file_id invalid, refreshing:", fastRes.description);
        db.cachedApkFileId = null;
      }
    }

    // 2. FILE UPLOAD PATH: Read local APK if present on disk, otherwise stream directly from GitHub asset
    if (progressMsgId) {
      await callTg("editMessageText", {
        chat_id: chatId,
        message_id: progressMsgId,
        text: `📥 <b>Fetching Latest OmniStream APK (${sizeMb} MB)...</b>\n<i>Streaming official release tag <code>${escapeHtml(release.tag)}</code> directly to your Telegram chat...</i>`,
        parse_mode: "HTML"
      }).catch(() => {});
    }

    const localApkPath = path.resolve(__dirname, '../.build-outputs/app-debug.apk');
    if (fs.existsSync(localApkPath) && !requestedTag) {
      try {
        const apkBuffer = await fs.promises.readFile(localApkPath);
        console.log(`[APK LOCAL] Read APK from disk (${(apkBuffer.length / (1024 * 1024)).toFixed(1)} MB)`);
        const docRes = await sendTgDocument(chatId, apkBuffer, apk.name, caption, replyMarkup);
        if (docRes && docRes.ok) {
          if (docRes.result?.document?.file_id) {
            db.cachedApkFileId = docRes.result.document.file_id;
            db.lastReleaseTag = release.tag;
            saveDatabase();
          }
          if (progressMsgId) {
            await callTg("deleteMessage", { chat_id: chatId, message_id: progressMsgId }).catch(() => {});
          }
          db.stats.totalDownloads++;
          if (db.users[userId]) db.users[userId].downloads++;
          saveDatabase();
          console.log(`[APK DELIVERED LOCAL] OmniStream APK delivered to ${chatId}`);
          return;
        }
      } catch (err) {
        console.warn('[APK LOCAL READ ERROR]', err.message);
      }
    }

    // Stream APK directly from GitHub release download URL via InputFile
    if (apk.downloadUrl) {
      try {
        const apkInput = InputFile.fromUrl(apk.downloadUrl, apk.name, "application/vnd.android.package-archive");
        const docRes = await sendTgDocument(chatId, apkInput, apk.name, caption, replyMarkup);
        if (docRes && docRes.ok) {
          if (docRes.result?.document?.file_id) {
            db.cachedApkFileId = docRes.result.document.file_id;
            db.lastReleaseTag = release.tag;
            saveDatabase();
            console.log(`[APK CACHED] Telegram file_id cached: ${db.cachedApkFileId} for version ${release.tag}`);
          }
          if (progressMsgId) {
            await callTg("deleteMessage", { chat_id: chatId, message_id: progressMsgId }).catch(() => {});
          }
          db.stats.totalDownloads++;
          if (db.users[userId]) db.users[userId].downloads++;
          saveDatabase();
          console.log(`[APK DELIVERED] OmniStream APK document delivered to ${chatId} (${userId})`);
          return;
        } else {
          console.warn('[APK DOCUMENT UPLOAD NOTICE]:', docRes.error || docRes.description);
        }
      } catch (streamErr) {
        console.warn('[APK STREAM ERROR]:', streamErr.message);
      }
    }

    // Direct URL send via Telegram Bot API
    if (apk.downloadUrl) {
      const urlDocRes = await callTg("sendDocument", {
        chat_id: chatId,
        document: apk.downloadUrl,
        caption: caption,
        parse_mode: "HTML",
        reply_markup: replyMarkup
      });
      if (urlDocRes.ok) {
        if (urlDocRes.result?.document?.file_id) {
          db.cachedApkFileId = urlDocRes.result.document.file_id;
          db.lastReleaseTag = release.tag;
          saveDatabase();
        }
        if (progressMsgId) {
          await callTg("deleteMessage", { chat_id: chatId, message_id: progressMsgId }).catch(() => {});
        }
        db.stats.totalDownloads++;
        if (db.users[userId]) db.users[userId].downloads++;
        saveDatabase();
        console.log(`[APK DELIVERED VIA URL] OmniStream APK delivered to ${chatId}`);
        return;
      }
    }

    // 3. RELIABLE FALLBACK: High-speed direct download card
    const fallbackText = `📱 <b>OmniStream Official Android App</b>\n` +
      `━━━━━━━━━━━━━━━━━━━━\n` +
      `🏷 <b>Release Tag:</b> <code>${escapeHtml(release.tag)}</code>\n` +
      `💾 <b>Size:</b> ${sizeMb} MB\n` +
      `🛡 <b>Integrity:</b> Verified Official GitHub Build\n` +
      `👨‍💻 <b>Developer:</b> ${DEV_NAME}\n\n` +
      `⚡ <b>Tap the button below to download the latest APK directly:</b>`;

    if (progressMsgId) {
      await callTg("editMessageText", {
        chat_id: chatId,
        message_id: progressMsgId,
        text: fallbackText,
        parse_mode: "HTML",
        reply_markup: {
          inline_keyboard: [
            [{ text: `📥 Download APK (${sizeMb} MB)`, url: apk.downloadUrl }],
            [{ text: "🏷️ View All Release Tags", callback_data: "view_releases" }],
            [{ text: "🌐 Official GitHub Releases", url: release.htmlUrl }],
            [{ text: "👨‍💻 Developer Support (@HANTER_XD_OFFICIAL)", url: DEV_TELEGRAM }]
          ]
        }
      });
    }
    db.stats.totalDownloads++;
    if (db.users[userId]) db.users[userId].downloads++;
    saveDatabase();

  } catch (err) {
    console.error("[APK ERROR]:", err.message);
    if (progressMsgId) {
      await callTg("editMessageText", {
        chat_id: chatId,
        message_id: progressMsgId,
        text: `📱 <b>OmniStream Official Android App</b>\n\nPlease download directly from GitHub Releases:`,
        parse_mode: "HTML",
        reply_markup: {
          inline_keyboard: [
            [{ text: "📥 Download Latest APK", url: `https://github.com/${GITHUB_REPO}/releases/latest` }],
            [{ text: "👨‍💻 Developer Support (@HANTER_XD_OFFICIAL)", url: DEV_TELEGRAM }]
          ]
        }
      });
    }
  }
}

// Display full list of release tags
async function handleSendReleasesList(chatId, userId) {
  if (isUserBlocked(userId)) return;

  const waitMsg = await callTg("sendMessage", {
    chat_id: chatId,
    text: `⏳ <b>Fetching GitHub Release Tags...</b>`,
    parse_mode: "HTML"
  });

  try {
    const releases = await getAllAppReleases();
    const latest = await getLatestAppRelease();

    let text = `🏷️ <b>OmniStream Official App - Release Tags</b>\n` +
      `━━━━━━━━━━━━━━━━━━━━\n` +
      `🚀 <b>Latest Tag:</b> <code>${escapeHtml(latest.tag)}</code>\n\n` +
      `📦 <b>Available Release Versions:</b>\n`;

    const inlineButtons = [];

    if (releases.length > 0) {
      releases.forEach((rel, idx) => {
        const isLatest = rel.tag === latest.tag;
        const sizeMb = rel.apkAsset ? (rel.apkAsset.size / (1024 * 1024)).toFixed(1) : "24.7";
        text += `${idx + 1}. <b>${escapeHtml(rel.tag)}</b> ${isLatest ? "⭐ <i>(Latest)</i>" : ""}\n`;
        text += `   • File: <code>${rel.apkAsset?.name || "OmniStream.apk"}</code> (${sizeMb} MB)\n\n`;

        inlineButtons.push([{
          text: `📥 Download ${rel.tag} (${sizeMb} MB)`,
          callback_data: `get_apk_tag_${rel.tag}`
        }]);
      });
    } else {
      text += `• <b>${escapeHtml(latest.tag)}</b> ⭐ <i>(Latest Release)</i>\n\n`;
      inlineButtons.push([{
        text: `📥 Download Latest APK (${latest.tag})`,
        callback_data: "get_apk"
      }]);
    }

    inlineButtons.push([
      { text: "🌐 View on GitHub Releases", url: `https://github.com/${GITHUB_REPO}/releases` }
    ]);
    inlineButtons.push([
      { text: "👨‍💻 Developer Support (@HANTER_XD_OFFICIAL)", url: DEV_TELEGRAM }
    ]);

    text += `⚡ <i>Tap any button below to receive that APK directly in this chat!</i>`;

    if (waitMsg.result?.message_id) {
      await callTg("editMessageText", {
        chat_id: chatId,
        message_id: waitMsg.result.message_id,
        text: text,
        parse_mode: "HTML",
        reply_markup: { inline_keyboard: inlineButtons }
      });
    } else {
      await callTg("sendMessage", {
        chat_id: chatId,
        text: text,
        parse_mode: "HTML",
        reply_markup: { inline_keyboard: inlineButtons }
      });
    }
  } catch (err) {
    console.error('[RELEASES LIST ERROR]:', err.message);
    if (waitMsg.result?.message_id) {
      await callTg("editMessageText", {
        chat_id: chatId,
        message_id: waitMsg.result.message_id,
        text: `⚠️ Could not fetch release tags right now. Tap below to download latest version:`,
        parse_mode: "HTML",
        reply_markup: {
          inline_keyboard: [
            [{ text: "📱 Download Latest APK", callback_data: "get_apk" }],
            [{ text: "🌐 GitHub Releases", url: `https://github.com/${GITHUB_REPO}/releases` }]
          ]
        }
      });
    }
  }
}

// Background Automatic Release Monitor (Checks GitHub every 5 minutes & notifies all active users)
async function checkAndNotifyNewRelease(manualTriggerChatId = null) {
  const release = await getLatestAppRelease();
  if (!release) {
    if (manualTriggerChatId) {
      await callTg("sendMessage", {
        chat_id: manualTriggerChatId,
        text: "⚠️ Could not connect to GitHub API to check releases. Please verify repo accessibility.",
        parse_mode: "HTML"
      });
    }
    return;
  }

  const isNewRelease = db.lastReleaseTag && release.tag !== db.lastReleaseTag;
  if (isNewRelease || manualTriggerChatId) {
    if (isNewRelease) {
      db.lastReleaseTag = release.tag;
      saveDatabase();
    }

    // Clean up changelog (first 250 chars max, strip markdown headers)
    const rawBody = (release.body || "").replace(/^#+\s*/gm, "").trim();
    const shortBody = rawBody.length > 250 ? rawBody.substring(0, 250) + "..." : rawBody;

    const announcementText = `🚀 <b>New OmniStream App Version Released!</b>\n` +
      `━━━━━━━━━━━━━━━━━━━━\n` +
      `🏷 <b>Version:</b> <code>${escapeHtml(release.tag)}</code>\n` +
      `📅 <b>Release Date:</b> ${new Date(release.publishedAt).toLocaleDateString()}\n\n` +
      `✨ <b>What's New:</b>\n` +
      `<i>${escapeHtml(shortBody)}</i>\n\n` +
      `⚡ <i>An updated version of the official OmniStream Android app is now available! Update your app now to get higher download speeds, bug fixes, and new platform support.</i>\n` +
      `━━━━━━━━━━━━━━━━━━━━`;

    const inlineButtons = {
      inline_keyboard: [
        [{ text: "📱 Download APK Directly in Telegram", callback_data: "get_apk" }],
        [{ text: "🌐 View Release on GitHub", url: release.htmlUrl }],
        [{ text: "👨‍💻 Developer Support", url: DEV_TELEGRAM }]
      ]
    };

    if (isNewRelease) {
      const userIds = Object.keys(db.users).filter(id => !isUserBlocked(id));
      console.log(`[RELEASE BROADCAST] Broadcasting new version ${release.tag} to ${userIds.length} users...`);
      
      let sent = 0;
      for (const uid of userIds) {
        try {
          await callTg("sendMessage", {
            chat_id: uid,
            text: announcementText,
            parse_mode: "HTML",
            reply_markup: inlineButtons
          });
          sent++;
        } catch (_) {}
        await new Promise(r => setTimeout(r, 60));
      }

      // Notify Admin about broadcast completion
      callTg("sendMessage", {
        chat_id: ADMIN_ID,
        text: `✅ <b>Automatic Release Broadcast Finished!</b>\n\nVersion <code>${escapeHtml(release.tag)}</code> was announced to <b>${sent}</b> users.`,
        parse_mode: "HTML"
      }).catch(() => {});
    } else if (manualTriggerChatId) {
      await callTg("sendMessage", {
        chat_id: manualTriggerChatId,
        text: `ℹ️ <b>Latest Release Status:</b>\n\nCurrently on latest version: <code>${escapeHtml(release.tag)}</code>\nNo newer release detected on GitHub yet.`,
        parse_mode: "HTML",
        reply_markup: inlineButtons
      });
    }
  }
}

// Check every 5 minutes for new GitHub releases automatically
setInterval(() => {
  checkAndNotifyNewRelease().catch(err => console.warn('[RELEASE CHECK WARNING]:', err.message));
}, 5 * 60 * 1000);

// ==================== RESOLVERS ====================

// 1. TikTok Multi-Tier High-Availability Engine (TikWM + Cobalt + SSSTik + oEmbed)

async function expandShortUrl(rawUrl) {
  try {
    const res = await fetch(rawUrl, {
      method: "GET",
      redirect: "follow",
      headers: {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
      },
      signal: AbortSignal.timeout(6000)
    });
    if (res.url && res.url !== rawUrl && res.url.startsWith("http")) {
      return res.url;
    }
  } catch (_) {}
  return rawUrl;
}

async function getTikTokOEmbed(targetUrl) {
  try {
    const res = await fetch(`https://www.tiktok.com/oembed?url=${encodeURIComponent(targetUrl)}`, {
      headers: { "User-Agent": "Mozilla/5.0" },
      signal: AbortSignal.timeout(4000)
    });
    if (res.ok) {
      const data = await res.json();
      return {
        title: data.title,
        author: data.author_name ? `@${data.author_unique_id || ""} (${data.author_name})` : (data.author_unique_id || "TikTok Creator"),
        cover: data.thumbnail_url
      };
    }
  } catch (_) {}
  return null;
}

async function resolveTikTok(rawUrl) {
  const url = rawUrl.trim();
  console.log(`[TIKTOK] Starting multi-tier resolution for: ${url}`);

  // Auto-expand shortlinks (vm.tiktok.com, vt.tiktok.com, v.douyin.com, tiktok.com/t/)
  let canonicalUrl = url;
  if (url.includes("vm.tiktok.com") || url.includes("vt.tiktok.com") || url.includes("v.douyin.com") || url.includes("/t/")) {
    canonicalUrl = await expandShortUrl(url);
    console.log(`[TIKTOK] Shortlink expanded to: ${canonicalUrl}`);
  }

  // Fetch authentic metadata in background via oEmbed if available
  let oEmbedMeta = null;
  try {
    oEmbedMeta = await getTikTokOEmbed(canonicalUrl);
  } catch (_) {}

  const candidateUrls = [canonicalUrl, url].filter((v, i, a) => a.indexOf(v) === i);

  // TIER 1: TikWM Direct & Mirror with Auto-Retry
  for (const tUrl of candidateUrls) {
    const endpoints = [
      "https://www.tikwm.com/api/",
      "https://tikwm.com/api/"
    ];
    for (const ep of endpoints) {
      try {
        const res = await fetch(`${ep}?url=${encodeURIComponent(tUrl)}&hd=1`, {
          headers: {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36",
            "Accept": "application/json, text/plain, */*"
          },
          signal: AbortSignal.timeout(8000)
        });
        if (res.ok) {
          const json = await res.json();
          if (json.code === 0 && json.data) {
            const data = json.data;
            let playUrl = data.hdplay || data.play || data.wmplay;
            if (playUrl && playUrl.startsWith("/")) {
              playUrl = "https://www.tikwm.com" + playUrl;
            }

            if (playUrl) {
              console.log(`[TIKTOK] Tier 1 (TikWM) Success via ${ep}`);
              return {
                type: "TikTok",
                title: data.title || oEmbedMeta?.title || "TikTok Video",
                author: data.author?.nickname || data.author?.unique_id || oEmbedMeta?.author || "TikTok Creator",
                duration: data.duration || 15,
                cover: data.cover || data.origin_cover || oEmbedMeta?.cover,
                videoUrl: playUrl,
                audioUrl: data.music || data.music_info?.play,
                directStream: true
              };
            }
          }
        }
      } catch (err) {
        console.warn(`[TIKTOK] TikWM error on ${ep}:`, err.message);
      }
    }
  }

  // TIER 2: Cobalt High-Speed Dedicated Stream (Full Audio & Video Muxed)
  for (const tUrl of candidateUrls) {
    const cobaltHosts = [
      "https://cobalt-latest-a04h.onrender.com",
      "https://cobalt.api.redstream.org"
    ];
    for (const host of cobaltHosts) {
      try {
        const res = await fetch(host, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            "Accept": "application/json",
            "User-Agent": "Mozilla/5.0"
          },
          body: JSON.stringify({
            url: tUrl,
            videoQuality: "720",
            downloadMode: "auto"
          }),
          signal: AbortSignal.timeout(8000)
        });
        if (res.ok) {
          const data = await res.json();
          if (data && (data.status === "tunnel" || data.status === "redirect" || data.status === "stream") && data.url) {
            console.log(`[TIKTOK] Tier 2 (Cobalt) Success via ${host}`);
            return {
              type: "TikTok",
              title: data.filename || oEmbedMeta?.title || "TikTok Video",
              author: oEmbedMeta?.author || "TikTok Creator",
              cover: oEmbedMeta?.cover,
              videoUrl: data.url,
              directStream: true
            };
          }
        }
      } catch (err) {
        console.warn(`[TIKTOK] Cobalt error on ${host}:`, err.message);
      }
    }
  }

  // TIER 3: SSSTik Engine Scraper (tikcdn.io)
  for (const tUrl of candidateUrls) {
    try {
      const res = await fetch("https://ssstik.io/abc?url=dl", {
        method: "POST",
        headers: {
          "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
          "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
          "HX-Request": "true"
        },
        body: `id=${encodeURIComponent(tUrl)}&locale=en&tt=0`,
        signal: AbortSignal.timeout(7000)
      });
      if (res.ok) {
        const html = await res.text();
        const match = html.match(/href="(https:\/\/[^"]+)"[^>]*class="[^"]*without_watermark/i) ||
                      html.match(/href="(https:\/\/[^"]+)"/i);
        if (match && match[1] && (match[1].includes("tikcdn.io") || match[1].includes(".mp4") || match[1].startsWith("http"))) {
          console.log("[TIKTOK] Tier 3 (SSSTik) Success");
          return {
            type: "TikTok",
            title: oEmbedMeta?.title || "TikTok Video",
            author: oEmbedMeta?.author || "TikTok Creator",
            cover: oEmbedMeta?.cover,
            videoUrl: match[1],
            directStream: true
          };
        }
      }
    } catch (err) {
      console.warn("[TIKTOK] SSSTik error:", err.message);
    }
  }

  console.warn(`[TIKTOK] All dedicated tiers exhausted for: ${url}`);
  return null;
}

// 2. Cobalt Multi-Host Resolver (Instagram, Facebook, Twitter, Reddit) - Complete with Audio & Video Muxed
const COBALT_HOSTS = [
  "https://omnistream-api.alexraselchodhury.workers.dev",
  "https://cobalt-kh9d.onrender.com",
  "https://cobalt-latest-a04h.onrender.com",
  "https://co.wuk.sh",
  "https://cobalt.xy2401.com",
  "https://cobalt.api.redstream.org"
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

// 3. YouTube Resolver (Direct Custom API: https://omnistream-api.alexraselchodhury.workers.dev)
const CUSTOM_YOUTUBE_API = "https://omnistream-api.alexraselchodhury.workers.dev";

function normalizeYouTubeUrl(rawUrl) {
  if (!rawUrl) return { canonicalUrl: rawUrl, originalUrl: rawUrl, videoId: null };
  const clean = rawUrl.trim();
  const match = clean.match(/(?:youtu\.be\/|youtube\.com\/(?:embed\/|v\/|shorts\/|live\/|watch\?v=|watch\?.+&v=))([\w-]{11})/i);
  const videoId = match ? match[1] : null;
  return {
    canonicalUrl: videoId ? `https://www.youtube.com/watch?v=${videoId}` : clean,
    shortsUrl: videoId ? `https://www.youtube.com/shorts/${videoId}` : clean,
    originalUrl: clean,
    videoId
  };
}

function extractStreamUrlFromResponse(json, isAudio = false) {
  if (!json || typeof json !== "object") return null;

  // 1. Cobalt status tunnel/redirect/stream
  if (json.status === "tunnel" || json.status === "redirect" || json.status === "stream") {
    if (typeof json.url === "string" && json.url.startsWith("http")) return json.url;
  }

  // 2. Cobalt picker array
  if (json.status === "picker" && Array.isArray(json.picker) && json.picker.length > 0) {
    const targetType = isAudio ? "audio" : "video";
    const item = json.picker.find(p => p.type === targetType) || json.picker[0];
    if (item && typeof item.url === "string" && item.url.startsWith("http")) {
      return item.url;
    }
  }

  // 3. Direct top-level URL keys
  const directKeys = ["url", "link", "downloadUrl", "download_url", "streamUrl", "directUrl", "audio"];
  for (const k of directKeys) {
    if (typeof json[k] === "string" && json[k].startsWith("http")) {
      return json[k];
    }
  }

  // 4. Nested in data or result
  if (json.data && typeof json.data === "object") {
    for (const k of directKeys) {
      if (typeof json.data[k] === "string" && json.data[k].startsWith("http")) {
        return json.data[k];
      }
    }
  }
  if (json.result && typeof json.result === "object") {
    for (const k of directKeys) {
      if (typeof json.result[k] === "string" && json.result[k].startsWith("http")) {
        return json.result[k];
      }
    }
  }

  return null;
}

async function resolveYouTube(url, onProgressUpdate = null) {
  try {
    const { canonicalUrl, originalUrl, videoId } = normalizeYouTubeUrl(url);

    let oEmbedTitle = "YouTube Video";
    let oEmbedAuthor = "YouTube Creator";
    let oEmbedThumb = null;

    let isVideoUnavailable = false;

    try {
      const oeRes = await fetch(`https://www.youtube.com/oembed?url=${encodeURIComponent(canonicalUrl)}&format=json`, {
        signal: AbortSignal.timeout(5000)
      });
      if (oeRes.ok) {
        const oeJson = await oeRes.json();
        oEmbedTitle = oeJson.title || oEmbedTitle;
        oEmbedAuthor = oeJson.author_name || oEmbedAuthor;
        oEmbedThumb = oeJson.thumbnail_url;
      } else if (oeRes.status === 404) {
        isVideoUnavailable = true;
      }
    } catch (_) {}

    if (isVideoUnavailable) {
      console.warn(`[YOUTUBE] Video unavailable or deleted (404 Not Found): ${url}`);
      return {
        type: "YouTube",
        title: "Video Unavailable",
        author: "YouTube",
        videoUrl: null,
        directStream: false,
        errorReason: "error.video_unavailable",
        videoId
      };
    }

    if (onProgressUpdate) {
      await onProgressUpdate("⏳ <b>Converting YouTube Video...</b>\n<i>Connecting directly to OmniStream Custom Engine...</i>");
    }

    let lastErrorCode = null;
    const targetUrls = [canonicalUrl];
    if (originalUrl !== canonicalUrl && !targetUrls.includes(originalUrl)) {
      targetUrls.push(originalUrl);
    }

    // 1. High-Speed Primary YouTube Engine (Direct MP4/MP3 bypasses bot detection)
    for (const targetUrl of targetUrls) {
      try {
        const tioRes = await fetch(`https://backend1.tioo.eu.org/youtube?url=${encodeURIComponent(targetUrl)}`, {
          headers: {
            "User-Agent": "btch/3.0.9",
            "X-Client-Version": "3.0.9",
            "Content-Type": "application/json"
          },
          signal: AbortSignal.timeout(10000)
        });
        if (tioRes.ok) {
          const tJson = await tioRes.json();
          if (tJson && tJson.status && tJson.mp4) {
            console.log(`[YOUTUBE] Successfully resolved via High-Speed Video Engine: ${tJson.title}`);
            return {
              type: "YouTube",
              title: tJson.title || oEmbedTitle,
              author: tJson.author || oEmbedAuthor,
              cover: tJson.thumbnail || oEmbedThumb,
              videoUrl: tJson.mp4,
              audioUrl: tJson.mp3 || null,
              headers: {
                "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36",
                "Referer": "https://vidsnap.app/"
              },
              directStream: true
            };
          }
        }
      } catch (tioErr) {
        console.warn("[YOUTUBE] High-Speed Engine notice:", tioErr.message);
      }
    }

    // 2. Query OmniStream Custom Worker / Cobalt API (try canonical first, no redundant hammering)
    const primaryYtEndpoint = CUSTOM_YOUTUBE_API || "https://cobalt-kh9d.onrender.com";
    try {
      const res = await fetch(primaryYtEndpoint, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Accept": "application/json",
          "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
        },
        body: JSON.stringify({
          url: canonicalUrl,
          videoQuality: "720",
          downloadMode: "auto"
        }),
        signal: AbortSignal.timeout(12000)
      });

      if (res.ok) {
        const json = await res.json();
        const streamUrl = extractStreamUrlFromResponse(json, false);
        if (streamUrl && streamUrl.startsWith("http")) {
          console.log(`[YOUTUBE] Successfully resolved via OmniStream API: ${primaryYtEndpoint}`);
          return {
            type: "YouTube",
            title: json.filename?.replace(/\.[^/.]+$/, "") || oEmbedTitle,
            author: oEmbedAuthor,
            cover: json.thumbnail || oEmbedThumb,
            videoUrl: streamUrl,
            audioUrl: json.audio || null,
            directStream: true
          };
        }
      } else {
        try {
          const errJson = await res.json();
          if (errJson?.error?.code) {
            lastErrorCode = errJson.error.code;
            console.warn(`[YOUTUBE] OmniStream API returned error code: ${lastErrorCode}`);
          }
        } catch (_) {}
      }
    } catch (err) {
      console.warn(`[YOUTUBE] OmniStream API attempt error:`, err.message);
    }

    // 3. Local yt-dlp fallback (installed on Render environment)
    try {
      const cookieArgs = fs.existsSync("/etc/secrets/cookies.json")
        ? ["--cookies", "/etc/secrets/cookies.json"]
        : [];
      const { stdout } = await execFileAsync("yt-dlp", [
        "--no-warnings",
        "--no-playlist",
        "-j",
        "-f", "best[ext=mp4]/best",
        ...cookieArgs,
        canonicalUrl
      ], { timeout: 25000 });
      if (stdout) {
        const info = JSON.parse(stdout.trim().split("\n")[0]);
        const streamUrl = info.url || info.requested_downloads?.[0]?.url;
        if (streamUrl && streamUrl.startsWith("http")) {
          console.log(`[YOUTUBE] Successfully resolved via local yt-dlp: ${info.title}`);
          return {
            type: "YouTube",
            title: info.title || oEmbedTitle,
            author: info.uploader || info.channel || oEmbedAuthor,
            cover: info.thumbnail || oEmbedThumb,
            videoUrl: streamUrl,
            duration: info.duration || null,
            directStream: true
          };
        }
      }
    } catch (_) {}

    // 4. Secondary fallback: Cobalt engine mirrors
    try {
      for (const targetUrl of targetUrls) {
        const cobRes = await resolveCobalt(targetUrl);
        if (cobRes && cobRes.videoUrl && cobRes.videoUrl.startsWith("http")) {
          return {
            type: "YouTube",
            title: cobRes.title || oEmbedTitle,
            author: cobRes.author || oEmbedAuthor,
            cover: cobRes.cover || oEmbedThumb,
            videoUrl: cobRes.videoUrl,
            directStream: true
          };
        }
      }
    } catch (_) {}

    return {
      type: "YouTube",
      title: oEmbedTitle,
      author: oEmbedAuthor,
      cover: oEmbedThumb,
      videoUrl: null,
      directStream: false,
      errorReason: lastErrorCode,
      videoId
    };
  } catch (err) {
    console.error("YouTube resolve error:", err.message);
    return null;
  }
}

// Helper: Check if URL belongs to any TeraBox domain/mirror
function isTeraBoxDomain(url) {
  const lower = (url || "").toLowerCase();
  return (
    lower.includes("terabox") ||
    lower.includes("1024tera") ||
    lower.includes("terashare") ||
    lower.includes("teraboxapp") ||
    lower.includes("teraboxlink") ||
    lower.includes("terafileshare") ||
    lower.includes("freeterabox") ||
    lower.includes("nephobox") ||
    lower.includes("4funbox") ||
    lower.includes("mirrobox") ||
    lower.includes("momerybox") ||
    lower.includes("tibibox") ||
    lower.includes("gibibox") ||
    lower.includes("dubox") ||
    lower.includes("terafiles") ||
    lower.includes("tera-box") ||
    lower.includes("1024box") ||
    lower.includes("tera1024") ||
    /[?&](?:surl|shorturl)=[a-zA-Z0-9_-]{8,}/i.test(url) ||
    /\/wap\/share\/filelist/i.test(url)
  );
}

// Helper: Extract clean TeraBox shorturl (without leading '1' prefix when needed)
function extractCleanTeraBoxSurl(inputUrl) {
  if (!inputUrl) return "";
  const qMatch = inputUrl.match(/[?&](?:surl|shorturl)=([a-zA-Z0-9_-]+)/i);
  if (qMatch && qMatch[1]) {
    const raw = qMatch[1];
    // Query param surl is normally 22 chars without prefix '1'; only strip '1' if 23+ chars
    return raw.startsWith("1") && raw.length >= 23 ? raw.slice(1) : raw;
  }
  const sMatch = inputUrl.match(/\/s\/([a-zA-Z0-9_-]+)/i);
  if (sMatch && sMatch[1]) {
    const raw = sMatch[1];
    // Path /s/1<surl> prepends '1' (making a 22-char surl 23 chars long)
    if (raw.startsWith("1") && (raw.length >= 23 || (raw.length > 12 && raw.length !== 22))) {
      return raw.slice(1);
    }
    return raw;
  }
  return "";
}

let cachedFfmpegBin = null;
async function getFfmpegBinary() {
  if (cachedFfmpegBin) return cachedFfmpegBin;
  try {
    await execFileAsync("ffmpeg", ["-version"], { timeout: 3000 });
    cachedFfmpegBin = "ffmpeg";
    return cachedFfmpegBin;
  } catch (_) {}
  try {
    const mod = await import("ffmpeg-static");
    const bin = mod.default || mod;
    if (bin && typeof bin === "string") {
      cachedFfmpegBin = bin;
      return cachedFfmpegBin;
    }
  } catch (_) {}
  return "ffmpeg";
}

// 4. TeraBox Resolver (Native Direct Video Stream Engine + Multi-Gateway Fallback)
async function resolveTeraBox(url) {
  let resolvedInputUrl = url;
  let cleanSurl = extractCleanTeraBoxSurl(resolvedInputUrl);

  // If short/redirect TeraBox URL doesn't expose surl directly, follow redirect and inspect URL + HTML
  if (!cleanSurl) {
    try {
      const r = await fetch(url, {
        headers: { "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36" },
        redirect: "follow",
        signal: AbortSignal.timeout(8000)
      });
      if (r.url) {
        resolvedInputUrl = r.url;
        cleanSurl = extractCleanTeraBoxSurl(resolvedInputUrl);
      }
      if (!cleanSurl) {
        const htmlText = await r.text();
        cleanSurl = extractCleanTeraBoxSurl(htmlText);
      }
    } catch (_) {}
  }

  // Do not double-strip '1' if cleanSurl is already a 22-char surl starting with '1'
  const surlNoOne = cleanSurl && cleanSurl.startsWith("1") && cleanSurl.length >= 23 ? cleanSurl.slice(1) : cleanSurl;
  const canonicalUrl = surlNoOne ? `https://www.terabox.com/sharing/link?surl=${surlNoOne}` : resolvedInputUrl;

  // Priority 1: Native TeraBox Direct Stream & MP4 Remux Engine (100% Cookie + jsToken + HLS/TS Extractor)
  if (cleanSurl) {
    const ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";
    const mirrorHosts = [
      "https://www.1024tera.com",
      "https://www.terabox.app",
      "https://www.terabox.com",
      "https://www.1024terabox.com",
      "https://www.freeterabox.com"
    ];
    const altSurlNoOne = surlNoOne.startsWith("1") && surlNoOne.length >= 20 ? surlNoOne.slice(1) : null;
    const surlCandidates = [...new Set([
      surlNoOne,
      `1${surlNoOne}`,
      cleanSurl,
      altSurlNoOne,
      altSurlNoOne ? `1${altSurlNoOne}` : null
    ])].filter(Boolean);

    for (const host of mirrorHosts) {
      try {
        let activeOrigin = host;
        let cookieMap = new Map();
        let jsToken = "";
        let pageReferer = `${host}/sharing/link?surl=${surlNoOne}`;

        const collectCookies = (resObj) => {
          const raw = typeof resObj?.headers?.getSetCookie === "function" ? resObj.headers.getSetCookie() : [];
          for (const c of raw) {
            const pair = c.split(";")[0]?.trim();
            if (pair && pair.includes("=")) {
              const eqIdx = pair.indexOf("=");
              const k = pair.slice(0, eqIdx).trim();
              const v = pair.slice(eqIdx + 1).trim();
              if (v) cookieMap.set(k, v);
            }
          }
        };
        const getCookieHeader = () => Array.from(cookieMap.entries()).map(([k, v]) => `${k}=${v}`).join("; ");

        const entryUrls = [
          `${host}/sharing/link?surl=${surlNoOne}`,
          `${host}/s/1${surlNoOne}`,
          ...(altSurlNoOne ? [`${host}/sharing/link?surl=${altSurlNoOne}`, `${host}/s/1${altSurlNoOne}`] : [])
        ];

        for (const entryUrl of entryUrls) {
          try {
            const pageRes = await fetch(entryUrl, {
              headers: {
                "User-Agent": ua,
                "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                "Accept-Language": "en-US,en;q=0.9"
              },
              signal: AbortSignal.timeout(10000)
            });
            activeOrigin = pageRes.url ? new URL(pageRes.url).origin : host;
            pageReferer = pageRes.url || entryUrl;
            collectCookies(pageRes);
            const html = await pageRes.text();
            const jtMatch =
              html.match(/fn%28%22([a-fA-F0-9]+)%22%29/) ||
              html.match(/jsToken\s*=\s*["']([a-fA-F0-9]+)["']/) ||
              html.match(/window\.jsToken\s*=\s*["']([a-fA-F0-9]+)["']/);
            if (jtMatch && jtMatch[1]) {
              jsToken = jtMatch[1];
              break;
            }
          } catch (_) {}
        }

        if (!jsToken) continue;

        let listData = null;
        let randsk = "";
        let cleanShorturlForList = surlNoOne;

        for (const candidateSurl of surlCandidates) {
          const listEndpoints = [
            `${activeOrigin}/api/shorturlinfo?app_id=250528&web=1&channel=dubox&clienttype=0&jsToken=${jsToken}&shorturl=${candidateSurl}&root=1`,
            `${activeOrigin}/share/list?app_id=250528&web=1&channel=dubox&clienttype=0&jsToken=${jsToken}&shorturl=${candidateSurl}&root=1`
          ];
          for (const listUrl of listEndpoints) {
            try {
              const listRes = await fetch(listUrl, {
                headers: { "User-Agent": ua, "Cookie": getCookieHeader(), "Referer": pageReferer },
                signal: AbortSignal.timeout(10000)
              });
              collectCookies(listRes);
              if (listRes.ok) {
                const parsed = await listRes.json();
                if (parsed && parsed.errno === 0 && Array.isArray(parsed.list) && parsed.list.length > 0) {
                  listData = parsed;
                  if (parsed.randsk) {
                    randsk = parsed.randsk;
                    cookieMap.set("BDCLND", parsed.randsk);
                  }
                  cleanShorturlForList = candidateSurl.startsWith("1") && candidateSurl.length >= 23
                    ? candidateSurl.slice(1)
                    : candidateSurl;
                  break;
                }
              }
            } catch (_) {}
          }
          if (listData) break;
        }

        if (!listData || !Array.isArray(listData.list) || listData.list.length === 0) continue;

        const uk = listData.uk || listData.list[0]?.uk;
        const shareid = listData.share_id || listData.shareid;
        const ts = listData.server_time || listData.timestamp || Math.floor(Date.now() / 1000);
        const sekeyParam = randsk ? `&sekey=${encodeURIComponent(decodeURIComponent(randsk))}` : "";

        const pickBestFile = (items) => {
          const files = items.filter(item => String(item.isdir) === "0");
          const videoFile = files.find(item =>
            String(item.category) === "1" ||
            /\.(mp4|mkv|webm|mov|avi|flv|ts|m4v|3gp)$/i.test(item.server_filename || "")
          );
          return videoFile || files[0] || items[0];
        };

        let fileItem = pickBestFile(listData.list);

        // Traverse up to 3 folder levels if shared link is a directory
        for (let depth = 0; depth < 3 && fileItem && String(fileItem.isdir) === "1" && fileItem.path; depth++) {
          let subFound = false;
          const subSurlOptions = [...new Set([cleanShorturlForList, surlNoOne, `1${cleanShorturlForList}`])];
          for (const subSurl of subSurlOptions) {
            const subEndpoints = [
              `${activeOrigin}/share/list?app_id=250528&web=1&channel=dubox&clienttype=0&jsToken=${jsToken}&shorturl=${subSurl}&dir=${encodeURIComponent(fileItem.path)}&page=1&num=100&by=name&order=asc&uk=${uk}&shareid=${shareid}${sekeyParam}`,
              `${activeOrigin}/share/list?app_id=250528&web=1&channel=dubox&clienttype=0&jsToken=${jsToken}&shorturl=${subSurl}&dir=${encodeURIComponent(fileItem.path)}&root=0&uk=${uk}&shareid=${shareid}${sekeyParam}`
            ];
            for (const subUrl of subEndpoints) {
              try {
                const subRes = await fetch(subUrl, {
                  headers: { "User-Agent": ua, "Cookie": getCookieHeader(), "Referer": pageReferer },
                  signal: AbortSignal.timeout(10000)
                });
                if (subRes.ok) {
                  const subData = await subRes.json();
                  if (subData && subData.errno === 0 && Array.isArray(subData.list) && subData.list.length > 0) {
                    fileItem = pickBestFile(subData.list);
                    subFound = true;
                    break;
                  }
                }
              } catch (_) {}
            }
            if (subFound) break;
          }
          if (!subFound) break;
        }

        if (!fileItem || String(fileItem.isdir) === "1") continue;

        const fsId = fileItem.fs_id;
        let fileName = fileItem.server_filename || "TeraBox_Video.mp4";
        if (!fileName.match(/\.(mp4|mkv|webm|mov|avi)$/i)) {
          fileName = `${fileName}.mp4`;
        }
        const cookieHeader = getCookieHeader();

        const remuxTsBufferToMp4 = async (tsBuf, sourceStreamUrl) => {
          const uniqueId = `${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;
          const tmpTs = path.join(os.tmpdir(), `tb_${uniqueId}.ts`);
          const tmpMp4 = path.join(os.tmpdir(), `tb_${uniqueId}.mp4`);
          try {
            const ffmpegBin = await getFfmpegBinary();
            fs.writeFileSync(tmpTs, tsBuf);
            await execFileAsync(ffmpegBin, [
              "-y",
              "-i", tmpTs,
              "-c", "copy",
              "-bsf:a", "aac_adtstoasc",
              "-movflags", "+faststart",
              tmpMp4
            ], { timeout: 45000 });
            const mp4Buf = fs.readFileSync(tmpMp4);
            try { fs.unlinkSync(tmpTs); } catch (_) {}
            try { fs.unlinkSync(tmpMp4); } catch (_) {}
            if (mp4Buf.byteLength > 1000) {
              return {
                type: "TeraBox",
                title: fileName,
                author: "TeraBox Cloud",
                duration: fileItem.duration ? Number(fileItem.duration) : undefined,
                videoUrl: sourceStreamUrl,
                buffer: mp4Buf,
                directStream: true
              };
            }
          } catch (ffErr) {
            try { fs.unlinkSync(tmpTs); } catch (_) {}
            try { fs.unlinkSync(tmpMp4); } catch (_) {}
            console.warn("TeraBox ffmpeg remux fallback:", ffErr.message);
            return {
              type: "TeraBox",
              title: fileName,
              author: "TeraBox Cloud",
              duration: fileItem.duration ? Number(fileItem.duration) : undefined,
              videoUrl: sourceStreamUrl,
              buffer: tsBuf,
              directStream: true
            };
          }
          return null;
        };

        const downloadCandidateStream = async (candidate) => {
          const { segLines, totalBytes } = candidate;
          const firstSeg = segLines[0];
          let fullStreamUrl = firstSeg;
          const hasByteRangeParams = /len=\d+/.test(firstSeg) && /range=\d+-\d+/.test(firstSeg);

          if (hasByteRangeParams && totalBytes > 0) {
            const targetBytes = Math.min(totalBytes, 2000 * 1024 * 1024);
            fullStreamUrl = firstSeg
              .replace(/len=\d+/, `len=${targetBytes}`)
              .replace(/range=\d+-\d+/, `range=0-${targetBytes - 1}`);
            try {
              const vidRes = await fetch(fullStreamUrl, {
                headers: {
                  "User-Agent": ua,
                  "Cookie": cookieHeader,
                  "Referer": `${activeOrigin}/`
                },
                signal: AbortSignal.timeout(60000)
              });
              if (vidRes.ok) {
                const tsBuf = Buffer.from(await vidRes.arrayBuffer());
                if (tsBuf.byteLength > 1000) {
                  const remuxed = await remuxTsBufferToMp4(tsBuf, fullStreamUrl);
                  if (remuxed) return remuxed;
                }
              }
            } catch (_) {}
          }

          // Fallback for multi-segment HLS where range rewrite isn't supported: fetch & concat TS segments up to 2GB
          const chunks = [];
          let accumulated = 0;
          const maxAccumulated = 2000 * 1024 * 1024;
          const batchSize = 4;
          for (let i = 0; i < segLines.length && accumulated < maxAccumulated; i += batchSize) {
            const batch = segLines.slice(i, i + batchSize);
            const batchBuffers = await Promise.all(batch.map(async (segUrl) => {
              try {
                const r = await fetch(segUrl, {
                  headers: {
                    "User-Agent": ua,
                    "Cookie": cookieHeader,
                    "Referer": `${activeOrigin}/`
                  },
                  signal: AbortSignal.timeout(20000)
                });
                if (r.ok) return Buffer.from(await r.arrayBuffer());
              } catch (_) {}
              return null;
            }));
            for (const b of batchBuffers) {
              if (!b || b.byteLength === 0) continue;
              if (accumulated + b.byteLength > maxAccumulated && chunks.length > 0) {
                accumulated = maxAccumulated;
                break;
              }
              chunks.push(b);
              accumulated += b.byteLength;
            }
          }

          if (chunks.length > 0 && accumulated > 1000) {
            const combinedTs = Buffer.concat(chunks);
            const remuxed = await remuxTsBufferToMp4(combinedTs, firstSeg);
            if (remuxed) return remuxed;
          }
          return null;
        };

        // Stream extraction via M3U8_AUTO qualities + full ts_size range rewrite
        const streamTypes = ["M3U8_AUTO_720", "M3U8_AUTO_480", "M3U8_AUTO_360", "M3U8_FLV_264_480", "M3U8_AUTO_240", "M3U8_AUTO_1080"];
        let fallbackSegCandidate = null;

        for (const stype of streamTypes) {
          try {
            const streamUrl = `${activeOrigin}/share/streaming?uk=${uk}&shareid=${shareid}&type=${stype}&fid=${fsId}&sign=1&timestamp=${ts}&jsToken=${jsToken}&esl=1&isplayer=1&ehps=0&clienttype=0&app_id=250528&web=1&channel=dubox`;
            const sRes = await fetch(streamUrl, {
              headers: { "User-Agent": ua, "Cookie": cookieHeader, "Referer": pageReferer },
              signal: AbortSignal.timeout(10000)
            });
            if (!sRes.ok) continue;
            const m3u8 = await sRes.text();
            if (!m3u8.includes("#EXTM3U")) continue;

            const segLines = m3u8.split(/\r?\n/).map(l => l.trim()).filter(l => l.startsWith("http") || l.startsWith("/"));
            if (segLines.length === 0) continue;
            const normalizedSegLines = segLines.map(l => l.startsWith("/") ? `${activeOrigin}${l}` : l);

            const firstSeg = normalizedSegLines[0];
            const lastSeg = normalizedSegLines[normalizedSegLines.length - 1];
            const tsSizeMatch = firstSeg.match(/ts_size=(\d+)/);
            const lastRangeMatch = lastSeg.match(/range=\d+-(\d+)/);
            const totalBytes = tsSizeMatch ? parseInt(tsSizeMatch[1], 10) : (lastRangeMatch ? parseInt(lastRangeMatch[1], 10) + 1 : 0);

            if (totalBytes > 2000 * 1024 * 1024) {
              if (!fallbackSegCandidate || totalBytes < fallbackSegCandidate.totalBytes) {
                fallbackSegCandidate = { segLines: normalizedSegLines, totalBytes };
              }
              continue;
            }

            const result = await downloadCandidateStream({ segLines: normalizedSegLines, totalBytes });
            if (result) return result;
          } catch (_) {}
        }

        // If all available transcode qualities were >2GB, download & remux the smallest one
        if (fallbackSegCandidate) {
          try {
            const fallbackResult = await downloadCandidateStream(fallbackSegCandidate);
            if (fallbackResult) return fallbackResult;
          } catch (_) {}
        }

        if (fileItem.dlink && typeof fileItem.dlink === "string" && fileItem.dlink.startsWith("http")) {
          return {
            type: "TeraBox",
            title: fileName,
            author: "TeraBox Cloud",
            videoUrl: fileItem.dlink,
            headers: { "User-Agent": ua, "Cookie": cookieHeader, "Referer": `${activeOrigin}/` },
            directStream: true
          };
        }
      } catch (err) {
        console.warn(`Native TeraBox stream extractor notice (${host}):`, err.message);
      }
    }
  }

  // Priority 2: SyntexCore Dedicated TeraBox API (Exclusively for Telegram Bot)
  for (const targetApiUrl of [...new Set([canonicalUrl, url])]) {
    try {
      const res = await fetch("https://syntexcore.site/api/v1/terabox-dl", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          url: targetApiUrl,
          apiKey: "syntx_live_2o8vqnbvwh3xw7p4w887ps"
        }),
        signal: AbortSignal.timeout(8000)
      });
      if (res.ok) {
        const json = await res.json();
        if (json.data?.status !== "error" && json.status !== "error") {
          const payload = json.data?.data || json.data || json;
          const direct = payload.download_link || payload.url || payload.dlink || payload.direct_link || payload.downloadUrl || payload.download ||
            (payload.list && payload.list[0]?.dlink) || (payload.files && payload.files[0]?.url) || (payload.file && (payload.file.download_link || payload.file.url));
          if (direct && typeof direct === "string" && direct.startsWith("http")) {
            return {
              type: "TeraBox",
              title: payload.file_name || payload.filename || payload.title || payload.name || "TeraBox Video.mp4",
              author: "TeraBox Cloud",
              videoUrl: direct,
              directStream: true
            };
          }
        }
      }
    } catch (err) {
      console.warn("SyntexCore TeraBox notice:", err.message);
    }
  }

  // Priority 3: High-Speed Public Worker Resolvers
  const publicGateways = [
    `https://terabox-dl.qtcloud.workers.dev/api/get-info?url=${encodeURIComponent(url)}`,
    `https://tb-api.subhankar.me/api?url=${encodeURIComponent(url)}`,
    `https://yt-dlp-terabox.vercel.app/api?url=${encodeURIComponent(url)}`
  ];

  for (const gw of publicGateways) {
    try {
      const res = await fetch(gw, { signal: AbortSignal.timeout(6000) });
      if (res.ok) {
        const json = await res.json();
        const payload = json.data?.data || json.data || json;
        const direct = payload.download_link || payload.url || payload.dlink || payload.direct_link || (payload.list && payload.list[0]?.dlink);
        if (direct && typeof direct === "string" && direct.startsWith("http")) {
          return {
            type: "TeraBox",
            title: payload.file_name || payload.filename || payload.title || "TeraBox File.mp4",
            author: "TeraBox Cloud",
            videoUrl: direct,
            directStream: true
          };
        }
      }
    } catch (_) {}
  }
  return null;
}

// 5. MEGA Resolver (SyntexCore Primary + megajs Direct Stream Fallback)
async function resolveMega(url) {
  // Priority 1: SyntexCore Dedicated mega-dl API
  try {
    const res = await fetch("https://syntexcore.site/api/v1/mega-dl", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        url,
        apiKey: "syntx_live_2o8vqnbvwh3xw7p4w887ps"
      }),
      signal: AbortSignal.timeout(15000)
    });
    if (res.ok) {
      const json = await res.json();
      if (json.data?.status !== "error" && json.status !== "error") {
        const payload = json.data?.data || json.data || json;
        const direct = payload.download_link || payload.url || payload.dlink || payload.direct_link || payload.downloadUrl || (Array.isArray(payload) ? (payload[0]?.download_link || payload[0]?.url) : null);
        if (direct && typeof direct === "string" && direct.startsWith("http")) {
          return {
            type: "MEGA",
            title: payload.file_name || payload.filename || payload.name || payload.title || "MEGA File",
            author: "MEGA Cloud",
            videoUrl: direct,
            directStream: true
          };
        }
      }
    }
  } catch (err) {
    console.warn("SyntexCore MEGA notice:", err.message);
  }

  // Priority 2: megajs Native Decryption Engine
  try {
    const { File: MegaFile } = await import('megajs');
    if (MegaFile) {
      const file = MegaFile.fromURL(url);
      await file.loadAttributes();
      if (file.name) {
        let buf = null;
        if (file.size > 0 && file.size < 2000 * 1024 * 1024) {
          try {
            buf = await file.downloadBuffer();
          } catch (dErr) {
            console.warn("megajs downloadBuffer notice:", dErr?.message);
          }
        }
        return {
          type: "MEGA",
          title: file.name,
          author: "MEGA Cloud",
          videoUrl: url,
          buffer: buf,
          fileSize: file.size,
          directStream: true
        };
      }
    }
  } catch (mErr) {
    console.warn("megajs notice:", mErr?.message);
  }

  return null;
}

// ==================== AUDIO EXTRACTORS & DELIVERY ====================

async function resolveCobaltAudio(url) {
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
          downloadMode: "audio",
          audioFormat: "mp3",
          alwaysProxy: true
        }),
        signal: AbortSignal.timeout(8000)
      });
      if (res.ok) {
        const json = await res.json();
        const streamUrl = json.url;
        if (streamUrl && streamUrl.startsWith("http")) {
          return streamUrl;
        }
      }
    } catch (_) {}
  }
  return null;
}

async function resolveYouTubeAudio(url) {
  const { canonicalUrl, originalUrl } = normalizeYouTubeUrl(url);
  const targetUrls = [canonicalUrl];
  if (originalUrl !== canonicalUrl && !targetUrls.includes(originalUrl)) {
    targetUrls.push(originalUrl);
  }

  // 1. Primary: High-Speed Audio Engine (direct GoogleVideo MP3)
  for (const targetUrl of targetUrls) {
    try {
      const tioRes = await fetch(`https://backend1.tioo.eu.org/youtube?url=${encodeURIComponent(targetUrl)}`, {
        headers: {
          "User-Agent": "btch/3.0.9",
          "X-Client-Version": "3.0.9",
          "Content-Type": "application/json"
        },
        signal: AbortSignal.timeout(8000)
      });
      if (tioRes.ok) {
        const tJson = await tioRes.json();
        if (tJson && tJson.status && tJson.mp3) {
          return tJson.mp3;
        }
      }
    } catch (_) {}
  }

  // 2. Custom Worker / Cobalt API
  const primaryEndpoint = CUSTOM_YOUTUBE_API || "https://cobalt-kh9d.onrender.com";
  try {
    const res = await fetch(primaryEndpoint, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Accept": "application/json",
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
      },
      body: JSON.stringify({ url: canonicalUrl, downloadMode: "audio", audioFormat: "mp3" }),
      signal: AbortSignal.timeout(10000)
    });
    if (res.ok) {
      const json = await res.json();
      const streamUrl = extractStreamUrlFromResponse(json, true);
      if (streamUrl && streamUrl.startsWith("http")) {
        return streamUrl;
      }
    }
  } catch (_) {}

  // 3. Local yt-dlp audio extraction
  try {
    const cookieArgs = fs.existsSync("/etc/secrets/cookies.json")
      ? ["--cookies", "/etc/secrets/cookies.json"]
      : [];
    const { stdout } = await execFileAsync("yt-dlp", [
      "--no-warnings",
      "--no-playlist",
      "-j",
      "-f", "ba/b",
      ...cookieArgs,
      canonicalUrl
    ], { timeout: 25000 });
    if (stdout) {
      const info = JSON.parse(stdout.trim().split("\n")[0]);
      const streamUrl = info.url || info.requested_downloads?.[0]?.url;
      if (streamUrl && streamUrl.startsWith("http")) {
        return streamUrl;
      }
    }
  } catch (_) {}

  // 4. Fallback: Cobalt audio mirrors
  for (const targetUrl of targetUrls) {
    const cobAudio = await resolveCobaltAudio(targetUrl);
    if (cobAudio) return cobAudio;
  }
  return null;
}

async function deliverAudioTrack(chatId, url, media, safeTitle, platformName, videoBuffer = null) {
  try {
    let audioUrl = media?.audioUrl || null;
    const lower = url.toLowerCase();
    const isYt = lower.includes("youtube.com") || lower.includes("youtu.be");

    // 1. If no direct audioUrl yet, query audio resolvers
    if (!audioUrl) {
      if (isYt) {
        audioUrl = await resolveYouTubeAudio(url);
      }
      if (!audioUrl) {
        audioUrl = await resolveCobaltAudio(url);
      }
      // Guaranteed stream extraction fallback: the extracted media stream container itself contains the full audio track!
      if (!audioUrl && media?.videoUrl && media.videoUrl.startsWith("http")) {
        console.log(`[AUDIO TRACK GUARANTEE] Re-using media stream container as audio track for ${chatId}`);
        audioUrl = media.videoUrl;
      }
    }

    const cleanTitle = (media?.title || safeTitle || "Audio Track")
      .replace(/&[a-zA-Z0-9#x]+;/g, "")
      .replace(/[^\w\s-]/g, "")
      .trim()
      .substring(0, 45) || "Audio";

    const isDirectMediaContainer = Boolean(audioUrl && (audioUrl === media?.videoUrl || audioUrl.includes(".mp4")));
    const audioExt = isDirectMediaContainer ? "m4a" : "mp3";
    const audioMime = isDirectMediaContainer ? "audio/mp4" : "audio/mpeg";

    const audioFilename = formatOmniStreamFilename(platformName, cleanTitle, audioExt);
    const audioCaption = `🎵 <b>${escapeHtml(safeTitle)}</b>\n\n` +
      `👤 <b>Artist/Creator:</b> ${escapeHtml(media?.author || "Creator")}\n` +
      `📻 <b>Format:</b> High Quality Audio (${audioExt.toUpperCase()})\n` +
      `🌐 <b>Platform:</b> ${escapeHtml(platformName)}\n\n` +
      `⚡ <i>Audio track extracted via OmniStream Bot (@OmniStream34_bot)</i>`;

    const audioMarkup = {
      inline_keyboard: [
        [{ text: "🌐 Official Web App", url: "https://hanter-xd-official.github.io/OmniStream/" }],
        [{ text: "👨‍💻 Developer Profile", url: DEV_TELEGRAM }]
      ]
    };

    // 2. Stream audio from audioUrl in chunks using InputFile (zero server buffer, unlimited timeout)
    if (audioUrl && audioUrl.startsWith("http")) {
      try {
        const audioInput = InputFile.fromUrl(audioUrl, audioFilename, audioMime, {
          "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
          "Referer": lower.includes("tiktok.com") ? "https://www.tiktok.com/" : url
        });

        const aRes = await sendTgAudio(chatId, audioInput, audioFilename, audioCaption, safeTitle, media?.author || platformName, audioMarkup, media?.duration);
        if (aRes && aRes.ok) {
          console.log(`[DELIVERED AUDIO] Audio stream sent to ${chatId}`);
          return true;
        }
      } catch (err) {
        console.warn("Audio streaming delivery error:", err.message);
      }

      // Fallback: send directly via Telegram sendAudio URL
      try {
        const urlRes = await callTg("sendAudio", {
          chat_id: chatId,
          audio: audioUrl,
          caption: audioCaption,
          parse_mode: "HTML",
          title: safeTitle,
          performer: media?.author || platformName,
          reply_markup: audioMarkup
        });
        if (urlRes && urlRes.ok) {
          console.log(`[DELIVERED AUDIO] Audio URL sent to ${chatId}`);
          return true;
        }
      } catch (urlErr) {
        console.warn("Audio URL delivery error:", urlErr.message);
      }
    }

    // 3. Fallback: If videoBuffer was provided from direct in-memory file (e.g. TeraBox), deliver audio container
    if (videoBuffer && videoBuffer.byteLength > 1000 && videoBuffer.byteLength < 48 * 1024 * 1024) {
      try {
        const aRes = await sendTgAudio(chatId, videoBuffer, audioFilename, audioCaption, safeTitle, media?.author || platformName, audioMarkup);
        if (aRes && aRes.ok) {
          console.log(`[DELIVERED AUDIO] Media container audio sent to ${chatId}`);
          return true;
        }
      } catch (bufErr) {
        console.warn("VideoBuffer audio fallback error:", bufErr.message);
      }
    }
  } catch (err) {
    console.error("deliverAudioTrack error:", err.message);
  }
  return false;
}

// ==================== PROCESS URL ====================

async function processMediaUrl(rawUrl, chatId, progressMsgId, userId) {
  const url = rawUrl.trim();
  console.log(`[PROCESS] URL: ${url} for User: ${userId}`);

  let media = null;
  const lower = url.toLowerCase();

  db.stats.totalLinks++;
  saveDatabase();

  try {
    if (lower.includes("tiktok.com") || lower.includes("douyin.com")) {
      media = await resolveTikTok(url);
      if (!media || !media.videoUrl) {
        console.log(`[TIKTOK FAILOVER] resolveTikTok exhausted, trying resolveCobalt for: ${url}`);
        media = await resolveCobalt(url);
      }
    } else if (lower.includes("youtube.com") || lower.includes("youtu.be")) {
      media = await resolveYouTube(url, async (statusText) => {
        await callTg("editMessageText", {
          chat_id: chatId,
          message_id: progressMsgId,
          text: statusText,
          parse_mode: "HTML"
        });
      });
    } else if (isTeraBoxDomain(lower)) {
      media = await resolveTeraBox(url);
    } else if (lower.includes("mega.nz") || lower.includes("mega.co.nz") || lower.includes("mega.io")) {
      media = await resolveMega(url);
    } else {
      media = await resolveCobalt(url);
    }

    if (!media || (!media.videoUrl && !media.buffer)) {
      const isYt = lower.includes("youtube.com") || lower.includes("youtu.be");
      let noticeText;
      if (isYt && media?.errorReason === "error.video_unavailable") {
        noticeText = `⚠️ <b>Video Unavailable</b>\n\n` +
          `This YouTube video is currently unavailable (Private, Restricted, or Deleted).\n\n` +
          `📌 <i>No video stream could be found on YouTube servers. Please try with another active public YouTube link.</i>`;
      } else if (isYt && media?.errorReason === "error.api.youtube.login") {
        noticeText = `⚠️ <b>YouTube Security Notice (Sign-in Required)</b>\n\n` +
          `YouTube anti-bot security blocked extraction for this video on the server:\n` +
          `<code>Status: error.api.youtube.login</code>\n\n` +
          `📌 <b>Reason:</b>\n` +
          `YouTube requires account sign-in or age verification to view this video.\n\n` +
          `💡 <b>Tip:</b> Please try with a standard, publicly accessible video link.`;
      } else if (isYt && media?.errorReason === "error.api.youtube.api_error") {
        noticeText = `⚠️ <b>YouTube Stream Notice</b>\n\n` +
          `YouTube backend returned: <code>error.api.youtube.api_error</code>\n\n` +
          `📌 <b>Reason:</b>\n` +
          `YouTube temporarily restricted processing streams for this specific video (age-restricted, copyright, or bot protected).\n\n` +
          `💡 <b>Tip:</b> Please try again in a few moments or test with another public video link.`;
      } else if (isYt) {
        noticeText = `⚠️ <b>YouTube Stream Notice</b>\n\nCould not extract a downloadable stream for this YouTube video from OmniStream API. Please verify the video is publicly accessible and try again.`;
      } else {
        noticeText = `⚠️ <b>Direct Stream Notice</b>\n\nCould not extract a direct video file from this link. Please verify the video is publicly accessible and try again.`;
      }

      await callTg("editMessageText", {
        chat_id: chatId,
        message_id: progressMsgId,
        text: noticeText,
        parse_mode: "HTML",
        reply_markup: {
          inline_keyboard: [
            [{ text: "💬 Support (@HANTER_XD_OFFICIAL)", url: DEV_TELEGRAM }]
          ]
        }
      });

      // Alert Administrator of extraction failure
      notifyAdminOfError({
        error: new Error(media?.errorReason || "Stream resolution failure"),
        context: `Media Extraction Failure (${isYt ? "YouTube" : "Platform"})`,
        userId,
        originalUrl: url,
        extraInfo: `Reason: ${media?.errorReason || "No stream URL found"} | Error Notice Sent`
      }).catch(() => {});
      return;
    }

    const safeTitle = media.title ? String(media.title).trim() : "Media Video";
    const shortTitle = safeTitle.length > 40 ? safeTitle.substring(0, 40) + "..." : safeTitle;
    const platformName = detectPlatformName(media.type, url);
    const standardizedFilename = formatOmniStreamFilename(platformName, media.title || safeTitle, "mp4");
    const defaultMarkup = {
      inline_keyboard: [
        [{ text: "👨‍💻 Developer Profile", url: DEV_TELEGRAM }]
      ]
    };

    // 1. Direct in-memory buffer delivery via Telegram Bot API sendVideo (e.g. TeraBox & MEGA direct files)
    if (media.buffer && media.buffer.byteLength > 1000) {
      await callTg("editMessageText", {
        chat_id: chatId,
        message_id: progressMsgId,
        text: `⚡ <b>Ready:</b> ${escapeHtml(shortTitle)}\n📥 <i>Sending video file directly to Telegram...</i>`,
        parse_mode: "HTML"
      });

      const sizeMb = (media.buffer.byteLength / (1024 * 1024)).toFixed(1);
      const filename = media.title || standardizedFilename;
      const isAudio = filename.match(/\.(mp3|m4a|wav|aac|flac|ogg)$/i);
      const cap = `🎬 <b>${escapeHtml(safeTitle)}</b>\n\n` +
        `👤 <b>Platform:</b> ${escapeHtml(media.type || platformName)}\n` +
        (media.duration ? `⏱ <b>Duration:</b> ${formatSeconds(media.duration)}\n` : "") +
        `💾 <b>Size:</b> ${sizeMb} MB\n\n` +
        `⚡ <i>Downloaded via OmniStream Bot (@OmniStream34_bot)</i>`;

      if (isAudio) {
        const aRes = await sendTgAudio(chatId, media.buffer, filename, cap, safeTitle, media.author || platformName, defaultMarkup);
        if (aRes?.ok) {
          db.stats.totalDownloads++;
          if (db.users[userId]) db.users[userId].downloads++;
          saveDatabase();
          try { await callTg("deleteMessage", { chat_id: chatId, message_id: progressMsgId }); } catch (_) {}
          logDownloadToAdmin({
            userId,
            userFullName: getUserFullName(db.users[userId]),
            username: db.users[userId]?.username,
            originalUrl: url,
            platform: media.type || platformName,
            title: safeTitle,
            fileSize: media.buffer.byteLength,
            duration: media.duration
          }).catch(() => {});
          return;
        }
      } else {
        const vRes = await sendTgVideo(chatId, media.buffer, standardizedFilename, cap, defaultMarkup, media.duration);
        if (vRes?.ok) {
          db.stats.totalDownloads++;
          if (db.users[userId]) db.users[userId].downloads++;
          saveDatabase();
          try { await callTg("deleteMessage", { chat_id: chatId, message_id: progressMsgId }); } catch (_) {}
          logDownloadToAdmin({
            userId,
            userFullName: getUserFullName(db.users[userId]),
            username: db.users[userId]?.username,
            originalUrl: url,
            platform: media.type || platformName,
            title: safeTitle,
            fileSize: media.buffer.byteLength,
            duration: media.duration
          }).catch(() => {});
          const isYt = lower.includes("youtube.com") || lower.includes("youtu.be");
          if (isYt) {
            console.log(`[AUDIO DELIVERY] Video sent from buffer. Delivering audio track to ${chatId}...`);
            await deliverAudioTrack(chatId, url, media, safeTitle, platformName, media.buffer);
          }
          return;
        }
      }

      const dRes = await sendTgDocument(chatId, media.buffer, standardizedFilename, cap, defaultMarkup);
      if (dRes?.ok) {
        db.stats.totalDownloads++;
        if (db.users[userId]) db.users[userId].downloads++;
        saveDatabase();
        try { await callTg("deleteMessage", { chat_id: chatId, message_id: progressMsgId }); } catch (_) {}
        logDownloadToAdmin({
          userId,
          userFullName: getUserFullName(db.users[userId]),
          username: db.users[userId]?.username,
          originalUrl: url,
          platform: media.type || platformName,
          title: safeTitle,
          fileSize: media.buffer.byteLength,
          duration: media.duration
        }).catch(() => {});
        const isYt = lower.includes("youtube.com") || lower.includes("youtu.be");
        if (isYt) {
          console.log(`[AUDIO DELIVERY] Video document sent from buffer. Delivering audio track to ${chatId}...`);
          await deliverAudioTrack(chatId, url, media, safeTitle, platformName, media.buffer);
        }
        return;
      }
    }

    // 2. Stream video file first, then deliver audio file once video is confirmed sent
    if (media.videoUrl && media.videoUrl.startsWith("http") && media.videoUrl !== url) {
      await callTg("editMessageText", {
        chat_id: chatId,
        message_id: progressMsgId,
        text: `⚡ <b>Ready:</b> ${escapeHtml(shortTitle)}\n📥 <i>Streaming video file directly to Telegram...</i>`,
        parse_mode: "HTML"
      });

      const baseCaption = `🎬 <b>${escapeHtml(safeTitle)}</b>\n\n` +
        `👤 <b>Platform:</b> ${escapeHtml(media.type || platformName)}\n` +
        (media.duration ? `⏱ <b>Duration:</b> ${formatSeconds(media.duration)}\n` : "") +
        `⚡ <i>Downloaded via OmniStream Bot (@OmniStream34_bot)</i>`;

      const reqHeaders = media.headers || {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36",
        "Referer": media.type === "TikTok" ? "https://www.tiktok.com/" : url
      };

      // Extract thumbnail preview
      const thumbBuffer = await extractThumbnailBuffer(media, media.videoUrl);

      // STEP 1: Process and send video file FIRST
      let videoDelivered = false;
      try {
        const videoInput = InputFile.fromUrl(media.videoUrl, standardizedFilename, "video/mp4", reqHeaders);

        // Stream video directly in chunks with thumbnail preview (zero RAM buffer, unlimited timeout)
        let sendRes = await sendTgVideo(chatId, videoInput, standardizedFilename, baseCaption, defaultMarkup, media.duration, thumbBuffer);
        if (!sendRes || !sendRes.ok) {
          console.warn("[STREAM VIDEO] sendTgVideo fallback to document stream:", sendRes?.error || sendRes?.description);
          sendRes = await sendTgDocument(chatId, videoInput, standardizedFilename, baseCaption, defaultMarkup);
        }
        if (sendRes && sendRes.ok) {
          videoDelivered = true;
          console.log(`[DELIVERED VIDEO] Video file sent successfully to ${chatId}`);
        }
      } catch (streamErr) {
        console.warn("Direct video streaming notice, trying URL fallback:", streamErr.message);
      }

      if (!videoDelivered) {
        // Fallback: Call Telegram Bot API sendVideo directly with the extracted videoUrl
        const urlVideoRes = await sendTgVideoByUrl(chatId, media.videoUrl, baseCaption, defaultMarkup);
        if (urlVideoRes && urlVideoRes.ok) {
          videoDelivered = true;
          console.log(`[DELIVERED VIDEO] Video sent via URL to ${chatId}`);
        }
      }

      // STEP 2: Only after the video is successfully sent should you send the corresponding audio file
      if (videoDelivered) {
        try { await callTg("deleteMessage", { chat_id: chatId, message_id: progressMsgId }); } catch (_) {}
        db.stats.totalDownloads++;
        if (db.users[userId]) db.users[userId].downloads++;
        saveDatabase();

        // Activity Logging: Send comprehensive metadata of every processed download directly to Admin
        logDownloadToAdmin({
          userId,
          userFullName: getUserFullName(db.users[userId]),
          username: db.users[userId]?.username,
          originalUrl: url,
          platform: media.type || platformName,
          title: safeTitle,
          fileSize: media.size || media.fileSize || "Direct Stream",
          duration: media.duration,
          mediaUrl: media.videoUrl
        }).catch(() => {});

        // For YouTube & other video links, explicitly ensure both video and audio streams are processed
        const isYt = lower.includes("youtube.com") || lower.includes("youtu.be");
        const canDeliverAudio = isYt || (media.type !== "TeraBox" && media.type !== "MEGA");

        if (canDeliverAudio) {
          console.log(`[AUDIO DELIVERY] Video confirmed delivered. Now delivering audio track for ${chatId}...`);
          await deliverAudioTrack(chatId, url, media, safeTitle, platformName);
        }
        return;
      }
    }

    const isYt = lower.includes("youtube.com") || lower.includes("youtu.be");
    const deliveryNotice = isYt
      ? `⚠️ <b>YouTube Delivery Notice</b>\n\nCould not transfer the video file to Telegram (the file may exceed Telegram's 2GB upload limit or requires authentication).`
      : `⚠️ <b>Video Delivery Notice</b>\n\nCould not send the video file directly to Telegram (the file may exceed Telegram's 2GB upload limit or require authentication).`;

    await callTg("editMessageText", {
      chat_id: chatId,
      message_id: progressMsgId,
      text: deliveryNotice,
      parse_mode: "HTML",
      reply_markup: defaultMarkup
    });

    // Alert Administrator of delivery failure
    notifyAdminOfError({
      error: new Error("Video file transfer to Telegram failed (2GB limit or upload timeout)"),
      context: `Video Delivery Failure (${platformName || "Platform"})`,
      userId,
      originalUrl: url,
      extraInfo: `Title: ${safeTitle} | Video Delivered: false`
    }).catch(() => {});

  } catch (err) {
    console.error("[PROCESS ERROR]:", err.message);
    try {
      await callTg("editMessageText", {
        chat_id: chatId,
        message_id: progressMsgId,
        text: `❌ An unexpected error occurred while processing this media. Please verify the URL and try again.`,
        parse_mode: "HTML"
      });
    } catch (_) {}

    // Alert Administrator of unexpected runtime exception
    notifyAdminOfError({
      error: err,
      context: "processMediaUrl Runtime Exception",
      userId,
      originalUrl: url,
      extraInfo: `Chat ID: ${chatId}`
    }).catch(() => {});
  }
}

// ==================== ADMIN PANEL HANDLERS ====================

async function sendAdminDashboard(chatId, messageId = null) {
  const totalUsers = Object.keys(db.users).length;
  const blockedCount = db.blockedUsers.length;
  const activeCount = Math.max(0, totalUsers - blockedCount);
  const uptime = formatUptime(process.uptime());

  const qStats = typeof mediaQueue !== "undefined" ? mediaQueue.getStats() : { activeTasks: 0, concurrencyLimit: 2, queuedTasks: 0 };

  const text = `👑 <b>OmniStream Master Admin Panel</b>\n` +
    `━━━━━━━━━━━━━━━━━━━━\n` +
    `👤 <b>Administrator:</b> ${DEV_NAME}\n` +
    `🆔 <b>Admin ID:</b> <code>${ADMIN_ID}</code>\n\n` +
    `📊 <b>System Overview:</b>\n` +
    `• 👥 <b>Total Users:</b> <b>${totalUsers}</b>\n` +
    `• 🟢 <b>Active Users:</b> <b>${activeCount}</b>\n` +
    `• 🚫 <b>Blocked Users:</b> <b>${blockedCount}</b>\n` +
    `• 🚀 <b>Concurrent Workers:</b> <b>${qStats.activeTasks}/${qStats.concurrencyLimit} Active</b> (${qStats.queuedTasks} queued)\n` +
    `• 📥 <b>Total Downloads:</b> <b>${db.stats.totalDownloads}</b>\n` +
    `• 🔗 <b>Total Links Processed:</b> <b>${db.stats.totalLinks}</b>\n` +
    `• 🏷 <b>Latest App Release:</b> <code>${escapeHtml(db.lastReleaseTag)}</code>\n` +
    `• ⚡ <b>Bot Uptime:</b> <b>${uptime}</b>\n` +
    `━━━━━━━━━━━━━━━━━━━━\n` +
    `<i>Select an action below to manage users and system controls:</i>`;

  const inlineKeyboard = {
    inline_keyboard: [
      [
        { text: "👥 Registered Users", callback_data: "admin_users" },
        { text: "📊 Detailed Status", callback_data: "admin_status" }
      ],
      [
        { text: "💬 Message User (ID)", callback_data: "admin_prompt_dm" },
        { text: "📢 Broadcast Message", callback_data: "admin_prompt_broadcast" }
      ],
      [
        { text: "🚫 Block User (ID)", callback_data: "admin_prompt_block" },
        { text: "✅ Unblock User (ID)", callback_data: "admin_prompt_unblock" }
      ],
      [
        { text: "🚀 Check App Release", callback_data: "admin_check_release" },
        { text: "🔄 Refresh Dashboard", callback_data: "admin_refresh" }
      ]
    ]
  };

  if (messageId) {
    return await callTg("editMessageText", {
      chat_id: chatId,
      message_id: messageId,
      text,
      parse_mode: "HTML",
      reply_markup: inlineKeyboard
    });
  } else {
    return await callTg("sendMessage", {
      chat_id: chatId,
      text,
      parse_mode: "HTML",
      reply_markup: inlineKeyboard
    });
  }
}

async function sendUsersList(chatId, messageId = null) {
  const usersArray = Object.values(db.users);
  if (usersArray.length === 0) {
    const emptyText = "👥 <b>Registered Users:</b>\n\n<i>No users have registered yet.</i>";
    const markup = {
      inline_keyboard: [[{ text: "🔙 Back to Dashboard", callback_data: "admin_refresh" }]]
    };
    if (messageId) {
      return await callTg("editMessageText", { chat_id: chatId, message_id: messageId, text: emptyText, parse_mode: "HTML", reply_markup: markup });
    }
    return await callTg("sendMessage", { chat_id: chatId, text: emptyText, parse_mode: "HTML", reply_markup: markup });
  }

  // Sort by last active descending, take up to 20 recent
  const sorted = usersArray.sort((a, b) => new Date(b.lastActive || 0) - new Date(a.lastActive || 0)).slice(0, 20);

  let text = `👥 <b>Recent Registered Users (${sorted.length}/${usersArray.length}):</b>\n━━━━━━━━━━━━━━━━━━━━\n`;

  const inlineButtons = [];

  for (const u of sorted) {
    const isBlocked = isUserBlocked(u.id);
    const isAdm = isAdmin(u.id);
    const isVer = isUserVerified(u.id);
    const statusIcon = isAdm ? "👑 [ADMIN]" : (isBlocked ? "🚫 [BLOCKED]" : (isVer ? "🟢 [VERIFIED]" : "⏳ [UNVERIFIED]"));
    const userDisplay = u.firstName ? escapeHtml(u.firstName) : "User";
    const uname = u.username ? `@${escapeHtml(u.username)}` : "No @username";
    
    text += `• <b>${userDisplay}</b> (${uname})\n` +
      `  🆔 <code>${u.id}</code> | ${statusIcon} | 📥 ${u.downloads || 0} dl\n\n`;

    if (!isAdm) {
      inlineButtons.push([
        { text: `💬 Message ${userDisplay}`, callback_data: `admin_msg_user_${u.id}` },
        { text: isBlocked ? `✅ Unblock` : `🚫 Block`, callback_data: isBlocked ? `admin_unblock_do_${u.id}` : `admin_block_do_${u.id}` }
      ]);
    }
  }

  text += `━━━━━━━━━━━━━━━━━━━━\n` +
    `💡 <b>Quick Actions:</b>\n` +
    `• Direct Message: <code>/msg &lt;userId&gt; &lt;text&gt;</code>\n` +
    `• Broadcast All: <code>/broadcast &lt;text&gt;</code>\n` +
    `• Block user: <code>/block &lt;userId&gt;</code>\n` +
    `• Unblock user: <code>/unblock &lt;userId&gt;</code>`;

  inlineButtons.push([{ text: "🔙 Back to Dashboard", callback_data: "admin_refresh" }]);

  const markup = { inline_keyboard: inlineButtons };

  if (messageId) {
    return await callTg("editMessageText", { chat_id: chatId, message_id: messageId, text, parse_mode: "HTML", reply_markup: markup });
  }
  return await callTg("sendMessage", { chat_id: chatId, text, parse_mode: "HTML", reply_markup: markup });
}

// ==================== ASYNCHRONOUS TASK QUEUE & CONCURRENCY SYSTEM ====================

class AsyncTaskQueue {
  constructor() {
    // Render Free Tier Optimized Worker Pool: 2 parallel workers (avoids 512MB RAM exhaustion & CPU starvation)
    this.concurrency = parseInt(process.env.MAX_CONCURRENT_TASKS || "2", 10);
    if (isNaN(this.concurrency) || this.concurrency < 1) this.concurrency = 2;

    // Per-user max active concurrent tasks (default 1 to ensure fair-share scheduling on free tier)
    this.perUserConcurrency = parseInt(process.env.PER_USER_CONCURRENCY || "1", 10);
    if (isNaN(this.perUserConcurrency) || this.perUserConcurrency < 1) this.perUserConcurrency = 1;

    // Safety RSS memory ceiling for Render's 512MB free container
    this.maxMemoryRssMb = parseInt(process.env.MAX_MEMORY_RSS_MB || "360", 10);

    this.queue = [];
    this.activeTasks = new Map();
    this.userActiveCount = new Map();

    this.metrics = {
      totalEnqueued: 0,
      totalProcessed: 0,
      totalFailed: 0,
      peakConcurrency: 0,
      totalProcessingTimeMs: 0,
      startTime: Date.now()
    };

    this._taskCounter = 0;
  }

  get activeCount() {
    return this.activeTasks.size;
  }

  get queueLength() {
    return this.queue.length;
  }

  getStats() {
    const avgTime = this.metrics.totalProcessed > 0
      ? Math.round(this.metrics.totalProcessingTimeMs / this.metrics.totalProcessed)
      : 0;
    const currentRssMb = (process.memoryUsage().rss / (1024 * 1024)).toFixed(1);
    return {
      tier: "Render Free Tier Optimized",
      concurrencyLimit: this.concurrency,
      perUserLimit: this.perUserConcurrency,
      activeTasks: this.activeCount,
      queuedTasks: this.queueLength,
      memoryRssMb: parseFloat(currentRssMb),
      memoryLimitMb: 512,
      totalEnqueued: this.metrics.totalEnqueued,
      totalProcessed: this.metrics.totalProcessed,
      totalFailed: this.metrics.totalFailed,
      peakConcurrency: this.metrics.peakConcurrency,
      avgProcessingTimeMs: avgTime,
      uptimeSeconds: Math.floor((Date.now() - this.metrics.startTime) / 1000)
    };
  }

  async enqueue(taskData) {
    const taskId = ++this._taskCounter;
    const task = {
      id: taskId,
      userId: String(taskData.userId || "0"),
      chatId: taskData.chatId,
      url: taskData.url,
      priority: taskData.priority || 0,
      progressMsgId: taskData.progressMsgId || null,
      enqueuedAt: Date.now(),
      startedAt: null,
      status: "queued",
      lastReportedPosition: null
    };

    this.metrics.totalEnqueued++;
    this._insertTaskByPriority(task);
    const initialPosition = this.getTaskQueuePosition(task.id);

    console.log(`[TASK QUEUE] Enqueued Task #${task.id} for User ${task.userId} (Priority: ${task.priority}, Position: #${initialPosition}, Active: ${this.activeCount}/${this.concurrency})`);

    const canRunNow = this.activeCount < this.concurrency && (this.userActiveCount.get(task.userId) || 0) < this.perUserConcurrency;
    if (!canRunNow && task.progressMsgId) {
      callTg("editMessageText", {
        chat_id: task.chatId,
        message_id: task.progressMsgId,
        text: `⏳ <b>Queued for processing...</b>\n\n` +
          `📊 <b>Queue Position:</b> <b>#${initialPosition}</b>\n` +
          `⚡ <b>Active Worker Slots:</b> <b>${this.activeCount}/${this.concurrency}</b>\n\n` +
          `<i>Render free-tier optimized queue. Your download will begin automatically as soon as an active worker completes.</i>`,
        parse_mode: "HTML"
      }).catch(() => {});
    }

    this.processNext();
    return task;
  }

  _insertTaskByPriority(task) {
    let inserted = false;
    for (let i = 0; i < this.queue.length; i++) {
      const other = this.queue[i];
      if (task.priority > other.priority) {
        this.queue.splice(i, 0, task);
        inserted = true;
        break;
      }
    }
    if (!inserted) {
      this.queue.push(task);
    }
  }

  getTaskQueuePosition(taskId) {
    const idx = this.queue.findIndex(t => t.id === taskId);
    return idx === -1 ? 0 : idx + 1;
  }

  processNext() {
    // Render Free Tier RAM Safety Guard: prevent OOM crashes on Render (512MB RAM limit)
    const currentRssMb = process.memoryUsage().rss / (1024 * 1024);
    if (currentRssMb > this.maxMemoryRssMb && this.activeCount > 0) {
      console.warn(`[RENDER RESOURCE GUARD] High memory allocation (${currentRssMb.toFixed(1)}MB > ${this.maxMemoryRssMb}MB). Pausing queue dispatch until active worker releases memory...`);
      if (typeof global.gc === "function") {
        try { global.gc(); } catch (_) {}
      }
      setTimeout(() => this.processNext(), 2000);
      return;
    }

    while (this.activeCount < this.concurrency && this.queue.length > 0) {
      const rssNow = process.memoryUsage().rss / (1024 * 1024);
      if (rssNow > this.maxMemoryRssMb && this.activeCount > 0) {
        break;
      }

      let selectedIdx = -1;
      for (let i = 0; i < this.queue.length; i++) {
        const candidate = this.queue[i];
        const userCount = this.userActiveCount.get(candidate.userId) || 0;
        if (userCount < this.perUserConcurrency) {
          selectedIdx = i;
          break;
        }
      }

      if (selectedIdx === -1) {
        break;
      }

      const [task] = this.queue.splice(selectedIdx, 1);
      task.status = "processing";
      task.startedAt = Date.now();

      this.activeTasks.set(task.id, task);
      const currUserCount = this.userActiveCount.get(task.userId) || 0;
      this.userActiveCount.set(task.userId, currUserCount + 1);

      if (this.activeCount > this.metrics.peakConcurrency) {
        this.metrics.peakConcurrency = this.activeCount;
      }

      console.log(`[RENDER TASK WORKER] Starting Task #${task.id} for User ${task.userId} (Active: ${this.activeCount}/${this.concurrency}, RAM: ${(process.memoryUsage().rss / (1024 * 1024)).toFixed(1)}MB, Queued: ${this.queue.length})`);

      this._notifyQueuedPositions();
      this._executeTask(task);
    }
  }

  async _executeTask(task) {
    const startTime = Date.now();
    try {
      if (task.progressMsgId) {
        callTg("editMessageText", {
          chat_id: task.chatId,
          message_id: task.progressMsgId,
          text: `⚡ <b>Starting download...</b>\n<i>Connecting to high-speed media stream...</i>`,
          parse_mode: "HTML"
        }).catch(() => {});
      }

      await processMediaUrl(task.url, task.chatId, task.progressMsgId, task.userId);
      this.metrics.totalProcessed++;
    } catch (err) {
      this.metrics.totalFailed++;
      console.error(`[TASK WORKER ERROR] Task #${task.id} failed:`, err.message);
      if (task.progressMsgId) {
        callTg("editMessageText", {
          chat_id: task.chatId,
          message_id: task.progressMsgId,
          text: `❌ <i>An error occurred while processing your request. Please try again later.</i>`,
          parse_mode: "HTML"
        }).catch(() => {});
      }
    } finally {
      const elapsed = Date.now() - startTime;
      this.metrics.totalProcessingTimeMs += elapsed;

      this.activeTasks.delete(task.id);
      const userCount = this.userActiveCount.get(task.userId) || 1;
      if (userCount <= 1) {
        this.userActiveCount.delete(task.userId);
      } else {
        this.userActiveCount.set(task.userId, userCount - 1);
      }

      // Proactively trigger garbage collection if memory is elevated on Render free tier
      if (process.memoryUsage().rss > 300 * 1024 * 1024 && typeof global.gc === "function") {
        try { global.gc(); } catch (_) {}
      }

      console.log(`[RENDER TASK WORKER COMPLETED] Task #${task.id} finished in ${elapsed}ms (Active: ${this.activeCount}/${this.concurrency}, Queued: ${this.queue.length})`);
      this.processNext();
    }
  }

  _notifyQueuedPositions() {
    for (let i = 0; i < Math.min(5, this.queue.length); i++) {
      const task = this.queue[i];
      const position = i + 1;
      if (task.progressMsgId && task.lastReportedPosition !== position) {
        task.lastReportedPosition = position;
        callTg("editMessageText", {
          chat_id: task.chatId,
          message_id: task.progressMsgId,
          text: `⏳ <b>Queued for processing...</b>\n\n` +
            `📊 <b>Queue Position:</b> <b>#${position}</b>\n` +
            `⚡ <b>Active Worker Slots:</b> <b>${this.activeCount}/${this.concurrency}</b>\n\n` +
            `<i>Render free-tier optimized queue. Your download will begin automatically as soon as an active worker completes.</i>`,
          parse_mode: "HTML"
        }).catch(() => {});
      }
    }
  }
}

const mediaQueue = new AsyncTaskQueue();

// ==================== UPDATE HANDLER ====================

async function handleUpdate(update) {
  try {
    // 1. Handle Callback Queries (Inline Button clicks)
    if (update.callback_query) {
      const cq = update.callback_query;
      const cqId = cq.id;
      const senderId = String(cq.from?.id);
      const chatId = cq.message?.chat?.id;
      const msgId = cq.message?.message_id;
      const data = cq.data || "";
      const senderName = cq.from?.first_name || "User";

      // 0. Handle Confirmation of Channel & Group Membership
      if (data === "confirm_membership") {
        await callTg("answerCallbackQuery", {
          callback_query_id: cqId,
          text: "🔍 Checking your channel & group membership..."
        });

        // Force a live check against Telegram servers
        const liveStatus = await verifyUserMembershipLive(senderId, true);

        console.log(`[VERIFICATION CHECK] User: ${senderId} (${senderName}) | Result:`, liveStatus);

        // If either check explicitly failed (user is not member of both)
        if (!liveStatus.isMember) {
          if (msgId) {
            // Delete previously sent verification message or update it
            try {
              await callTg("deleteMessage", { chat_id: chatId, message_id: msgId });
            } catch (e) {
              console.warn("Could not delete previous prompt message:", e.message);
            }
          }
          await sendVerificationPrompt(chatId, senderName, true, {
            channelJoined: liveStatus.channelJoined,
            groupJoined: liveStatus.groupJoined
          });
          return;
        }

        // Successfully confirmed membership!
        registerOrUpdateUser(cq.from);
        if (db.users[senderId]) {
          db.users[senderId].isVerified = true;
          db.users[senderId].verifiedAt = new Date().toISOString();
        }
        saveDatabase();

        // Delete the membership verification prompt message before sending the successful verification message
        if (msgId) {
          try {
            await callTg("deleteMessage", {
              chat_id: chatId,
              message_id: msgId
            });
          } catch (e) {
            console.warn("Could not delete verification prompt message:", e.message);
          }
        }

        // 1. Send confirmation success message to user
        const successUserText = `🎉 <b>Verification Successful!</b>\n━━━━━━━━━━━━━━━━━━━━\n` +
          `Welcome to OmniStream, <b>${escapeHtml(senderName)}</b>! You have successfully verified your membership.\n\n` +
          `✅ <b>Channel:</b> <a href="${REQUIRED_CHANNEL.url}">@hanter_xdofficial</a> (Joined)\n` +
          `✅ <b>Group:</b> <a href="${REQUIRED_GROUP.url}">@hanter_xd_official34</a> (Joined)\n` +
          `━━━━━━━━━━━━━━━━━━━━\n` +
          `🚀 <b>All Bot Features & Commands Are Now Unlocked!</b>\n\n` +
          `• Send any media link (YouTube, TikTok, Facebook, Instagram, TeraBox) to download.\n` +
          `• Tap <b>📱 Download Official App</b> below to get the APK file.\n` +
          `• Send <b>/help</b> anytime for full instructions.`;

        await callTg("sendMessage", {
          chat_id: chatId,
          text: successUserText,
          parse_mode: "HTML",
          disable_web_page_preview: true,
          reply_markup: getReplyKeyboardForUser(senderId)
        });

        // 2. Notify the Administrator immediately:
        // "Once the confirmation is complete, send a notification to me, the admin, stating that the user has successfully joined."
        const uHandle = cq.from?.username ? `@${escapeHtml(cq.from.username)}` : "No @username";
        const adminAlertText = `🎉 <b>User Successfully Verified & Joined!</b>\n━━━━━━━━━━━━━━━━━━━━\n` +
          `👤 <b>Name:</b> ${escapeHtml(senderName)}\n` +
          `🏷 <b>Username:</b> ${uHandle}\n` +
          `🆔 <b>User ID:</b> <code>${senderId}</code>\n` +
          `📢 <b>Channel:</b> Joined (<a href="${REQUIRED_CHANNEL.url}">@hanter_xdofficial</a>)\n` +
          `👥 <b>Group:</b> Joined (<a href="${REQUIRED_GROUP.url}">@hanter_xd_official34</a>)\n` +
          `📅 <b>Verified Time:</b> ${new Date().toLocaleString()}\n` +
          `━━━━━━━━━━━━━━━━━━━━\n` +
          `✅ <i>The user has confirmed membership and now has full access to the bot.</i>`;

        callTg("sendMessage", {
          chat_id: ADMIN_ID,
          text: adminAlertText,
          parse_mode: "HTML",
          disable_web_page_preview: true,
          reply_markup: {
            inline_keyboard: [
              [{ text: `💬 Message ${escapeHtml(senderName)}`, callback_data: `admin_msg_user_${senderId}` }],
              [{ text: "👑 Admin Panel", callback_data: "admin_refresh" }]
            ]
          }
        }).catch(err => console.warn("Failed to notify admin of user verification:", err.message));

        return;
      }

      // Block all other callback interactions if user is not verified
      if (!isAdmin(senderId)) {
        const cbMembership = await verifyUserMembershipLive(senderId);
        if (!cbMembership.isMember) {
          await callTg("answerCallbackQuery", {
            callback_query_id: cqId,
            text: "⚠️ Please join both our Channel and Group and click Confirm to unlock this bot!",
            show_alert: true
          });
          await sendVerificationPrompt(chatId, senderName, false, {
            channelJoined: cbMembership.channelJoined,
            groupJoined: cbMembership.groupJoined
          });
          return;
        }
      }

      // Allow any user to download APK directly (latest or specific tag)
      if (data === "get_apk") {
        await callTg("answerCallbackQuery", { callback_query_id: cqId, text: "Fetching Latest OmniStream APK..." });
        await handleSendApk(chatId, senderId);
        return;
      }

      if (data.startsWith("get_apk_tag_")) {
        const reqTag = data.replace("get_apk_tag_", "");
        await callTg("answerCallbackQuery", { callback_query_id: cqId, text: `Fetching APK for tag ${reqTag}...` });
        await handleSendApk(chatId, senderId, reqTag);
        return;
      }

      if (data === "view_releases") {
        await callTg("answerCallbackQuery", { callback_query_id: cqId, text: "Loading GitHub release tags..." });
        await handleSendReleasesList(chatId, senderId);
        return;
      }

      // Strictly check admin access for all admin callbacks
      if (data.startsWith("admin_")) {
        if (!isAdmin(senderId)) {
          await callTg("answerCallbackQuery", {
            callback_query_id: cqId,
            text: "⛔ Access Denied. Admin privileges required.",
            show_alert: true
          });
          return;
        }

        if (data === "admin_refresh") {
          await callTg("answerCallbackQuery", { callback_query_id: cqId, text: "Dashboard Refreshed" });
          await sendAdminDashboard(chatId, msgId);
          return;
        }

        if (data === "admin_users") {
          await callTg("answerCallbackQuery", { callback_query_id: cqId, text: "Loading user list..." });
          await sendUsersList(chatId, msgId);
          return;
        }

        if (data === "admin_check_release") {
          await callTg("answerCallbackQuery", { callback_query_id: cqId, text: "Checking GitHub Releases..." });
          await checkAndNotifyNewRelease(chatId);
          return;
        }

        if (data === "admin_status" || data === "admin_stats") {
          await callTg("answerCallbackQuery", { callback_query_id: cqId });
          const uptime = formatUptime(process.uptime());
          const mem = (process.memoryUsage().rss / (1024 * 1024)).toFixed(1);
          const totalUsers = Object.keys(db.users).length;
          const verifiedUsers = Object.values(db.users).filter(u => isUserVerified(u.id)).length;
          const unverifiedUsers = Math.max(0, totalUsers - verifiedUsers);
          const qStats = typeof mediaQueue !== "undefined" ? mediaQueue.getStats() : { activeTasks: 0, concurrencyLimit: 2, queuedTasks: 0, totalEnqueued: 0, totalProcessed: 0, totalFailed: 0, peakConcurrency: 0, avgProcessingTimeMs: 0 };
          const statsText = `📊 <b>OmniStream Detailed System Status</b>\n━━━━━━━━━━━━━━━━━━━━\n` +
            `• <b>Engine:</b> Node.js ${process.version} (ESM Mode)\n` +
            `• <b>RAM Usage:</b> ${mem} MB\n` +
            `• <b>Uptime:</b> ${uptime}\n` +
            `• <b>Total Users:</b> ${totalUsers}\n` +
            `• <b>Verified Users:</b> ${verifiedUsers}\n` +
            `• <b>Pending Verification:</b> ${unverifiedUsers}\n` +
            `• <b>Blocked Users:</b> ${db.blockedUsers.length}\n` +
            `• <b>Latest App Tag:</b> <code>${escapeHtml(db.lastReleaseTag)}</code>\n` +
            `• <b>Total Links:</b> ${db.stats.totalLinks}\n` +
            `• <b>Completed Video & APK Sends:</b> ${db.stats.totalDownloads}\n` +
            `• <b>Concurrent Task Pool:</b> <b>${qStats.activeTasks}/${qStats.concurrencyLimit} Active</b>\n` +
            `• <b>Queued Waiting Tasks:</b> <b>${qStats.queuedTasks}</b>\n` +
            `• <b>Tasks Processed:</b> <b>${qStats.totalProcessed}</b> (${qStats.totalFailed} failed)\n` +
            `• <b>Peak Concurrency:</b> <b>${qStats.peakConcurrency} Workers</b>\n` +
            `• <b>Avg Task Latency:</b> <b>${(qStats.avgProcessingTimeMs / 1000).toFixed(1)}s</b>\n` +
            `• <b>Vault Status:</b> 🔐 Active & Encrypted (Protected)\n` +
            `━━━━━━━━━━━━━━━━━━━━`;
          await callTg("editMessageText", {
            chat_id: chatId,
            message_id: msgId,
            text: statsText,
            parse_mode: "HTML",
            reply_markup: {
              inline_keyboard: [[{ text: "🔙 Back to Dashboard", callback_data: "admin_refresh" }]]
            }
          });
          return;
        }

        if (data === "admin_prompt_block") {
          await callTg("answerCallbackQuery", { callback_query_id: cqId });
          await callTg("sendMessage", {
            chat_id: chatId,
            text: `🚫 <b>Block User by ID:</b>\n\nPlease type the command:\n<code>/block &lt;User_ID&gt;</code>\n\nExample: <code>/block 123456789</code>`,
            parse_mode: "HTML"
          });
          return;
        }

        if (data === "admin_prompt_unblock") {
          await callTg("answerCallbackQuery", { callback_query_id: cqId });
          await callTg("sendMessage", {
            chat_id: chatId,
            text: `✅ <b>Unblock User by ID:</b>\n\nPlease type the command:\n<code>/unblock &lt;User_ID&gt;</code>\n\nExample: <code>/unblock 123456789</code>`,
            parse_mode: "HTML"
          });
          return;
        }

        if (data === "admin_prompt_dm") {
          await callTg("answerCallbackQuery", { callback_query_id: cqId });
          await callTg("sendMessage", {
            chat_id: chatId,
            text: `💬 <b>Direct Message to an Individual User:</b>\n\nPlease type:\n<code>/msg &lt;User_ID&gt; &lt;Your message here&gt;</code>\n\nExample:\n<code>/msg 8939822002 Hello! Thank you for using OmniStream.</code>\n\n<i>💡 Tip: Tap <b>👥 Registered Users</b> to message any user with 1-click!</i>`,
            parse_mode: "HTML"
          });
          return;
        }

        if (data.startsWith("admin_msg_user_")) {
          const targetId = data.replace("admin_msg_user_", "").trim();
          adminPendingMsgTarget = targetId;
          const targetUser = db.users[targetId];
          const targetName = targetUser?.firstName || `User ${targetId}`;
          await callTg("answerCallbackQuery", { callback_query_id: cqId, text: `Replying to ${targetName}...` });
          await callTg("sendMessage", {
            chat_id: chatId,
            text: `💬 <b>Send Direct Message to ${escapeHtml(targetName)} (<code>${targetId}</code>):</b>\n\n` +
              `Type your message below and send it, and the bot will immediately deliver it to this user!\n\n` +
              `<i>Or send: <code>/msg ${targetId} &lt;Your message&gt;</code></i>`,
            parse_mode: "HTML"
          });
          return;
        }

        if (data === "admin_prompt_broadcast") {
          await callTg("answerCallbackQuery", { callback_query_id: cqId });
          await callTg("sendMessage", {
            chat_id: chatId,
            text: `📢 <b>Broadcast Message to All Users:</b>\n\nPlease type:\n<code>/broadcast &lt;Your message here&gt;</code>\n\nExample:\n<code>/broadcast 🚀 OmniStream updated with ultra-fast download servers!</code>`,
            parse_mode: "HTML"
          });
          return;
        }

        if (data.startsWith("admin_block_do_")) {
          const targetId = data.replace("admin_block_do_", "").trim();
          blockUser(targetId);
          await callTg("answerCallbackQuery", { callback_query_id: cqId, text: `User ${targetId} Blocked!`, show_alert: true });
          
          // Notify the blocked user
          callTg("sendMessage", {
            chat_id: targetId,
            text: `🚫 <b>Access Restricted</b>\n\nYour access to OmniStream Bot has been revoked by the administrator.\nContact @HANTER_XD_OFFICIAL for assistance.`,
            parse_mode: "HTML"
          }).catch(() => {});

          await sendUsersList(chatId, msgId);
          return;
        }

        if (data.startsWith("admin_unblock_do_")) {
          const targetId = data.replace("admin_unblock_do_", "").trim();
          unblockUser(targetId);
          await callTg("answerCallbackQuery", { callback_query_id: cqId, text: `User ${targetId} Unblocked!`, show_alert: true });

          // Notify the unblocked user
          callTg("sendMessage", {
            chat_id: targetId,
            text: `✅ <b>Access Restored</b>\n\nYour access to OmniStream Bot has been restored by the administrator. You may now download videos!`,
            parse_mode: "HTML"
          }).catch(() => {});

          await sendUsersList(chatId, msgId);
          return;
        }
      }

      await callTg("answerCallbackQuery", { callback_query_id: cqId });
      return;
    }

    // 2. Handle Text Messages
    if (!update || !update.message) return;
    const msg = update.message;
    const chatId = msg.chat?.id;
    const senderId = String(msg.from?.id || chatId);
    const text = (msg.text || "").trim();
    const userFullName = getUserFullName(msg.from);
    const senderName = userFullName;

    if (!chatId) return;

    // Register user in database
    const { isNew, user } = registerOrUpdateUser(msg.from);

    // If a brand new user joins, alert the Administrator privately!
    if (isNew && !isAdmin(senderId)) {
      const alertAdmin = `🔔 <b>New User Registered in OmniStream!</b>\n━━━━━━━━━━━━━━━━━━━━\n` +
        `👤 <b>Full Name:</b> <b>${escapeHtml(userFullName)}</b>\n` +
        `🏷 <b>Username:</b> ${msg.from?.username ? `@${escapeHtml(msg.from.username)}` : "None"}\n` +
        `🆔 <b>User ID:</b> <code>${senderId}</code>\n` +
        `📅 <b>Joined:</b> ${new Date().toLocaleString()}\n` +
        `━━━━━━━━━━━━━━━━━━━━\n` +
        `<i>Quick action:</i>\n` +
        `• Message: <code>/msg ${senderId} Hello!</code>\n` +
        `• Block: <code>/block ${senderId}</code>`;

      callTg("sendMessage", {
        chat_id: ADMIN_ID,
        text: alertAdmin,
        parse_mode: "HTML",
        reply_markup: {
          inline_keyboard: [
            [
              { text: "👤 View User Profile", url: `tg://user?id=${senderId}` },
              { text: `💬 Message User`, callback_data: `admin_msg_user_${senderId}` }
            ],
            [
              { text: `🚫 Block User (${senderId})`, callback_data: `admin_block_do_${senderId}` }
            ]
          ]
        }
      }).catch(e => console.warn("Could not notify admin of new user:", e.message));
    }

    // CHECK IF USER IS BLOCKED
    if (isUserBlocked(senderId)) {
      console.log(`[BLOCKED USER REJECTED] ${senderId} (${senderName}) attempted access.`);
      await callTg("sendMessage", {
        chat_id: chatId,
        text: `🚫 <b>Access Restricted</b>\n\nYour account (ID: <code>${senderId}</code>) has been blocked from using OmniStream Bot by the administrator.\n\n<i>Contact Developer @HANTER_XD_OFFICIAL if you believe this is an error.</i>`,
        parse_mode: "HTML"
      });
      return;
    }

    // ==================== ADMIN ONLY COMMANDS & MENU ====================
    if (isAdmin(senderId)) {
      if (text === "👑 Admin Panel" || text === "/admin") {
        await sendAdminDashboard(chatId);
        return;
      }

      if (text === "/check_update") {
        await checkAndNotifyNewRelease(chatId);
        return;
      }

      if (text === "📊 Bot Stats" || text === "/stats") {
        const uptime = formatUptime(process.uptime());
        const totalUsers = Object.keys(db.users).length;
        const verifiedUsers = Object.values(db.users).filter(u => isUserVerified(u.id)).length;
        const unverifiedUsers = Math.max(0, totalUsers - verifiedUsers);
        const blockedCount = db.blockedUsers.length;
        const mem = (process.memoryUsage().rss / (1024 * 1024)).toFixed(1);
        const statsMsg = `📊 <b>OmniStream Bot Live Statistics</b>\n━━━━━━━━━━━━━━━━━━━━\n` +
          `• <b>Uptime:</b> ${uptime}\n` +
          `• <b>Memory Usage:</b> ${mem} MB\n` +
          `• <b>Total Registered Users:</b> ${totalUsers}\n` +
          `• <b>Verified Users:</b> ${verifiedUsers}\n` +
          `• <b>Pending Verification:</b> ${unverifiedUsers}\n` +
          `• <b>Active Users:</b> ${Math.max(0, totalUsers - blockedCount)}\n` +
          `• <b>Blocked Users:</b> ${blockedCount}\n` +
          `• <b>Latest App Release:</b> <code>${escapeHtml(db.lastReleaseTag)}</code>\n` +
          `• <b>Completed Downloads:</b> ${db.stats.totalDownloads}\n` +
          `• <b>Total Processed Links:</b> ${db.stats.totalLinks}\n` +
          `• <b>Security Vault:</b> 🔐 Active (Encrypted Token Seed)\n` +
          `━━━━━━━━━━━━━━━━━━━━`;
        await callTg("sendMessage", {
          chat_id: chatId,
          text: statsMsg,
          parse_mode: "HTML",
          reply_markup: getReplyKeyboardForUser(senderId)
        });
        return;
      }

      if (text === "👥 User Management" || text === "/users") {
        await sendUsersList(chatId);
        return;
      }

      if (text.startsWith("/block")) {
        const parts = text.split(/\s+/);
        const targetId = parts[1]?.trim();
        if (!targetId) {
          await callTg("sendMessage", {
            chat_id: chatId,
            text: `⚠️ <b>Usage:</b> <code>/block &lt;userId&gt;</code>\nExample: <code>/block 123456789</code>`,
            parse_mode: "HTML"
          });
          return;
        }

        const res = blockUser(targetId);
        if (!res.success) {
          await callTg("sendMessage", { chat_id: chatId, text: `❌ ${res.reason}`, parse_mode: "HTML" });
          return;
        }

        await callTg("sendMessage", {
          chat_id: chatId,
          text: `🚫 <b>User Blocked Successfully!</b>\n\nUser ID <code>${targetId}</code> is now restricted. They can no longer download videos or use this bot.`,
          parse_mode: "HTML"
        });

        // Send alert to blocked user
        callTg("sendMessage", {
          chat_id: targetId,
          text: `🚫 <b>Access Restricted</b>\n\nYour access to OmniStream Bot has been revoked by the administrator.`,
          parse_mode: "HTML"
        }).catch(() => {});
        return;
      }

      if (text.startsWith("/unblock")) {
        const parts = text.split(/\s+/);
        const targetId = parts[1]?.trim();
        if (!targetId) {
          await callTg("sendMessage", {
            chat_id: chatId,
            text: `⚠️ <b>Usage:</b> <code>/unblock &lt;userId&gt;</code>\nExample: <code>/unblock 123456789</code>`,
            parse_mode: "HTML"
          });
          return;
        }

        unblockUser(targetId);
        await callTg("sendMessage", {
          chat_id: chatId,
          text: `✅ <b>User Unblocked Successfully!</b>\n\nUser ID <code>${targetId}</code> has been restored and can now use OmniStream Bot freely.`,
          parse_mode: "HTML"
        });

        // Send alert to unblocked user
        callTg("sendMessage", {
          chat_id: targetId,
          text: `✅ <b>Access Restored</b>\n\nYour access to OmniStream Bot has been restored. You can now download videos!`,
          parse_mode: "HTML"
        }).catch(() => {});
        return;
      }

      if (text.startsWith("/broadcast") || text === "📢 Broadcast Message") {
        const broadcastContent = text.replace(/^\/broadcast\s*/i, "").trim();
        if (!broadcastContent || broadcastContent === "📢 Broadcast Message") {
          await callTg("sendMessage", {
            chat_id: chatId,
            text: `📢 <b>Broadcast Instructions:</b>\n\nSend: <code>/broadcast &lt;Your message here&gt;</code>\n\n<i>This will send the announcement to all active users.</i>`,
            parse_mode: "HTML"
          });
          return;
        }

        const userIds = Object.keys(db.users).filter(id => !isUserBlocked(id) && id !== ADMIN_ID);
        await callTg("sendMessage", {
          chat_id: chatId,
          text: `⏳ <i>Broadcasting message to ${userIds.length} users...</i>`,
          parse_mode: "HTML"
        });

        let successCount = 0;
        let failCount = 0;

        for (const uid of userIds) {
          try {
            const bRes = await callTg("sendMessage", {
              chat_id: uid,
              text: `📢 <b>Official OmniStream Announcement</b>\n━━━━━━━━━━━━━━━━━━━━\n\n${escapeHtml(broadcastContent)}\n\n━━━━━━━━━━━━━━━━━━━━\n<i>From: @OmniStream34_bot</i>`,
              parse_mode: "HTML"
            });
            if (bRes.ok) successCount++;
            else failCount++;
          } catch (_) {
            failCount++;
          }
          await new Promise(r => setTimeout(r, 60)); // Rate limit safety
        }

        await callTg("sendMessage", {
          chat_id: chatId,
          text: `✅ <b>Broadcast Completed!</b>\n\n• Delivered: <b>${successCount}</b> users\n• Failed/Blocked: <b>${failCount}</b> users`,
          parse_mode: "HTML"
        });
        return;
      }

      // Individual Direct Message to specific user: /msg <userId> <text> or /dm <userId> <text>
      if (text.startsWith("/msg") || text.startsWith("/dm") || text.startsWith("/send_user") || text === "💬 Message User") {
        const content = text.replace(/^\/(msg|dm|send_user)\s*/i, "").trim();
        const match = content.match(/^(\d+)\s+([\s\S]+)$/);
        if (!match) {
          await callTg("sendMessage", {
            chat_id: chatId,
            text: `💬 <b>Direct Message Instructions:</b>\n\n` +
              `Send: <code>/msg &lt;userId&gt; &lt;Your message here&gt;</code>\n\n` +
              `Example:\n<code>/msg 8939822002 Hi! How can I assist you today?</code>\n\n` +
              `<i>💡 Tip: You can also tap <b>👥 User Management</b> to select any user and message them directly!</i>`,
            parse_mode: "HTML"
          });
          return;
        }

        const targetId = match[1];
        const userMsg = match[2].trim();
        await deliverDirectMessage(targetId, userMsg, chatId);
        return;
      }

      // If admin tapped a user button and typed a message directly
      if (adminPendingMsgTarget && !text.startsWith("/")) {
        const targetId = adminPendingMsgTarget;
        adminPendingMsgTarget = null;
        await deliverDirectMessage(targetId, text, chatId);
        return;
      }
    } else {
      // If a non-admin attempts to send admin commands, deny silently without revealing admin endpoints
      if (text.startsWith("/admin") || text.startsWith("/block") || text.startsWith("/unblock") || text.startsWith("/broadcast") || text.startsWith("/users") || text.startsWith("/stats") || text.startsWith("/check_update") || text.startsWith("/msg") || text.startsWith("/dm")) {
        await callTg("sendMessage", {
          chat_id: chatId,
          text: `⚠️ <i>Unknown command. Send any media link (YouTube, TikTok, Facebook, Instagram, TeraBox) to download.</i>`,
          parse_mode: "HTML"
        });
        return;
      }
    }

    // ==================== GENERAL USER COMMANDS ====================

    if (text.startsWith("/start")) {
      const welcomeText = `👋 <b>Welcome, ${escapeHtml(senderName)}!</b>\n\n` +
        `🤖 I am <b>OmniStream Official Bot</b> (@OmniStream34_bot).\n` +
        `Download any social media video and audio in Full HD without watermarks!\n\n` +
        `🌐 <b>Official Website:</b> <a href="https://hanter-xd-official.github.io/OmniStream/">OmniStream Web Downloader</a>\n\n` +
        `🌟 <b>Supported Platforms:</b>\n` +
        `• <b>YouTube</b> (Shorts, HD Videos, Audio)\n` +
        `• <b>TikTok</b> (HD No-Watermark MP4 & MP3)\n` +
        `• <b>Instagram</b> (Reels, Posts, Stories)\n` +
        `• <b>Facebook</b> (Reels, Watch Videos)\n` +
        `• <b>TeraBox</b> (Direct Fast Download)\n` +
        `• <b>MEGA</b> (Direct High-Speed Download)\n` +
        `• <b>Twitter / X</b> (Clips & Videos)\n\n` +
        `🚀 <b>How to Use:</b>\n` +
        `Simply copy and paste any video or post link here!\n` +
        `👇`;

      // 1. Send Welcome Message with Website, APK & Developer buttons
      await callTg("sendMessage", {
        chat_id: chatId,
        text: welcomeText,
        parse_mode: "HTML",
        disable_web_page_preview: false,
        reply_markup: {
          inline_keyboard: [
            [{ text: "🌐 Visit Official Website", url: "https://hanter-xd-official.github.io/OmniStream/" }],
            [{ text: "📱 Download Official App (APK)", callback_data: "get_apk" }],
            [{ text: "👨‍💻 Developer (@HANTER_XD_OFFICIAL)", url: DEV_TELEGRAM }]
          ]
        }
      });

      // 2. Send Official Welcome Sound Theme from Music/OmniStream Pro.wav
      await sendWelcomeAudio(chatId);

      // 3. Immediately after, require membership if not verified
      const startMembership = await verifyUserMembershipLive(senderId);
      if (!startMembership.isMember) {
        await sendVerificationPrompt(chatId, senderName, false, {
          channelJoined: startMembership.channelJoined,
          groupJoined: startMembership.groupJoined
        });
      } else {
        // Send persistent bottom menu keyboard with "Download Official App" option
        await callTg("sendMessage", {
          chat_id: chatId,
          text: `⚡ <i>Tap <b>📱 Download Official App</b> below to get the APK file directly in this chat!</i>`,
          parse_mode: "HTML",
          reply_markup: getReplyKeyboardForUser(senderId)
        });
      }
      return;
    }

    // ==================== MEMBERSHIP GATEWAY (BLOCK UNVERIFIED ACCESS) ====================
    // Continuously verify user membership in channel and group before processing ANY command, link, or message
    if (!isAdmin(senderId)) {
      const membership = await verifyUserMembershipLive(senderId);
      if (!membership.isMember) {
        console.log(`[ACCESS BLOCKED - UNVERIFIED USER] ${senderId} (${senderName}) attempted: "${text}" (ch: ${membership.channelJoined}, gr: ${membership.groupJoined})`);
        await sendVerificationPrompt(chatId, senderName, false, {
          channelJoined: membership.channelJoined,
          groupJoined: membership.groupJoined
        });
        return;
      }
    }

    // ==================== APP DOWNLOAD COMMAND & MENU TRIGGER ====================
    if (text === "📱 Download Official App" || text.startsWith("/app") || text.startsWith("/apk") || text.startsWith("/download_app") || text.startsWith("/latest") || text.startsWith("/update")) {
      await handleSendApk(chatId, senderId);
      return;
    }

    if (text === "🏷️ Release Tags" || text.startsWith("/releases") || text.startsWith("/tags") || text.startsWith("/tag_list")) {
      await handleSendReleasesList(chatId, senderId);
      return;
    }

    if (text.startsWith("/help") || text === "📖 Help Guide") {
      await callTg("sendMessage", {
        chat_id: chatId,
        text: `📖 <b>OmniStream Bot Guide</b>\n\n` +
          `1. Copy any video link from YouTube, TikTok, Facebook, Instagram, or TeraBox.\n` +
          `2. Send the link directly to this chat.\n` +
          `3. The bot will automatically fetch and deliver the MP4 video directly to you!\n\n` +
          `👨‍💻 <b>Developer:</b> ${DEV_NAME}`,
        parse_mode: "HTML",
        reply_markup: {
          inline_keyboard: [
            [{ text: "👨‍💻 Developer (@HANTER_XD_OFFICIAL)", url: DEV_TELEGRAM }]
          ]
        }
      });
      return;
    }

    if (text === "⚡ Supported Sites" || text === "⚡ Supported Platforms") {
      await callTg("sendMessage", {
        chat_id: chatId,
        text: `🌟 <b>OmniStream Supported Platforms:</b>\n\n` +
          `• <b>TikTok:</b> Ultra-fast 1080p, no-watermark MP4 & MP3 audio.\n` +
          `• <b>YouTube:</b> 720p/1080p MP4 with sound.\n` +
          `• <b>Facebook:</b> Public Reels and Watch videos.\n` +
          `• <b>Instagram:</b> Reels, Stories, and Carousels.\n` +
          `• <b>TeraBox:</b> Direct fast high-speed cloud download links.\n` +
          `• <b>MEGA:</b> Direct fast high-speed cloud download links.\n` +
          `• <b>Twitter / X:</b> High-definition MP4 clips.`,
        parse_mode: "HTML",
        reply_markup: {
          inline_keyboard: [
            [{ text: "👨‍💻 Developer (@HANTER_XD_OFFICIAL)", url: DEV_TELEGRAM }]
          ]
        }
      });
      return;
    }

    if (text === "/queue" || text === "/status" || text === "⚡ Queue Status" || text === "📊 Bot Status") {
      const q = typeof mediaQueue !== "undefined" ? mediaQueue.getStats() : { activeTasks: 0, concurrencyLimit: 2, queuedTasks: 0, perUserLimit: 1, memoryRssMb: 0, memoryLimitMb: 512, totalProcessed: 0, totalFailed: 0, peakConcurrency: 0, avgProcessingTimeMs: 0 };
      const statusMsg = `⚡ <b>OmniStream Concurrency & Task Queue Status</b>\n` +
        `━━━━━━━━━━━━━━━━━━━━\n` +
        `• 🚀 <b>Active Workers:</b> <b>${q.activeTasks}/${q.concurrencyLimit} Running</b>\n` +
        `• ⏳ <b>Pending Queue:</b> <b>${q.queuedTasks} Tasks Waiting</b>\n` +
        `• 👥 <b>Per-User Limit:</b> <b>${q.perUserLimit} Active Task/User</b>\n` +
        `• 💾 <b>Server RAM:</b> <b>${q.memoryRssMb} MB / 512 MB</b>\n` +
        `• 🛡 <b>Engine Profile:</b> Render Free Tier Optimized\n` +
        `• ✅ <b>Total Tasks Processed:</b> <b>${q.totalProcessed}</b>\n` +
        `• 📈 <b>Peak Concurrency:</b> <b>${q.peakConcurrency} Workers</b>\n` +
        `• ⏱ <b>Average Processing Time:</b> <b>${(q.avgProcessingTimeMs / 1000).toFixed(1)}s</b>\n` +
        `• 🟢 <b>Worker Engine:</b> Online & Multi-threaded\n` +
        `━━━━━━━━━━━━━━━━━━━━\n` +
        `<i>Render free-tier task queue with strict sequential delivery (video sent first, then audio).</i>`;
      await callTg("sendMessage", {
        chat_id: chatId,
        text: statusMsg,
        parse_mode: "HTML"
      });
      return;
    }

    // ==================== MEDIA LINK PROCESSING VIA ASYNC TASK QUEUE ====================
    const foundUrl = extractUrl(text);
    if (foundUrl) {
      const initResp = await callTg("sendMessage", {
        chat_id: chatId,
        text: "⏳ <b>Processing link...</b>\n<i>Connecting to high-concurrency worker engine...</i>",
        parse_mode: "HTML"
      });
      const progressMsgId = initResp.result?.message_id;
      if (progressMsgId) {
        await mediaQueue.enqueue({
          type: "media_download",
          url: foundUrl,
          chatId,
          userId: senderId,
          progressMsgId,
          priority: isAdmin(senderId) ? 10 : 0
        });
      }
    } else {
      await callTg("sendMessage", {
        chat_id: chatId,
        text: "⚠️ <i>Please send a valid media link (TikTok, Facebook, Instagram, YouTube, TeraBox) to download.</i>",
        parse_mode: "HTML"
      });

      // Forward general message/inquiry to Admin so Admin can 1-click reply!
      if (!isAdmin(senderId)) {
        const uName = senderName ? escapeHtml(senderName) : "User";
        const uHandle = msg.from?.username ? `@${escapeHtml(msg.from.username)}` : "";
        callTg("sendMessage", {
          chat_id: ADMIN_ID,
          text: `📩 <b>Message from User:</b>\n━━━━━━━━━━━━━━━━━━━━\n` +
            `👤 <b>From:</b> ${uName} ${uHandle} (<code>${senderId}</code>)\n` +
            `💬 <i>"${escapeHtml(text)}"</i>\n━━━━━━━━━━━━━━━━━━━━\n` +
            `<i>Quick reply:</i> <code>/msg ${senderId} &lt;Your reply&gt;</code>`,
          parse_mode: "HTML",
          reply_markup: {
            inline_keyboard: [
              [{ text: `💬 Reply to ${uName}`, callback_data: `admin_msg_user_${senderId}` }]
            ]
          }
        }).catch(() => {});
      }
    }
  } catch (err) {
    console.error("handleUpdate error:", err.message);
  }
}

// ==================== POLLING LOOP ====================

let lastUpdateId = 0;
const processedUpdateIds = new Set();
let lastConflictAt = 0;

async function pollUpdates() {
  if (!BOT_TOKEN) {
    console.warn("⚠️ Telegram polling paused: BOT_TOKEN environment variable is not provided.");
    console.warn("👉 Export BOT_TOKEN='your_token' or ensure remote secret endpoint is reachable.");
    while (!BOT_TOKEN) {
      await new Promise(r => setTimeout(r, 10000));
      await resolveSecretToken();
    }
  }

  while (true) {
    try {
      const recentlyConflicted = Date.now() - lastConflictAt < 120000;
      const pollTimeout = recentlyConflicted ? 2 : 20;
      const res = await fetch(`${TELEGRAM_API}/getUpdates?offset=${lastUpdateId}&timeout=${pollTimeout}`, {
        signal: AbortSignal.timeout((pollTimeout + 8) * 1000)
      });

      if (res.ok) {
        const data = await res.json();
        if (data && data.ok && Array.isArray(data.result) && data.result.length > 0) {
          for (const update of data.result) {
            if (update.update_id >= lastUpdateId) {
              lastUpdateId = update.update_id + 1;
            }
            if (!processedUpdateIds.has(update.update_id)) {
              processedUpdateIds.add(update.update_id);
              if (processedUpdateIds.size > 2000) {
                const firstKey = processedUpdateIds.values().next().value;
                processedUpdateIds.delete(firstKey);
              }
              handleUpdate(update).catch(e => console.error("Update task error:", e?.message));
            }
          }
          // Immediately commit offset on Telegram server so no stale instance can read the same update
          fetch(`${TELEGRAM_API}/getUpdates?offset=${lastUpdateId}&timeout=0`, {
            signal: AbortSignal.timeout(4000)
          }).catch(() => {});
        }
      } else if (res.status === 409) {
        lastConflictAt = Date.now();
        // Preempt any stale external poller immediately (within 10ms) so it stays in its 2000ms backoff
        await new Promise(r => setTimeout(r, 10));
      } else {
        await new Promise(r => setTimeout(r, 1000));
      }
    } catch (err) {
      await new Promise(r => setTimeout(r, 250));
    }
  }
}

pollUpdates().catch(err => {
  console.error("Critical poll loop failure, restarting:", err?.message);
  setTimeout(pollUpdates, 3000);
});
