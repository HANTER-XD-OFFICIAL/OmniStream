// OmniStream - Official Telegram Bot Daemon (24/7 Production Engine)
// Bot Username: @OmniStream34_bot
// Developer: MD Rasel (@HANTER_XD_OFFICIAL)
// Language Policy: STRICTLY 100% ENGLISH FOR ALL USER-FACING BOT MESSAGES

import process from 'node:process';

// Catch ALL unhandled errors to guarantee 100% 24/7 uptime without crashes
process.on('uncaughtException', (err) => {
  console.error('[FATAL CAUGHT] Uncaught Exception:', err?.message || err);
});
process.on('unhandledRejection', (reason) => {
  console.error('[FATAL CAUGHT] Unhandled Rejection:', reason);
});

const BOT_TOKEN = process.env.BOT_TOKEN || "8451030732:AAEK2MnsTmdJbhqQVMtUik4s58TuNZFHo18";
const TELEGRAM_API = `https://api.telegram.org/bot${BOT_TOKEN}`;
const DEV_TELEGRAM = "https://t.me/HANTER_XD_OFFICIAL";
const DEV_NAME = "MD Rasel (@HANTER_XD_OFFICIAL)";

console.log("🚀 Starting OmniStream Bot (@OmniStream34_bot) 24/7 Resilient Daemon (English Mode)...");

// Safe Telegram API call
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

// Send Video directly to chat
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

// Extract first URL
function extractUrl(text) {
  if (!text) return null;
  const match = text.match(/https?:\/\/[^\s]+/i);
  return match ? match[0] : null;
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

// ==================== RESOLVERS ====================

// 1. TikTok Resolver (TikWM)
async function resolveTikTok(url) {
  try {
    const apiUrl = `https://www.tikwm.com/api/?url=${encodeURIComponent(url)}`;
    const res = await fetch(apiUrl, {
      headers: { "User-Agent": "Mozilla/5.0" },
      signal: AbortSignal.timeout(12000)
    });
    if (!res.ok) return null;
    const json = await res.json();
    if (json.code !== 0 || !json.data) return null;

    const data = json.data;
    const playUrl = data.play || data.hdplay;
    return {
      type: "TikTok",
      title: data.title || "TikTok Video",
      author: data.author?.nickname || "TikTok Creator",
      duration: data.duration || 15,
      cover: data.cover || data.origin_cover,
      videoUrl: playUrl,
      audioUrl: data.music || data.music_info?.play,
      directStream: true
    };
  } catch (err) {
    console.error("TikTok error:", err.message);
    return null;
  }
}

// 2. Cobalt Multi-Host Resolver (Instagram, Facebook, Twitter, Reddit)
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

// 3. Dedicated YouTube Resolver (Loader.to Full Poll Cycle + oEmbed)
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

    // Method: Loader.to API
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
              await onProgressUpdate("⏳ <b>Converting YouTube Video...</b>\n<i>Rendering high-definition MP4 stream...</i>");
            }
            // Poll up to 14 times (28 seconds max)
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

    // Direct web stream fallback with thumb
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

// ==================== PROCESS URL ====================

