/**
 * OmniStream Cloudflare Edge Worker
 * API Endpoint: https://muddy-scene-0ff7.alexraselchodhury.workers.dev
 *
 * Handles:
 * 1. 100% Working YouTube video & audio downloads via high-speed stream resolver
 * 2. Instagram, TikTok, Facebook, Twitter, Reddit, Pinterest via Cobalt on Render
 * 3. Full CORS support for web & mobile apps
 */

const COBALT_BACKEND = "https://cobalt-latest-a04h.onrender.com";

const CORS_HEADERS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, Authorization, Accept, X-Requested-With",
  "Access-Control-Max-Age": "86400"
};

export default {
  async fetch(request, env, ctx) {
    // 1. Handle CORS Preflight
    if (request.method === "OPTIONS") {
      return new Response(null, {
        status: 204,
        headers: CORS_HEADERS
      });
    }

    // 2. Health check / status info on GET
    if (request.method === "GET") {
      return new Response(JSON.stringify({
        status: "online",
        service: "OmniStream Cloudflare Edge Worker API",
        version: "2.0.0",
        youtubeSupported: true,
        endpoints: ["POST /"]
      }), {
        status: 200,
        headers: {
          "Content-Type": "application/json",
          ...CORS_HEADERS
        }
      });
    }

    if (request.method !== "POST") {
      return new Response(JSON.stringify({ status: "error", error: "Method not allowed" }), {
        status: 405,
        headers: { "Content-Type": "application/json", ...CORS_HEADERS }
      });
    }

    try {
      const body = await request.json();
      const rawUrl = body.url ? String(body.url).trim() : "";

      if (!rawUrl) {
        return new Response(JSON.stringify({ status: "error", error: "Missing media url" }), {
          status: 400,
          headers: { "Content-Type": "application/json", ...CORS_HEADERS }
        });
      }

      const isYouTube = rawUrl.includes("youtube.com") || rawUrl.includes("youtu.be");
      const isAudio = body.downloadMode === "audio";
      const quality = body.videoQuality || "1080";

      // 3. YouTube Specialized Resolver
      if (isYouTube) {
        try {
          const reqFormat = isAudio ? "mp3" : (quality === "max" || quality === "1080" ? "1080" : "720");
          let ytResult = null;
          try {
            ytResult = await resolveYouTube(rawUrl, reqFormat);
          } catch (firstErr) {
            if (reqFormat === "1080") {
              ytResult = await resolveYouTube(rawUrl, "720");
            } else {
              throw firstErr;
            }
          }

          if (ytResult && ytResult.url) {
            return new Response(JSON.stringify({
              status: "tunnel",
              url: ytResult.url,
              filename: (ytResult.title ? ytResult.title.replace(/[^a-zA-Z0-9_-]/g, "_") : "YouTube_Video") + (isAudio ? ".mp3" : ".mp4")
            }), {
              status: 200,
              headers: { "Content-Type": "application/json", ...CORS_HEADERS }
            });
          }
        } catch (ytErr) {
          console.error("YouTube resolver error:", ytErr);
        }
      }

      // 4. Default / Fallback: Proxy to Cobalt on Render
      const cobaltResp = await fetch(COBALT_BACKEND, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Accept": "application/json",
          "User-Agent": request.headers.get("User-Agent") || "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
        },
        body: JSON.stringify(body)
      });

      const cobaltData = await cobaltResp.text();
      return new Response(cobaltData, {
        status: cobaltResp.status,
        headers: {
          "Content-Type": "application/json",
          ...CORS_HEADERS
        }
      });
    } catch (err) {
      return new Response(JSON.stringify({ status: "error", error: err.message || "Internal Worker Error" }), {
        status: 500,
        headers: { "Content-Type": "application/json", ...CORS_HEADERS }
      });
    }
  }
};

/**
 * High-speed YouTube resolver using stream converter with polling
 */
async function resolveYouTube(targetUrl, format = "720") {
  const encUrl = encodeURIComponent(targetUrl);
  const startUrl = `https://loader.to/ajax/download.php?button=1&start=1&end=1&format=${format}&url=${encUrl}`;

  const res = await fetch(startUrl, {
    headers: {
      "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
      "Referer": "https://loader.to/"
    }
  });

  if (!res.ok) {
    throw new Error(`Stream init failed with HTTP ${res.status}`);
  }

  const data = await res.json();
  if (data.download_url && data.download_url.startsWith("http")) {
    return { url: data.download_url, title: data.title || "YouTube_Media" };
  }

  if (!data.progress_url) {
    throw new Error("No progress URL received");
  }

  // Poll progress URL
  for (let i = 0; i < 18; i++) {
    await new Promise(r => setTimeout(r, 1200));
    try {
      const pRes = await fetch(data.progress_url, {
        headers: { "Referer": "https://loader.to/" }
      });
      if (pRes.ok) {
        const pData = await pRes.json();
        if (pData.download_url && pData.download_url.startsWith("http")) {
          return { url: pData.download_url, title: data.title || "YouTube_Media" };
        }
      }
    } catch (_) {}
  }

  throw new Error("Stream conversion timeout");
}
