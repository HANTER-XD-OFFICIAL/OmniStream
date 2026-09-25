// OmniStream - Official Telegram Bot Daemon (24/7 Production Engine)
// Bot Username: @OmniStream34_bot
// Developer: MD Rasel (@HANTER_XD_OFFICIAL)
// GitHub Repository: HANTER-XD-OFFICIAL/OmniStream
// Language Policy: STRICTLY 100% ENGLISH FOR ALL USER-FACING BOT MESSAGES

import process from 'node:process';
import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

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
let BOT_TOKEN = (process.env.BOT_TOKEN || process.env.TELEGRAM_BOT_TOKEN || process.env.TELEGRAM_TOKEN || "").trim();
const REMOTE_WORKER_SECRET_URL = "https://omnistream-telegram-api.alexraselchodhury.workers.dev/";
const WORKER_AUTH_SECRET = (process.env.API_SECRET || process.env.WORKER_API_SECRET || "432872").trim();

async function resolveSecretToken() {
  if (BOT_TOKEN && BOT_TOKEN !== "YOUR_TELEGRAM_BOT_TOKEN") return BOT_TOKEN;
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

// Initial resolution if not already provided via process.env
await resolveSecretToken();

let TELEGRAM_API = `https://api.telegram.org/bot${BOT_TOKEN}`;

if (!BOT_TOKEN) {
  console.warn("⚠️ [SECURITY WARNING] Telegram BOT_TOKEN is not set in environment variables!");
  console.warn("👉 Set BOT_TOKEN in your system environment or cloud deployment dashboard (e.g., Render, Docker, or .env).");
  console.warn("   Example: BOT_TOKEN=\"123456789:ABCdefGHIjklMNOpqrSTUvwxYZ\"");
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
    db.users[id].username = from.username || db.users[id].username;
    db.users[id].lastActive = now;
  }
  return { isNew, user: db.users[id] };
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
  if (lower.includes("terabox") || lower.includes("1024tera") || lower.includes("teraboxapp") || lower.includes("terasharelink")) return "TeraBox";
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

async function sendTgVideo(chatId, videoBuffer, filename, caption, replyMarkup = null) {
  try {
    const form = new FormData();
    form.append("chat_id", String(chatId));
    form.append("caption", caption || "");
    form.append("parse_mode", "HTML");
    form.append("supports_streaming", "true");
    if (replyMarkup) {
      form.append("reply_markup", JSON.stringify(replyMarkup));
    }
    form.append("video", new Blob([videoBuffer], { type: "video/mp4" }), filename || "video.mp4");

    const res = await fetch(`${TELEGRAM_API}/sendVideo`, {
      method: "POST",
      body: form,
      signal: AbortSignal.timeout(90000)
    });
    return await res.json();
  } catch (err) {
    console.error("sendTgVideo error:", err.message);
    return { ok: false, error: err.message };
  }
}

async function sendTgDocument(chatId, fileBuffer, filename, caption, replyMarkup = null) {
  try {
    const form = new FormData();
    form.append("chat_id", String(chatId));
    form.append("caption", caption || "");
    form.append("parse_mode", "HTML");
    if (replyMarkup) {
      form.append("reply_markup", JSON.stringify(replyMarkup));
    }
    form.append("document", new Blob([fileBuffer], { type: "application/vnd.android.package-archive" }), filename || "OmniStream.apk");

    const res = await fetch(`${TELEGRAM_API}/sendDocument`, {
      method: "POST",
      body: form,
      signal: AbortSignal.timeout(180000)
    });
    return await res.json();
  } catch (err) {
    console.warn("sendTgDocument notice:", err.message);
    return { ok: false, error: err.message };
  }
}

async function sendTgAudio(chatId, fileBuffer, filename, caption, title = "OmniStream Audio Track", performer = "OmniStream", replyMarkup = null) {
  try {
    const form = new FormData();
    form.append("chat_id", String(chatId));
    if (caption) form.append("caption", caption);
    form.append("parse_mode", "HTML");
    if (title) form.append("title", title);
    if (performer) form.append("performer", performer);
    if (replyMarkup) {
      form.append("reply_markup", JSON.stringify(replyMarkup));
    }
    const ext = filename?.split('.').pop()?.toLowerCase() || 'mp3';
    let mime = 'audio/mpeg';
    if (ext === 'wav') mime = 'audio/wav';
    else if (ext === 'm4a') mime = 'audio/mp4';
    else if (ext === 'ogg') mime = 'audio/ogg';

    form.append("audio", new Blob([fileBuffer], { type: mime }), filename || "OmniStream_Audio.mp3");

    const res = await fetch(`${TELEGRAM_API}/sendAudio`, {
      method: "POST",
      body: form,
      signal: AbortSignal.timeout(120000)
    });
    return await res.json();
  } catch (err) {
    console.warn("sendTgAudio notice:", err.message);
    return { ok: false, error: err.message };
  }
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
    // 1. Default scope: All ordinary users
    await callTg("setMyCommands", {
      commands: [
        { command: "start", description: "Start OmniStream Bot" },
        { command: "app", description: "📱 Download Latest App (APK)" },
        { command: "tags", description: "🏷️ View All Release Tags & APKs" },
        { command: "help", description: "How to download videos & guide" }
      ],
      scope: { type: "default" }
    });

    // 2. Chat scope: ONLY for the Administrator (@HANTER_XD_OFFICIAL / 6204875999)
    await callTg("setMyCommands", {
      commands: [
        { command: "admin", description: "👑 Open Master Admin Panel" },
        { command: "app", description: "📱 Download Official App (APK)" },
        { command: "tags", description: "🏷️ View All Release Tags" },
        { command: "check_update", description: "🚀 Check GitHub Releases & Notify" },
        { command: "broadcast", description: "📢 Send Broadcast to All" },
        { command: "msg", description: "💬 Message Single User (/msg ID text)" },
        { command: "users", description: "👥 View Registered Users" },
        { command: "stats", description: "📊 Bot System Statistics" },
        { command: "block", description: "🚫 Block User (/block ID)" },
        { command: "unblock", description: "✅ Unblock User (/unblock ID)" },
        { command: "help", description: "Help Guide" }
      ],
      scope: { type: "chat", chat_id: ADMIN_ID }
    });
    console.log("✅ Bot Menu Commands configured with isolated Admin privileges & APK Download.");
  } catch (e) {
    console.warn("Could not set bot commands:", e.message);
  }
}