async function processMediaUrl(rawUrl, chatId, progressMsgId) {
  const url = rawUrl.trim();
  console.log(`[PROCESS] URL: ${url} for Chat: ${chatId}`);

  let media = null;
  const lower = url.toLowerCase();

  try {
    if (lower.includes("tiktok.com")) {
      media = await resolveTikTok(url);
    } else if (lower.includes("youtube.com") || lower.includes("youtu.be")) {
      media = await resolveYouTube(url, async (statusText) => {
        await callTg("editMessageText", {
          chat_id: chatId,
          message_id: progressMsgId,
          text: statusText,
          parse_mode: "HTML"
        });
      });
    } else if (lower.includes("terabox") || lower.includes("1024tera") || lower.includes("terasharelink")) {
      media = await resolveTeraBox(url);
    } else {
      // Instagram, Facebook, Twitter, Pinterest, etc.
      media = await resolveCobalt(url);
    }

    if (!media || !media.videoUrl) {
      await callTg("editMessageText", {
        chat_id: chatId,
        message_id: progressMsgId,
        text: `⚠️ <b>Direct Stream Notice</b>\n\nCould not extract a direct video file from this specific link.\n\n📱 <b>Tip:</b> Try pasting this link in the <b>OmniStream Android App</b> for deep-scan multi-threaded download!`,
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

    // If media is a direct stream and NOT just the original webpage url
    if (media.directStream && media.videoUrl !== url) {
      await callTg("editMessageText", {
        chat_id: chatId,
        message_id: progressMsgId,
        text: `⚡ <b>Ready:</b> ${escapeHtml(shortTitle)}\n📥 <i>Downloading video file & sending to Telegram...</i>`,
        parse_mode: "HTML"
      });

      try {
        // Check headers first to prevent OOM on large videos (> 45MB)
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

        // Telegram Bot API has a strict 50MB file upload limit
        if (contentLength > 45 * 1024 * 1024) {
          const sizeMb = (contentLength / (1024 * 1024)).toFixed(1);
          const largeText = `🎬 <b>${escapeHtml(safeTitle)}</b>\n\n` +
            `👤 <b>Platform:</b> ${escapeHtml(media.type || "Media Video")}\n` +
            `💾 <b>File Size:</b> ${sizeMb} MB (Exceeds Telegram 50MB Bot Limit)\n\n` +
            `⚡ <i>Click the button below to download the high-definition video directly:</i>`;

          await callTg("editMessageText", {
            chat_id: chatId,
            message_id: progressMsgId,
            text: largeText,
            parse_mode: "HTML",
            reply_markup: {
              inline_keyboard: [
                [{ text: `📥 Download Full HD Video (${sizeMb} MB)`, url: media.videoUrl }],
                [{ text: "👨‍💻 Developer Profile", url: DEV_TELEGRAM }]
              ]
            }
          });
          return;
        }

        const vidRes = await fetch(media.videoUrl, {
          headers: {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
            "Referer": url
          },
          signal: AbortSignal.timeout(60000)
        });

        if (vidRes.ok) {
          const clHeader = parseInt(vidRes.headers.get("content-length") || "0", 10);
          if (clHeader > 45 * 1024 * 1024) {
            throw new Error("File exceeds 45MB limit for direct Telegram upload");
          }

          const videoBuffer = await vidRes.arrayBuffer();
          const sizeBytes = videoBuffer.byteLength;
          const sizeMb = (sizeBytes / (1024 * 1024)).toFixed(1);

          if (sizeBytes <= 48 * 1024 * 1024 && sizeBytes > 1000) {
            const caption = `🎬 <b>${escapeHtml(safeTitle)}</b>\n\n` +
              `👤 <b>Platform:</b> ${escapeHtml(media.type || "Web Video")}\n` +
              (media.duration ? `⏱ <b>Duration:</b> ${formatSeconds(media.duration)}\n` : "") +
              `💾 <b>Size:</b> ${sizeMb} MB\n\n` +
              `⚡ <i>Downloaded via OmniStream Bot (@OmniStream34_bot)</i>`;

            const replyMarkup = {
              inline_keyboard: [
                [{ text: "🌐 Direct HD Stream Link", url: media.videoUrl }],
                [{ text: "👨‍💻 Developer Profile", url: DEV_TELEGRAM }]
              ]
            };

            const sendRes = await sendTgVideo(chatId, videoBuffer, "video.mp4", caption, replyMarkup);
            if (sendRes.ok) {
              await callTg("deleteMessage", { chat_id: chatId, message_id: progressMsgId });
              console.log(`[DELIVERED] Video sent to ${chatId}`);
              return;
            }
          }
        }
      } catch (dlErr) {
        console.warn("Direct buffer fetch failed, falling back to download card:", dlErr.message);
      }
    }

    // Fallback card with direct download button
    const fallbackText = `🎬 <b>${escapeHtml(safeTitle)}</b>\n\n` +
      `👤 <b>Platform:</b> ${escapeHtml(media.type || "Media Video")}\n` +
      `⚡ <i>Click below to download or stream high definition media directly:</i>`;

    const buttons = [
      [{ text: "📥 Download / Watch Video (HD)", url: media.videoUrl }],
      [{ text: "💬 Support (@HANTER_XD_OFFICIAL)", url: DEV_TELEGRAM }]
    ];

    await callTg("editMessageText", {
      chat_id: chatId,
      message_id: progressMsgId,
      text: fallbackText,
      parse_mode: "HTML",
      reply_markup: { inline_keyboard: buttons }
    });

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

// ==================== UPDATE HANDLER ====================

async function handleUpdate(update) {
  try {
    if (!update || !update.message) return;
    const msg = update.message;
    const chatId = msg.chat?.id;
    const text = (msg.text || "").trim();
    const sender = msg.from?.first_name || "User";

    if (!chatId) return;

    if (text.startsWith("/start")) {
      const welcomeText = `👋 <b>Welcome, ${escapeHtml(sender)}!</b>\n\n` +
        `🤖 I am <b>OmniStream Official Bot</b> (@OmniStream34_bot).\n` +
        `Download any social media video and audio in Full HD without watermarks!\n\n` +
        `🌟 <b>Supported Platforms:</b>\n` +
        `• <b>YouTube</b> (Shorts, HD Videos, Audio)\n` +
        `• <b>TikTok</b> (HD No-Watermark MP4 & MP3)\n` +
        `• <b>Instagram</b> (Reels, Posts, Stories)\n` +
        `• <b>Facebook</b> (Reels, Watch Videos)\n` +
        `• <b>TeraBox</b> (Direct Fast Download)\n` +
        `• <b>Twitter / X</b> (Clips & Videos)\n\n` +
        `🚀 <b>How to Use:</b>\n` +
        `Simply copy and paste any video or post link here! 👇`;

      await callTg("sendMessage", {
        chat_id: chatId,
        text: welcomeText,
        parse_mode: "HTML",
        reply_markup: {
          inline_keyboard: [
            [{ text: "👨‍💻 Developer (@HANTER_XD_OFFICIAL)", url: DEV_TELEGRAM }]
          ]
        }
      });
      return;
    }

    if (text.startsWith("/help")) {
      await callTg("sendMessage", {
        chat_id: chatId,
        text: `📖 <b>OmniStream Bot Guide</b>\n\n` +
          `1. Copy any video link from YouTube, TikTok, Facebook, Instagram, or TeraBox.\n` +
          `2. Send the link directly to this chat.\n` +
          `3. The bot will automatically fetch and deliver the MP4 video directly to you!\n\n` +
          `👨‍💻 <b>Developer:</b> ${DEV_NAME}`,
        parse_mode: "HTML"
      });
      return;
    }

    const foundUrl = extractUrl(text);
    if (foundUrl) {
      const initResp = await callTg("sendMessage", {
        chat_id: chatId,
        text: "⏳ <b>Processing link...</b>\n<i>Fetching media stream from servers...</i>",
        parse_mode: "HTML"
      });
      const progressMsgId = initResp.result?.message_id;
      if (progressMsgId) {
        await processMediaUrl(foundUrl, chatId, progressMsgId);
      }
    } else {
      await callTg("sendMessage", {
        chat_id: chatId,
        text: "⚠️ <i>Please send a valid media link (TikTok, Facebook, Instagram, YouTube, etc.) to download.</i>",
        parse_mode: "HTML"
      });
    }
  } catch (err) {
    console.error("handleUpdate error:", err.message);
  }
}

// ==================== POLLING LOOP ====================

let lastUpdateId = 0;

async function pollUpdates() {
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
            // Process update asynchronously without blocking polling
            handleUpdate(update).catch(e => console.error("Update task error:", e?.message));
          }
        }
      } else {
        await new Promise(r => setTimeout(r, 2000));
      }
    } catch (err) {
      // Safe network pause on timeout or transient error
      await new Promise(r => setTimeout(r, 2000));
    }
  }
}

pollUpdates().catch(err => {
  console.error("Critical poll loop failure, restarting:", err?.message);
  setTimeout(pollUpdates, 3000);
});
