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
 * High-speed YouTube resolver using direct stream encoder (loader.to / en.loader.to) with RapidAPI failover
 */
async function resolveYouTube(targetUrl, format = "720") {
  const isAudio = format === "mp3" || format === "audio";
  const reqFmt = isAudio ? "mp3" : (format === "1080" || format === "max" ? "1080" : "720");

  const hosts = ["https://loader.to", "https://en.loader.to"];

  for (const host of hosts) {
    try {
      const encUrl = encodeURIComponent(targetUrl);
      const startUrl = `${host}/ajax/download.php?button=1&start=1&end=1&format=${reqFmt}&url=${encUrl}`;

      const res = await fetch(startUrl, {
        headers: {
          "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
          "Referer": `${host}/`
        }
      });

      if (res.ok) {
        const data = await res.json();
        if (data.download_url && data.download_url.startsWith("http")) {
          return { url: data.download_url, title: data.title || "YouTube_Media" };
        }

        if (data.progress_url) {
          // Poll progress URL up to 25 times (every 1s)
          for (let i = 0; i < 25; i++) {
            await new Promise(r => setTimeout(r, 1100));
            try {
              const pRes = await fetch(data.progress_url, {
                headers: { "Referer": `${host}/` }
              });
              if (pRes.ok) {
                const pData = await pRes.json();
                if (pData.download_url && pData.download_url.startsWith("http")) {
                  return { url: pData.download_url, title: pData.title || data.title || "YouTube_Media" };
                }
              }
            } catch (_) {}
          }
        }
      }
    } catch (loaderErr) {
      console.warn(`${host} resolver error:`, loaderErr.message);
    }
  }

  // 2. RapidAPI pool failover
  const ytMatch = targetUrl.match(/(?:youtu\.be\/|youtube\.com\/(?:embed\/|v\/|watch\?v=|shorts\/|live\/|watch\?.+&v=))([\w-]{11})/i);
  const videoId = ytMatch ? ytMatch[1] : null;

  if (videoId) {
    const rapidKeys = [
      "032d76f1d5mshb4bec8c6a6bde50p145398jsn592ea147dc00",
      "daf7c2c2admsh4f57b66f003a149p127d27jsna9e0929c2f69",
      "ec3254c06amsh15d2ab52a9f83a0p181ae1jsn797161360aa4",
      "813fcad230mshf097ffbb0308a63p1e972bjsnd0227bcac6bf",
      "864eb7ae38msh28947dcfcf5ffbbp1f39eejsne5a966599b84",
      "5ab5420addmshc469dee4edfb688p1d11dbjsn1ff8ff1ea86a"
    ];
    const shuffledKeys = [...rapidKeys].sort(() => Math.random() - 0.5);

    for (const key of shuffledKeys) {
      try {
        const epB = isAudio
          ? `https://youtube-mp3-audio-video-downloader.p.rapidapi.com/download/${videoId}?response_mode=default`
          : `https://youtube-mp3-audio-video-downloader.p.rapidapi.com/download/${videoId}?format=720`;
        const resB = await fetch(epB, {
          headers: {
            "x-rapidapi-host": "youtube-mp3-audio-video-downloader.p.rapidapi.com",
            "x-rapidapi-key": key
          }
        });
        if (resB.ok) {
          const jB = await resB.json();
          const candidate = jB.download_url || jB.url || jB.link || jB.result?.url || jB.data?.downloadUrl;
          if (candidate && typeof candidate === "string" && candidate.startsWith("http")) {
            return { url: candidate, title: jB.title || "YouTube_Media" };
          }
        }
      } catch (_) {}
    }
  }

  throw new Error("YouTube stream could not be converted at this time");
}