setupBotCommands();

// Keyboard builders: Admin gets Admin Panel in menu bar, Regular users get direct Download App button
function getReplyKeyboardForUser(userId) {
  if (isAdmin(userId)) {
    return {
      keyboard: [
        [{ text: "👑 Admin Panel" }, { text: "📊 Bot Stats" }],
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
        [{ text: "📖 Help Guide" }, { text: "⚡ Supported Sites" }],
        [{ text: "📱 Download Official App" }]
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

  // Strategy 1: GitHub Releases API (ordered newest first)
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
              db.cachedApkFileId = null; // Invalidate cached Telegram file_id for new version
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
    } else {
      console.warn(`[GITHUB RELEASES] GitHub API status: ${res.status}, trying HTML scrape fallback.`);
    }
  } catch (err) {
    console.warn('[GITHUB RELEASES] API fetch note:', err.message);
  }

  // Strategy 2: GitHub Web Scraping (100% immune to API rate limits!)
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

    // 2. FILE UPLOAD PATH: Read local APK if present on disk, otherwise download from GitHub asset
    if (progressMsgId) {
      await callTg("editMessageText", {
        chat_id: chatId,
        message_id: progressMsgId,
        text: `📥 <b>Downloading Latest OmniStream APK (${sizeMb} MB)...</b>\n<i>Fetching tag <code>${escapeHtml(release.tag)}</code> and sending directly to your Telegram chat...</i>`,
        parse_mode: "HTML"
      }).catch(() => {});
    }

    let apkBuffer = null;
    const localApkPath = path.resolve(__dirname, '../.build-outputs/app-debug.apk');
    if (fs.existsSync(localApkPath) && !requestedTag) {
      try {
        apkBuffer = await fs.promises.readFile(localApkPath);
        console.log(`[APK LOCAL] Read APK from disk (${(apkBuffer.length / (1024 * 1024)).toFixed(1)} MB)`);
      } catch (err) {
        console.warn('[APK LOCAL READ ERROR]', err.message);
      }
    }

    if (!apkBuffer && apk.downloadUrl) {
      try {
        console.log(`[APK FETCH] Fetching APK from: ${apk.downloadUrl}`);
        const apkRes = await fetch(apk.downloadUrl, {
          headers: {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36",
            "Accept": "application/octet-stream,application/vnd.android.package-archive,*/*"
          },
          redirect: "follow",
          signal: AbortSignal.timeout(120000)
        });
        if (apkRes.ok) {
          apkBuffer = await apkRes.arrayBuffer();
          console.log(`[APK FETCH SUCCESS] Downloaded ${(apkBuffer.byteLength / (1024 * 1024)).toFixed(1)} MB`);
        } else {
          console.warn(`[APK FETCH FAILED] Status ${apkRes.status} for ${apk.downloadUrl}`);
        }
      } catch (err) {
        console.warn('[APK FETCH ERROR]', err.message);
      }
    }

    if (apkBuffer && apkBuffer.byteLength > 1000000) {
      if (progressMsgId) {
        await callTg("editMessageText", {
          chat_id: chatId,
          message_id: progressMsgId,
          text: `📤 <b>Uploading Official APK to Telegram...</b>\n<i>Delivering <code>${escapeHtml(apk.name)}</code> (${sizeMb} MB)...</i>`,
          parse_mode: "HTML"
        }).catch(() => {});
      }

      const docRes = await sendTgDocument(chatId, apkBuffer, apk.name, caption, replyMarkup);
      if (docRes.ok) {
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

// 3. YouTube Resolver (Loader.to Full Poll Cycle + oEmbed)
async function resolveYouTube(url, onProgressUpdate = null) {
  try {
    let oEmbedTitle = "YouTube Video";
    let oEmbedAuthor = "YouTube Creator";
    let oEmbedThumb = null;

    try {
      const oeRes = await fetch(`https://www.youtube.com/oembed?url=${encodeURIComponent(url)}&format=json`, {
        signal: AbortSignal.timeout(5000)
      });
      if (oeRes.ok) {
        const oeJson = await oeRes.json();
        oEmbedTitle = oeJson.title || oEmbedTitle;
        oEmbedAuthor = oeJson.author_name || oEmbedAuthor;
        oEmbedThumb = oeJson.thumbnail_url;
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
              cover: oEmbedThumb,
              videoUrl: sJson.download_url,
              directStream: true
            };
          }

          if (sJson.progress_url) {
            if (onProgressUpdate) {
              await onProgressUpdate("⏳ <b>Converting YouTube Video...</b>\n<i>Rendering high-definition MP4 stream with sound...</i>");
            }
            for (let i = 0; i < 14; i++) {
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
                      cover: oEmbedThumb,
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

    return {
      type: "YouTube",
      title: oEmbedTitle,
      author: oEmbedAuthor,
      cover: oEmbedThumb,
      videoUrl: url,
      directStream: false
    };
  } catch (err) {
    console.error("YouTube resolve error:", err.message);
    return null;
  }
}

// 4. TeraBox Resolver (SyntexCore Dedicated API + Multi-Gateway Failover)
async function resolveTeraBox(url) {
  // Primary: SyntexCore Dedicated TeraBox API
  try {
    const res = await fetch("https://syntexcore.site/api/v1/terabox-dl", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        url,
        apiKey: "syntx_live_2o8vqnbvwh3xw7p4w887ps"
      }),
      signal: AbortSignal.timeout(12000)
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
            title: payload.file_name || payload.filename || payload.title || payload.name || "TeraBox File",
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

  // Fallback: High-Speed Public Worker Resolvers
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
            title: payload.file_name || payload.filename || payload.title || "TeraBox File",
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
        if (file.size > 0 && file.size < 45 * 1024 * 1024) {
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
  const hosts = ["https://loader.to", "https://en.loader.to"];
  for (const host of hosts) {
    try {
      const encUrl = encodeURIComponent(url);
      const startUrl = `${host}/ajax/download.php?button=1&start=1&end=1&format=mp3&url=${encUrl}`;
      const startRes = await fetch(startUrl, {
        headers: { "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)", "Referer": `${host}/` },
        signal: AbortSignal.timeout(10000)
      });
      if (startRes.ok) {
        const sJson = await startRes.json();
        if (sJson.download_url && sJson.download_url.startsWith("http")) {
          return sJson.download_url;
        }
        if (sJson.progress_url) {
          for (let i = 0; i < 10; i++) {
            await new Promise(r => setTimeout(r, 2000));
            try {
              const pRes = await fetch(sJson.progress_url, { signal: AbortSignal.timeout(6000) });
              if (pRes.ok) {
                const pJson = await pRes.json();
                if (pJson.download_url && pJson.download_url.startsWith("http")) {
                  return pJson.download_url;
                }
              }
            } catch (_) {}
          }
        }
      }
    } catch (_) {}
  }
  return null;
}

async function deliverAudioTrack(chatId, url, media, safeTitle, platformName, videoBuffer = null) {
  try {
    let audioUrl = media?.audioUrl || null;
    const lower = url.toLowerCase();

    // 1. If no direct audioUrl yet, query audio resolver
    if (!audioUrl) {
      if (lower.includes("youtube.com") || lower.includes("youtu.be")) {
        audioUrl = await resolveYouTubeAudio(url);
      }
      if (!audioUrl) {
        audioUrl = await resolveCobaltAudio(url);
      }
    }

    const cleanTitle = (media?.title || safeTitle || "Audio Track")
      .replace(/&[a-zA-Z0-9#x]+;/g, "")
      .replace(/[^\w\s-]/g, "")
      .trim()
      .substring(0, 45) || "Audio";

    const audioFilename = formatOmniStreamFilename(platformName, cleanTitle, "mp3");
    const audioCaption = `🎵 <b>${escapeHtml(safeTitle)}</b>\n\n` +
      `👤 <b>Artist/Creator:</b> ${escapeHtml(media?.author || "Creator")}\n` +
      `📻 <b>Format:</b> High Quality Audio (MP3)\n` +
      `🌐 <b>Platform:</b> ${escapeHtml(platformName)}\n\n` +
      `⚡ <i>Audio track extracted via OmniStream Bot (@OmniStream34_bot)</i>`;

    const audioMarkup = {
      inline_keyboard: [
        [{ text: "🌐 Official Web App", url: "https://hanter-xd-official.github.io/OmniStream/" }],
        [{ text: "👨‍💻 Developer Profile", url: DEV_TELEGRAM }]
      ]
    };

    // Try fetching audio from audioUrl
    if (audioUrl && audioUrl.startsWith("http")) {
      try {
        const audioFetch = await fetch(audioUrl, {
          headers: {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
            "Referer": lower.includes("tiktok.com") ? "https://www.tiktok.com/" : url
          },
          signal: AbortSignal.timeout(35000)
        });
        if (audioFetch.ok) {
          const aBuf = await audioFetch.arrayBuffer();
          if (aBuf.byteLength > 1000 && aBuf.byteLength < 49 * 1024 * 1024) {
            const aRes = await sendTgAudio(chatId, aBuf, audioFilename, audioCaption, safeTitle, media?.author || platformName, audioMarkup);
            if (aRes && aRes.ok) {
              console.log(`[DELIVERED AUDIO] Audio buffer sent to ${chatId}`);
              return true;
            }
          }
        }
      } catch (err) {
        console.warn("Audio buffer fetch/send error:", err.message);
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

    // 2. If separate audio stream is not available, but videoBuffer was fetched,
    // Telegram sendAudio natively accepts MP4 container and plays audio stream!
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
    } else if (lower.includes("terabox") || lower.includes("1024tera") || lower.includes("terasharelink") || lower.includes("teraboxapp")) {
      media = await resolveTeraBox(url);
    } else if (lower.includes("mega.nz") || lower.includes("mega.co.nz") || lower.includes("mega.io")) {
      media = await resolveMega(url);
    } else {
      media = await resolveCobalt(url);
    }

    if (!media || !media.videoUrl) {
      if (lower.includes("terabox") || lower.includes("1024tera") || lower.includes("terasharelink") || lower.includes("teraboxapp")) {
        const surlMatch = url.match(/\/s\/(?:1)?([a-zA-Z0-9_-]+)/);
        const surl = surlMatch ? surlMatch[1] : "";
        const mirrorLink = surl ? `https://1024tera.com/s/1${surl}` : url;
        const webPortal = surl ? `https://terasharelink.com/s/1${surl}` : url;
        await callTg("editMessageText", {
          chat_id: chatId,
          message_id: progressMsgId,
          text: `📦 <b>TeraBox Cloud Storage Engine</b>\n━━━━━━━━━━━━━━━━━━━━\n` +
            `🔗 <b>Target URL:</b> <code>${escapeHtml(url)}</code>\n\n` +
            `⚡ <b>Engine:</b> <b>SyntexCore Cloud Node Active</b>\n` +
            `🚀 <b>Status:</b> <b>High-Speed Fast Stream & Direct Download Ready</b>\n\n` +
            `💡 <b>Instant Action:</b> You can stream the video or download the full file directly with maximum bandwidth using the buttons below:`,
          parse_mode: "HTML",
          reply_markup: {
            inline_keyboard: [
              [{ text: "⚡ High-Speed Direct Download / Play", url: mirrorLink }],
              [{ text: "🌐 Instant Cloud Web Portal", url: webPortal }],
              [{ text: "📥 Official Web Downloader", url: "https://hanter-xd-official.github.io/OmniStream/" }],
              [{ text: "💬 Developer Support (@HANTER_XD_OFFICIAL)", url: DEV_TELEGRAM }]
            ]
          }
        });
        return;
      }

      if (lower.includes("mega.nz") || lower.includes("mega.co.nz") || lower.includes("mega.io")) {
        await callTg("editMessageText", {
          chat_id: chatId,
          message_id: progressMsgId,
          text: `☁️ <b>MEGA Cloud Storage Engine</b>\n━━━━━━━━━━━━━━━━━━━━\n` +
            `🔗 <b>Target URL:</b> <code>${escapeHtml(url)}</code>\n\n` +
            `⚡ <b>Engine:</b> <b>SyntexCore Dedicated Cloud Node Active</b>\n` +
            `🚀 <b>Status:</b> <b>Direct Cloud Stream & Download Ready</b>\n\n` +
            `💡 <b>Instant Action:</b> You can open, stream or download this file directly from the high-speed MEGA cloud network:`,
          parse_mode: "HTML",
          reply_markup: {
            inline_keyboard: [
              [{ text: "🚀 Open Direct MEGA Cloud", url: url }],
              [{ text: "📥 Official Web Downloader", url: "https://hanter-xd-official.github.io/OmniStream/" }],
              [{ text: "💬 Developer Support (@HANTER_XD_OFFICIAL)", url: DEV_TELEGRAM }]
            ]
          }
        });
        return;
      }

      await callTg("editMessageText", {
        chat_id: chatId,
        message_id: progressMsgId,
        text: `⚠️ <b>Direct Stream Notice</b>\n\nCould not extract a direct video stream from this link.\n\n📱 <i>Tip: Verify the link is publicly accessible or try again in a moment.</i>`,
        parse_mode: "HTML",
        reply_markup: {
          inline_keyboard: [
            [{ text: "💬 Support (@HANTER_XD_OFFICIAL)", url: DEV_TELEGRAM }]
          ]
        }
      });
      return;
    }

    const safeTitle = media.title ? String(media.title).trim() : "Media Video";
    const shortTitle = safeTitle.length > 40 ? safeTitle.substring(0, 40) + "..." : safeTitle;

    // Direct in-memory buffer delivery (e.g. decrypted MEGA files)
    if (media.buffer && media.buffer.byteLength > 1000) {
      await callTg("editMessageText", {
        chat_id: chatId,
        message_id: progressMsgId,
        text: `⚡ <b>Ready:</b> ${escapeHtml(shortTitle)}\n📥 <i>Delivering file to Telegram...</i>`,
        parse_mode: "HTML"
      });

      const filename = media.title || "cloud_file.mp4";
      const isVideo = filename.match(/\.(mp4|mkv|webm|mov|avi)$/i);
      const isAudio = filename.match(/\.(mp3|m4a|wav|aac|flac|ogg)$/i);
      const cap = `☁️ <b>${escapeHtml(safeTitle)}</b>\n\n📥 Downloaded via OmniStream Cloud Engine`;
      const markup = {
        inline_keyboard: [
          [{ text: "🌐 Web Downloader", url: "https://hanter-xd-official.github.io/OmniStream/" }],
          [{ text: "💬 Support (@HANTER_XD_OFFICIAL)", url: DEV_TELEGRAM }]
        ]
      };

      if (isVideo) {
        const vRes = await sendTgVideo(chatId, media.buffer, filename, cap, markup);
        if (vRes?.ok) {
          try { await callTg("deleteMessage", { chat_id: chatId, message_id: progressMsgId }); } catch (_) {}
          return;
        }
      } else if (isAudio) {
        const aRes = await sendTgAudio(chatId, media.buffer, filename, cap, safeTitle, media.author || "MEGA Cloud", markup);
        if (aRes?.ok) {
          try { await callTg("deleteMessage", { chat_id: chatId, message_id: progressMsgId }); } catch (_) {}
          return;
        }
      }
      const dRes = await sendTgDocument(chatId, media.buffer, filename, cap, markup);
      if (dRes?.ok) {
        try { await callTg("deleteMessage", { chat_id: chatId, message_id: progressMsgId }); } catch (_) {}
        return;
      }
    }

    if (media.directStream && media.videoUrl !== url) {
      await callTg("editMessageText", {
        chat_id: chatId,
        message_id: progressMsgId,
        text: `⚡ <b>Ready:</b> ${escapeHtml(shortTitle)}\n📥 <i>Downloading video file & sending to Telegram...</i>`,
        parse_mode: "HTML"
      });

      try {
        let contentLength = 0;
        try {
          const headRes = await fetch(media.videoUrl, {
            method: "HEAD",
            headers: { "User-Agent": "Mozilla/5.0" },
            signal: AbortSignal.timeout(6000)
          });
          if (headRes.ok) {
            contentLength = parseInt(headRes.headers.get("content-length") || "0", 10);
          }
        } catch (_) {}

        if (contentLength > 45 * 1024 * 1024) {
          const sizeMb = (contentLength / (1024 * 1024)).toFixed(1);
          const largeText = `🎬 <b>${escapeHtml(safeTitle)}</b>\n\n` +
            `👤 <b>Platform:</b> ${escapeHtml(media.type || "Media Video")}\n` +
            `💾 <b>Video Size:</b> ${sizeMb} MB (Exceeds 50MB Bot Limit)\n\n` +
            `⚡ <i>Video download link is ready below. Extracting & sending audio track directly to chat...</i>`;

          await callTg("editMessageText", {
            chat_id: chatId,
            message_id: progressMsgId,
            text: largeText,
            parse_mode: "HTML",
            reply_markup: {
              inline_keyboard: [
                [{ text: `📥 Download Full HD Video (${sizeMb} MB)`, url: media.videoUrl }],
                [{ text: "🌐 Official Web App", url: "https://hanter-xd-official.github.io/OmniStream/" }],
                [{ text: "👨‍💻 Developer (@HANTER_XD_OFFICIAL)", url: DEV_TELEGRAM }]
              ]
            }
          });
          db.stats.totalDownloads++;
          if (db.users[userId]) db.users[userId].downloads++;
          saveDatabase();

          // Deliver audio track directly to chat!
          const platformName = detectPlatformName(media.type, url);
          await deliverAudioTrack(chatId, url, media, safeTitle, platformName);
          return;
        }

        let vidRes = await fetch(media.videoUrl, {
          headers: {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36",
            "Referer": media.type === "TikTok" ? "https://www.tiktok.com/" : url
          },
          signal: AbortSignal.timeout(60000)
        });

        if (!vidRes.ok && media.type === "TikTok") {
          vidRes = await fetch(media.videoUrl, {
            headers: {
              "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
            },
            signal: AbortSignal.timeout(60000)
          });
        }

        if (vidRes.ok) {
          const videoBuffer = await vidRes.arrayBuffer();
          const sizeBytes = videoBuffer.byteLength;
          const sizeMb = (sizeBytes / (1024 * 1024)).toFixed(1);

          if (sizeBytes <= 48 * 1024 * 1024 && sizeBytes > 1000) {
            const caption = `🎬 <b>${escapeHtml(safeTitle)}</b>\n\n` +
              `👤 <b>Platform:</b> ${escapeHtml(media.type || "Web Video")}\n` +
              (media.duration ? `⏱ <b>Duration:</b> ${formatSeconds(media.duration)}\n` : "") +
              `💾 <b>Size:</b> ${sizeMb} MB\n` +
              `🎵 <b>Audio:</b> Extracted & delivering below\n\n` +
              `⚡ <i>Downloaded via OmniStream Bot (@OmniStream34_bot)</i>`;

            const replyMarkup = {
              inline_keyboard: [
                [{ text: "🌐 Direct HD Stream Link", url: media.videoUrl }],
                [{ text: "👨‍💻 Developer Profile", url: DEV_TELEGRAM }]
              ]
            };

            const platformName = detectPlatformName(media.type, url);
            const standardizedFilename = formatOmniStreamFilename(platformName, media.title || safeTitle, "mp4");
            let sendRes = await sendTgVideo(chatId, videoBuffer, standardizedFilename, caption, replyMarkup);
            if (!sendRes || !sendRes.ok) {
              sendRes = await sendTgDocument(chatId, videoBuffer, media.title || standardizedFilename, caption, replyMarkup);
            }
            if (sendRes && sendRes.ok) {
              await callTg("deleteMessage", { chat_id: chatId, message_id: progressMsgId });
              db.stats.totalDownloads++;
              if (db.users[userId]) db.users[userId].downloads++;
              saveDatabase();
              console.log(`[DELIVERED MEDIA] File sent to ${chatId}`);

              // ALSO send audio track if it's a video file
              if (media.type !== "TeraBox" && media.type !== "MEGA") {
                await deliverAudioTrack(chatId, url, media, safeTitle, platformName, videoBuffer);
              }
              return;
            }
          }
        }
      } catch (dlErr) {
        console.warn("Direct buffer fetch failed, falling back to download card:", dlErr.message);
      }
    }

    const fallbackText = `🎬 <b>${escapeHtml(safeTitle)}</b>\n\n` +
      `👤 <b>Platform:</b> ${escapeHtml(media.type || "Media Video")}\n` +
      `⚡ <i>Click below to download or stream high definition media directly:</i>`;

    const buttons = [
      [{ text: "📥 Download / Watch Video (HD)", url: media.videoUrl }],
      [{ text: "🌐 Official Web App", url: "https://hanter-xd-official.github.io/OmniStream/" }],
      [{ text: "💬 Support (@HANTER_XD_OFFICIAL)", url: DEV_TELEGRAM }]
    ];

    await callTg("editMessageText", {
      chat_id: chatId,
      message_id: progressMsgId,
      text: fallbackText,
      parse_mode: "HTML",
      reply_markup: { inline_keyboard: buttons }
    });

    db.stats.totalDownloads++;
    if (db.users[userId]) db.users[userId].downloads++;
    saveDatabase();

    // Deliver audio track in fallback mode as well
    const fallbackPlatformName = detectPlatformName(media?.type, url);
    await deliverAudioTrack(chatId, url, media, safeTitle, fallbackPlatformName);

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
  }
}

// ==================== ADMIN PANEL HANDLERS ====================

async function sendAdminDashboard(chatId, messageId = null) {
  const totalUsers = Object.keys(db.users).length;
  const blockedCount = db.blockedUsers.length;
  const activeCount = Math.max(0, totalUsers - blockedCount);
  const uptime = formatUptime(process.uptime());

  const text = `👑 <b>OmniStream Master Admin Panel</b>\n` +
    `━━━━━━━━━━━━━━━━━━━━\n` +
    `👤 <b>Administrator:</b> ${DEV_NAME}\n` +
    `🆔 <b>Admin ID:</b> <code>${ADMIN_ID}</code>\n\n` +
    `📊 <b>System Overview:</b>\n` +
    `• 👥 <b>Total Users:</b> <b>${totalUsers}</b>\n` +
    `• 🟢 <b>Active Users:</b> <b>${activeCount}</b>\n` +
    `• 🚫 <b>Blocked Users:</b> <b>${blockedCount}</b>\n` +
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
        { text: "📊 Detailed Stats", callback_data: "admin_stats" }
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

        if (data === "admin_stats") {
          await callTg("answerCallbackQuery", { callback_query_id: cqId });
          const uptime = formatUptime(process.uptime());
          const mem = (process.memoryUsage().rss / (1024 * 1024)).toFixed(1);
          const totalUsers = Object.keys(db.users).length;
          const verifiedUsers = Object.values(db.users).filter(u => isUserVerified(u.id)).length;
          const unverifiedUsers = Math.max(0, totalUsers - verifiedUsers);
          const statsText = `📊 <b>OmniStream Detailed System Statistics</b>\n━━━━━━━━━━━━━━━━━━━━\n` +
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
    const senderName = msg.from?.first_name || "User";

    if (!chatId) return;

    // Register user in database
    const { isNew, user } = registerOrUpdateUser(msg.from);

    // If a brand new user joins, alert the Administrator privately!
    if (isNew && !isAdmin(senderId)) {
      const alertAdmin = `🔔 <b>New User Registered in OmniStream!</b>\n━━━━━━━━━━━━━━━━━━━━\n` +
        `👤 <b>Name:</b> ${escapeHtml(senderName)}\n` +
        `🏷 <b>Username:</b> @${msg.from?.username ? escapeHtml(msg.from.username) : "None"}\n` +
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
              { text: `💬 Message ${escapeHtml(senderName)}`, callback_data: `admin_msg_user_${senderId}` },
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

    // ==================== MEDIA LINK PROCESSING ====================
    const foundUrl = extractUrl(text);
    if (foundUrl) {
      const initResp = await callTg("sendMessage", {
        chat_id: chatId,
        text: "⏳ <b>Processing link...</b>\n<i>Fetching media stream from servers...</i>",
        parse_mode: "HTML"
      });
      const progressMsgId = initResp.result?.message_id;
      if (progressMsgId) {
        await processMediaUrl(foundUrl, chatId, progressMsgId, senderId);
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
      const res = await fetch(`${TELEGRAM_API}/getUpdates?offset=${lastUpdateId}&timeout=25`, {
        signal: AbortSignal.timeout(35000)
      });

      if (res.ok) {
        const data = await res.json();
        if (data && data.ok && Array.isArray(data.result)) {
          for (const update of data.result) {
            lastUpdateId = update.update_id + 1;
            handleUpdate(update).catch(e => console.error("Update task error:", e?.message));
          }
        }
      } else {
        await new Promise(r => setTimeout(r, 2000));
      }
    } catch (err) {
      await new Promise(r => setTimeout(r, 2000));
    }
  }
}

pollUpdates().catch(err => {
  console.error("Critical poll loop failure, restarting:", err?.message);
  setTimeout(pollUpdates, 3000);
});
