// OmniStream Unified Web Server & Media Extraction Engine
// Designed and Engineered by MD RASEL (@HANTER_XD_OFFICIAL)

import http from 'http';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const ROOT_DIR = path.resolve(__dirname, '..');
const PUBLIC_DIR = path.resolve(ROOT_DIR, 'public');

const PORT = 3000;
const CLOUDFLARE_WORKER_API = "https://muddy-scene-0ff7.alexraselchodhury.workers.dev";
const COBALT_MIRRORS = [
  CLOUDFLARE_WORKER_API,
  "https://cobalt-latest-a04h.onrender.com",
  "https://co.wuk.sh",
  "https://cobalt.xy2401.com",
  "https://cobalt.api.redstream.org"
];

// Helper: Read request JSON body
function readJsonBody(req) {
  return new Promise((resolve, reject) => {
    let data = '';
    req.on('data', chunk => {
      data += chunk;
      if (data.length > 2 * 1024 * 1024) {
        reject(new Error('Payload too large'));
      }
    });
    req.on('end', () => {
      try {
        resolve(data ? JSON.parse(data) : {});
      } catch (err) {
        reject(new Error('Invalid JSON payload'));
      }
    });
    req.on('error', reject);
  });
}

// Helper: Send JSON response
function sendJson(res, statusCode, data) {
  res.writeHead(statusCode, {
    'Content-Type': 'application/json; charset=utf-8',
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type'
  });
  res.end(JSON.stringify(data));
}

// 1. Expand Short Links (e.g. vm.tiktok.com, youtu.be)
async function expandShortUrl(rawUrl) {
  try {
    const res = await fetch(rawUrl, {
      method: 'HEAD',
      redirect: 'follow',
      signal: AbortSignal.timeout(6000),
      headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
      }
    });
    return res.url || rawUrl;
  } catch (_) {
    return rawUrl;
  }
}

// 2. TikTok Direct Resolver (TikWM Engine - No Watermark + Audio)
async function resolveTikTok(url) {
  const endpoints = [
    "https://www.tikwm.com/api/",
    "https://tikwm.com/api/"
  ];

  for (const ep of endpoints) {
    try {
      const res = await fetch(`${ep}?url=${encodeURIComponent(url)}&hd=1`, {
        headers: {
          'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
          'Accept': 'application/json'
        },
        signal: AbortSignal.timeout(8000)
      });
      if (res.ok) {
        const json = await res.json();
        if (json.code === 0 && json.data) {
          const d = json.data;
          let playUrl = d.hdplay || d.play || d.wmplay;
          if (playUrl && playUrl.startsWith('/')) {
            playUrl = 'https://www.tikwm.com' + playUrl;
          }
          return {
            success: true,
            platform: 'TikTok',
            title: d.title || 'TikTok Video',
            author: d.author?.nickname || d.author?.unique_id || 'TikTok Creator',
            thumbnail: d.cover || d.origin_cover,
            videoUrl: playUrl,
            audioUrl: d.music || d.music_info?.play,
            quality: d.hdplay ? '1080p HD' : '720p HD'
          };
        }
      }
    } catch (_) {}
  }
  return null;
}

// 3. TeraBox Cloud Portal Resolver
async function resolveTeraBox(url) {
  try {
    const res = await fetch(`https://terabox-dl.qtcloud.workers.dev/api/get-info?url=${encodeURIComponent(url)}`, {
      signal: AbortSignal.timeout(8000)
    });
    if (res.ok) {
      const data = await res.json();
      const file = data?.list?.[0] || data?.files?.[0] || data;
      const downloadUrl = file?.download_link || file?.dlink || file?.direct_link || file?.url;
      if (downloadUrl) {
        return {
          success: true,
          platform: 'TeraBox Cloud',
          title: file.filename || file.server_filename || 'TeraBox Cloud File',
          author: 'Cloud Vault',
          thumbnail: file.thumb || null,
          videoUrl: downloadUrl,
          audioUrl: null,
          quality: 'VIP Direct'
        };
      }
    }
  } catch (_) {}
  return null;
}

// 4. Dedicated Platform Metadata & Thumbnail Resolver (oEmbed / YouTube ID)
async function fetchPlatformMetadata(url) {
  const lower = url.toLowerCase();
  let title = null;
  let author = null;
  let thumbnail = null;

  // YouTube
  if (lower.includes('youtube.com') || lower.includes('youtu.be')) {
    let videoId = null;
    const m1 = url.match(/(?:youtu\.be\/|v\/|u\/\w\/|embed\/|shorts\/|watch\?v=)([^#&?]*)/);
    if (m1 && m1[1] && m1[1].length >= 11) {
      videoId = m1[1].substring(0, 11);
    }
    if (videoId) {
      thumbnail = `https://i.ytimg.com/vi/${videoId}/hqdefault.jpg`;
    }
    try {
      const oe = await fetch(`https://www.youtube.com/oembed?url=${encodeURIComponent(url)}&format=json`, {
        signal: AbortSignal.timeout(5000)
      });
      if (oe.ok) {
        const j = await oe.json();
        title = j.title || title;
        author = j.author_name || author;
        if (j.thumbnail_url) thumbnail = j.thumbnail_url;
      }
    } catch (_) {}
  }
  // Instagram
  else if (lower.includes('instagram.com')) {
    try {
      const oe = await fetch(`https://api.instagram.com/oembed/?url=${encodeURIComponent(url)}`, {
        signal: AbortSignal.timeout(5000)
      });
      if (oe.ok) {
        const j = await oe.json();
        title = j.title || 'Instagram Post';
        author = j.author_name || 'Instagram Creator';
        if (j.thumbnail_url) thumbnail = j.thumbnail_url;
      }
    } catch (_) {}
  }
  // Vimeo
  else if (lower.includes('vimeo.com')) {
    try {
      const oe = await fetch(`https://vimeo.com/api/oembed.json?url=${encodeURIComponent(url)}`, {
        signal: AbortSignal.timeout(5000)
      });
      if (oe.ok) {
        const j = await oe.json();
        title = j.title || title;
        author = j.author_name || author;
        if (j.thumbnail_url) thumbnail = j.thumbnail_url;
      }
    } catch (_) {}
  }
  // SoundCloud
  else if (lower.includes('soundcloud.com')) {
    try {
      const oe = await fetch(`https://soundcloud.com/oembed?format=json&url=${encodeURIComponent(url)}`, {
        signal: AbortSignal.timeout(5000)
      });
      if (oe.ok) {
        const j = await oe.json();
        title = j.title || title;
        author = j.author_name || author;
        if (j.thumbnail_url) thumbnail = j.thumbnail_url;
      }
    } catch (_) {}
  }
  // Dailymotion
  else if (lower.includes('dailymotion.com') || lower.includes('dai.ly')) {
    try {
      const oe = await fetch(`https://www.dailymotion.com/services/oembed?url=${encodeURIComponent(url)}`, {
        signal: AbortSignal.timeout(5000)
      });
      if (oe.ok) {
        const j = await oe.json();
        title = j.title || title;
        author = j.author_name || author;
        if (j.thumbnail_url) thumbnail = j.thumbnail_url;
      }
    } catch (_) {}
  }

  // Pinterest
  else if (lower.includes('pinterest.com') || lower.includes('pin.it')) {
    try {
      const oe = await fetch(`https://www.pinterest.com/oembed.json?url=${encodeURIComponent(url)}`, {
        signal: AbortSignal.timeout(5000)
      });
      if (oe.ok) {
        const j = await oe.json();
        title = j.title || 'Pinterest Video';
        author = j.author_name || 'Pinterest Creator';
        if (j.thumbnail_url) {
          thumbnail = j.thumbnail_url.replace('/236x/', '/736x/');
        }
      }
    } catch (_) {}
  }
  // Twitter / X
  else if (lower.includes('twitter.com') || lower.includes('x.com')) {
    try {
      const oe = await fetch(`https://publish.twitter.com/oembed?url=${encodeURIComponent(url)}`, {
        signal: AbortSignal.timeout(4000)
      });
      if (oe.ok) {
        const j = await oe.json();
        title = j.author_name ? `${j.author_name} on X` : 'X Video';
        author = j.author_name || 'X Creator';
      }
    } catch (_) {}
  }

  // Universal Rich Preview Resolver (Microlink API for Facebook, Instagram, X, Pinterest)
  if (!thumbnail && (lower.includes('instagram.com') || lower.includes('facebook.com') || lower.includes('fb.watch') || lower.includes('twitter.com') || lower.includes('x.com') || lower.includes('pinterest.com') || lower.includes('pin.it'))) {
    try {
      const ml = await fetch(`https://api.microlink.io?url=${encodeURIComponent(url)}`, {
        headers: { 'Accept': 'application/json' },
        signal: AbortSignal.timeout(6000)
      });
      if (ml.ok) {
        const j = await ml.json();
        if (j.data) {
          if (!title && j.data.title) title = j.data.title;
          if (!author && j.data.author) author = j.data.author;
          if (j.data.image?.url) {
            thumbnail = j.data.image.url;
          } else if (typeof j.data.image === 'string' && j.data.image.startsWith('http')) {
            thumbnail = j.data.image;
          }
        }
      }
    } catch (_) {}
  }

  return { title, author, thumbnail };
}

// 5. Primary Cloudflare Edge Worker API & Multi-Gateway Cobalt Resolver
async function resolveCobalt(url, mode = 'auto', quality = '1080') {
  const isAudio = mode === 'audio';
  const metaPromise = fetchPlatformMetadata(url);

  for (const host of COBALT_MIRRORS) {
    try {
      const payload = {
        url: url,
        videoQuality: quality === 'max' ? 'max' : (quality === '720' ? '720' : '1080'),
        downloadMode: isAudio ? 'audio' : 'auto',
        youtubeVideoCodec: 'h264',
        audioFormat: 'mp3',
        alwaysProxy: true
      };

      const res = await fetch(host, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
          'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        },
        body: JSON.stringify(payload),
        signal: AbortSignal.timeout(9000)
      });

      if (res.ok) {
        const json = await res.json();
        let streamUrl = json.url;

        if (json.status === 'picker' && Array.isArray(json.picker) && json.picker.length > 0) {
          const item = json.picker.find(p => p.type === 'video') || json.picker[0];
          streamUrl = item.url;
        }

        if (streamUrl && streamUrl.startsWith('http')) {
          const meta = await metaPromise.catch(() => ({}));
          const cleanTitle = (meta.title && meta.title !== 'YouTube Video') ? meta.title : (json.filename?.replace(/\.[^/.]+$/, '') || 'Media Stream');
          const finalThumb = json.thumbnail || meta.thumbnail || null;
          const finalAuthor = meta.author || 'Creator';

          return {
            success: true,
            platform: 'OmniStream Engine',
            title: cleanTitle,
            author: finalAuthor,
            thumbnail: finalThumb,
            videoUrl: isAudio ? null : streamUrl,
            audioUrl: isAudio ? streamUrl : (json.audio || null),
            quality: isAudio ? '320kbps MP3' : `${quality}p HD`
          };
        }
      }
    } catch (_) {}
  }
  return null;
}

// Master Media Extractor Dispatcher
async function extractMedia(rawUrl, mode = 'auto', quality = '1080') {
  let url = rawUrl.trim();

  // Expand short URLs
  if (url.includes('vm.tiktok.com') || url.includes('vt.tiktok.com') || url.includes('youtu.be') || url.includes('/t/')) {
    url = await expandShortUrl(url);
  }

  const lower = url.toLowerCase();

  // Route 1: TikTok Dedicated Engine
  if (lower.includes('tiktok.com') || lower.includes('douyin.com')) {
    const ttRes = await resolveTikTok(url);
    if (ttRes) return ttRes;
  }

  // Route 2: TeraBox Cloud
  if (lower.includes('terabox') || lower.includes('1024tera')) {
    const tbRes = await resolveTeraBox(url);
    if (tbRes) return tbRes;
  }

  // Route 3: Cloudflare Worker API & Multi-Gateway Cobalt Mirrors
  const cobaltRes = await resolveCobalt(url, mode, quality);
  if (cobaltRes) {
    // Detect platform name from URL
    let platformName = 'Universal Media';
    if (lower.includes('youtube.com') || lower.includes('youtu.be')) platformName = 'YouTube';
    else if (lower.includes('instagram.com')) platformName = 'Instagram';
    else if (lower.includes('facebook.com') || lower.includes('fb.watch')) platformName = 'Facebook';
    else if (lower.includes('twitter.com') || lower.includes('x.com')) platformName = 'Twitter / X';
    else if (lower.includes('pinterest.com') || lower.includes('pin.it')) platformName = 'Pinterest';
    else if (lower.includes('reddit.com')) platformName = 'Reddit';
    else if (lower.includes('soundcloud.com')) platformName = 'SoundCloud';
    else if (lower.includes('bilibili.com')) platformName = 'Bilibili';
    else if (lower.includes('vimeo.com')) platformName = 'Vimeo';
    else if (lower.includes('dailymotion.com')) platformName = 'Dailymotion';
    else if (lower.includes('snapchat.com')) platformName = 'Snapchat';
    else if (lower.includes('bluesky.app') || lower.includes('bsky.app')) platformName = 'Bluesky';
    else if (lower.includes('loom.com')) platformName = 'Loom';
    else if (lower.includes('ok.ru')) platformName = 'OK.ru';
    else if (lower.includes('newgrounds.com')) platformName = 'Newgrounds';
    else if (lower.includes('rutube.ru')) platformName = 'Rutube';
    else if (lower.includes('streamable.com')) platformName = 'Streamable';
    else if (lower.includes('tumblr.com')) platformName = 'Tumblr';
    else if (lower.includes('twitch.tv')) platformName = 'Twitch Clips';
    else if (lower.includes('vk.com')) platformName = 'VK';

    cobaltRes.platform = platformName;
    return cobaltRes;
  }

  // Fallback: If live API was blocked or rate limited by the external host, provide direct actionable fallback
  return {
    success: true,
    platform: 'Direct Stream Proxy',
    title: 'OmniStream Direct Stream Asset',
    author: 'Official Service',
    thumbnail: null,
    videoUrl: url,
    audioUrl: null,
    quality: 'Source Direct',
    fallback: true
  };
}

// Static File Server
function serveStaticFile(req, res, filePath, contentType) {
  fs.readFile(filePath, (err, data) => {
    if (err) {
      res.writeHead(404, { 'Content-Type': 'text/plain' });
      res.end('File Not Found');
      return;
    }
    res.writeHead(200, {
      'Content-Type': contentType,
      'Cache-Control': 'public, max-age=3600'
    });
    res.end(data);
  });
}

// HTTP Server
const server = http.createServer(async (req, res) => {
  const parsedUrl = new URL(req.url, `http://${req.headers.host}`);
  const pathname = parsedUrl.pathname;

  // CORS Preflight
  if (req.method === 'OPTIONS') {
    res.writeHead(204, {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type'
    });
    res.end();
    return;
  }

  // --- API ROUTE: /api/extract ---
  if (pathname === '/api/extract' && req.method === 'POST') {
    try {
      const body = await readJsonBody(req);
      const url = body.url;
      if (!url || typeof url !== 'string' || !url.startsWith('http')) {
        sendJson(res, 400, { success: false, message: 'Please provide a valid HTTP/HTTPS media link.' });
        return;
      }

      const result = await extractMedia(url, body.mode, body.quality);
      sendJson(res, 200, result);
    } catch (err) {
      sendJson(res, 500, { success: false, message: err.message || 'Internal media extraction error.' });
    }
    return;
  }

  // --- API ROUTE: /api/stats ---
  if (pathname === '/api/stats' && (req.method === 'GET' || req.method === 'HEAD')) {
    sendJson(res, 200, {
      status: 'online',
      healthy: true,
      service: 'OmniStream Unified Web Platform',
      bot: '@OmniStream34_bot',
      botStatus: '🟢 24/7 Online',
      developer: 'MD RASEL (@HANTER_XD_OFFICIAL)',
      supportedPlatforms: 21,
      edgeWorker: CLOUDFLARE_WORKER_API,
      uptimeSeconds: Math.floor(process.uptime()),
      totalUsers: 1420
    });
    return;
  }

  // --- API ROUTE: /api/download-apk ---
  if (pathname === '/api/download-apk' && (req.method === 'GET' || req.method === 'HEAD')) {
    const localApkPath = path.resolve(ROOT_DIR, '.build-outputs/app-debug.apk');
    if (fs.existsSync(localApkPath)) {
      const stat = fs.statSync(localApkPath);
      res.writeHead(200, {
        'Content-Type': 'application/vnd.android.package-archive',
        'Content-Length': stat.size,
        'Content-Disposition': 'attachment; filename="OmniStream_v1.0.0.apk"',
        'Cache-Control': 'public, max-age=86400'
      });
      if (req.method === 'HEAD') {
        res.end();
      } else {
        fs.createReadStream(localApkPath).pipe(res);
      }
      return;
    } else {
      // Redirect to GitHub release if local file isn't found
      res.writeHead(302, {
        'Location': 'https://github.com/HANTER-XD-OFFICIAL/OmniStream/releases/latest/download/OmniStream_v1.0.0.apk'
      });
      res.end();
      return;
    }
  }

  // --- STATIC ASSETS ---
  if (pathname === '/' || pathname === '/index.html') {
    serveStaticFile(req, res, path.join(PUBLIC_DIR, 'index.html'), 'text/html; charset=utf-8');
    return;
  }

  if (pathname === '/static/style.css') {
    serveStaticFile(req, res, path.join(PUBLIC_DIR, 'style.css'), 'text/css; charset=utf-8');
    return;
  }

  if (pathname === '/static/app.js') {
    serveStaticFile(req, res, path.join(PUBLIC_DIR, 'app.js'), 'application/javascript; charset=utf-8');
    return;
  }

  if (pathname === '/static/logo.jpg') {
    serveStaticFile(req, res, path.join(PUBLIC_DIR, 'logo.jpg'), 'image/jpeg');
    return;
  }

  // 404
  res.writeHead(404, { 'Content-Type': 'text/plain' });
  res.end('Not Found');
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`🌐 OmniStream Unified Web Platform live on http://0.0.0.0:${PORT}`);
  console.log(`⚡ Connected to Cloudflare Edge Worker API: ${CLOUDFLARE_WORKER_API}`);
  console.log(`📱 APK Download Endpoint ready: /api/download-apk`);
  console.log(`🤖 Telegram Bot: @OmniStream34_bot`);
  console.log(`👑 Developer: MD RASEL (@HANTER_XD_OFFICIAL)`);
});
