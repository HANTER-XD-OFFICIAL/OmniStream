// OmniStream Unified Web Client
// Engineered by MD RASEL (@HANTER_XD_OFFICIAL)

const PLATFORMS_CATALOG = [
  {
    id: "youtube",
    name: "YouTube",
    category: "social",
    color: "#FF0000",
    domains: ["youtube.com", "youtu.be"],
    features: "8K / 4K / 1080p Video, Shorts & 320k MP3 Audio",
    sampleUrl: "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
    badge: "8K/4K/MP3"
  },
  {
    id: "tiktok",
    name: "TikTok",
    category: "social",
    color: "#00F2FE",
    domains: ["tiktok.com", "douyin.com"],
    features: "HD Videos without watermark, Original Audio Tracks",
    sampleUrl: "https://www.tiktok.com/@tiktok/video/7106594312292453678",
    badge: "No Watermark"
  },
  {
    id: "facebook",
    name: "Facebook",
    category: "social",
    color: "#1877F2",
    domains: ["facebook.com", "fb.watch", "fb.com"],
    features: "Public HD Videos, Reels, Watch & Live Stream Archives",
    sampleUrl: "https://www.facebook.com/watch/?v=1234567890",
    badge: "HD Reels"
  },
  {
    id: "instagram",
    name: "Instagram",
    category: "social",
    color: "#E1306C",
    domains: ["instagram.com", "instagr.am"],
    features: "Reels, Video Posts, Stories & IGTV 1080p High Quality",
    sampleUrl: "https://www.instagram.com/reel/Cx123456789/",
    badge: "1080p Reels"
  },
  {
    id: "twitter",
    name: "Twitter / X",
    category: "social",
    color: "#1DA1F2",
    domains: ["twitter.com", "x.com"],
    features: "Video Tweets, High Bitrate Media, Voice Clips",
    sampleUrl: "https://twitter.com/user/status/1234567890",
    badge: "Direct MP4"
  },
  {
    id: "pinterest",
    name: "Pinterest",
    category: "social",
    color: "#E60023",
    domains: ["pinterest.com", "pin.it"],
    features: "Idea Pins, Video Pins, 1080p Direct MP4 Streams",
    sampleUrl: "https://www.pinterest.com/pin/123456789012345678/",
    badge: "Full HD Pin"
  },
  {
    id: "reddit",
    name: "Reddit",
    category: "social",
    color: "#FF4500",
    domains: ["reddit.com", "v.redd.it"],
    features: "Native Reddit Videos with Audio+Video Merged Stream",
    sampleUrl: "https://www.reddit.com/r/videos/comments/sample123/",
    badge: "Audio Muxed"
  },
  {
    id: "soundcloud",
    name: "SoundCloud",
    category: "audio",
    color: "#FF7700",
    domains: ["soundcloud.com"],
    features: "High quality 320kbps MP3 & Master Lossless Audio",
    sampleUrl: "https://soundcloud.com/artist/sample-track",
    badge: "320kbps MP3"
  },
  {
    id: "bilibili",
    name: "Bilibili",
    category: "video",
    color: "#00A1D6",
    domains: ["bilibili.com", "bilibili.tv", "b23.tv"],
    features: "Main Portal & Bilibili TV, 1080p / 4K 60FPS Streams",
    sampleUrl: "https://www.bilibili.com/video/BV1xx411c7mD",
    badge: "4K 60FPS"
  },
  {
    id: "dailymotion",
    name: "Dailymotion",
    category: "video",
    color: "#0066DC",
    domains: ["dailymotion.com", "dai.ly"],
    features: "Full HD Video Streams & Official Channel Clips",
    sampleUrl: "https://www.dailymotion.com/video/x8abcdef",
    badge: "Full HD"
  },
  {
    id: "snapchat",
    name: "Snapchat",
    category: "social",
    color: "#FFFC00",
    domains: ["snapchat.com"],
    features: "Public Spotlight Videos & Public Stories in HD",
    sampleUrl: "https://www.snapchat.com/spotlight/W7_ED1nYR9",
    badge: "Spotlight"
  },
  {
    id: "vimeo",
    name: "Vimeo",
    category: "video",
    color: "#1AB7EA",
    domains: ["vimeo.com"],
    features: "4K Master Videos, 60FPS High-Bitrate Creator Uploads",
    sampleUrl: "https://vimeo.com/76979871",
    badge: "4K Master"
  },
  {
    id: "bluesky",
    name: "Bluesky",
    category: "social",
    color: "#0085FF",
    domains: ["bsky.app"],
    features: "Decentralized AT Protocol Video & Audio Posts HD",
    sampleUrl: "https://bsky.app/profile/user.bsky.social/post/123456",
    badge: "AT Protocol"
  },
  {
    id: "loom",
    name: "Loom",
    category: "video",
    color: "#625DF5",
    domains: ["loom.com"],
    features: "High-Definition Screen & Camera Video Recordings",
    sampleUrl: "https://www.loom.com/share/abc123def456",
    badge: "Screen HD"
  },
  {
    id: "okru",
    name: "OK.ru",
    category: "video",
    color: "#EE8208",
    domains: ["ok.ru"],
    features: "Public Video Clips & Community Movie Streams",
    sampleUrl: "https://ok.ru/video/1234567890",
    badge: "HD Movie"
  },
  {
    id: "newgrounds",
    name: "Newgrounds",
    category: "audio",
    color: "#FFA500",
    domains: ["newgrounds.com"],
    features: "Original Animations, Indie Movies & Soundtrack Audio",
    sampleUrl: "https://www.newgrounds.com/portal/view/123456",
    badge: "Animations"
  },
  {
    id: "rutube",
    name: "Rutube",
    category: "video",
    color: "#0055FF",
    domains: ["rutube.ru"],
    features: "Full HD Video Streams & Official Channel Archives",
    sampleUrl: "https://rutube.ru/video/1234567890abcdef/",
    badge: "Rutube HD"
  },
  {
    id: "streamable",
    name: "Streamable",
    category: "audio",
    color: "#0F86FF",
    domains: ["streamable.com"],
    features: "Instant Direct 1080p High-Speed MP4 Downloads",
    sampleUrl: "https://streamable.com/moo7b",
    badge: "Instant MP4"
  },
  {
    id: "tumblr",
    name: "Tumblr",
    category: "social",
    color: "#36465D",
    domains: ["tumblr.com"],
    features: "High-Resolution Video Posts & Animated Media Clips",
    sampleUrl: "https://creator.tumblr.com/post/1234567890",
    badge: "Hi-Res"
  },
  {
    id: "twitch",
    name: "Twitch Clips",
    category: "live",
    color: "#9146FF",
    domains: ["twitch.tv", "clips.twitch.tv"],
    features: "Live Stream Highlights, Top Clips 1080p 60FPS",
    sampleUrl: "https://clips.twitch.tv/GloriousSampleClip",
    badge: "60 FPS Clip"
  },
  {
    id: "vk",
    name: "VK (VKontakte)",
    category: "video",
    color: "#0077FF",
    domains: ["vk.com", "vk.ru"],
    features: "VK Videos, Clips, Creator Feeds & Community HD Media",
    sampleUrl: "https://vk.com/video-123456_789012",
    badge: "VK HD"
  },
  {
    id: "terabox",
    name: "TeraBox Cloud (VIP)",
    category: "video",
    color: "#00C48C",
    domains: ["terabox.com", "1024tera.com", "teraboxapp.com", "terabox.app"],
    features: "Fast Direct Cloud File Stream Bypass Engine",
    sampleUrl: "https://terabox.com/s/1sampleKey123",
    badge: "VIP Cloud"
  }
];

document.addEventListener("DOMContentLoaded", () => {
  initUrlDetection();
  initPlatformsGrid();
  initFormHandler();
  initQuickChips();
  initClipboardPaste();
  pollLiveStats();
});

// URL Platform Auto-Detection
function detectPlatform(url) {
  if (!url || typeof url !== "string") return null;
  const lower = url.toLowerCase().trim();
  for (const p of PLATFORMS_CATALOG) {
    if (p.domains.some(d => lower.includes(d))) {
      return p;
    }
  }
  return null;
}

function initUrlDetection() {
  const input = document.getElementById("mediaUrlInput");
  const badge = document.getElementById("detectedPlatformBadge");
  const icon = document.getElementById("detectedPlatformIcon");
  const name = document.getElementById("detectedPlatformName");
  const clearBtn = document.getElementById("clearBtn");

  input.addEventListener("input", () => {
    const val = input.value.trim();
    if (val.length > 0) {
      clearBtn.classList.remove("hidden");
    } else {
      clearBtn.classList.add("hidden");
    }

    const platform = detectPlatform(val);
    if (platform) {
      badge.classList.add("detected");
      badge.style.borderColor = platform.color;
      badge.style.boxShadow = `0 0 16px ${platform.color}40`;
      icon.textContent = "✓";
      name.textContent = `${platform.name} (${platform.badge})`;
    } else if (val.startsWith("http://") || val.startsWith("https://")) {
      badge.classList.add("detected");
      badge.style.borderColor = "var(--cyan-bright)";
      badge.style.boxShadow = "0 0 16px var(--cyan-glow)";
      icon.textContent = "⚡";
      name.textContent = "Universal Media URL";
    } else {
      badge.classList.remove("detected");
      badge.style.borderColor = "";
      badge.style.boxShadow = "";
      icon.textContent = "⚡";
      name.textContent = "Ready to Detect";
    }
  });

  clearBtn.addEventListener("click", () => {
    input.value = "";
    clearBtn.classList.add("hidden");
    input.dispatchEvent(new Event("input"));
    input.focus();
  });
}

// Clipboard Paste
function initClipboardPaste() {
  const pasteBtn = document.getElementById("pasteBtn");
  const input = document.getElementById("mediaUrlInput");

  pasteBtn.addEventListener("click", async () => {
    try {
      const text = await navigator.clipboard.readText();
      if (text && text.startsWith("http")) {
        input.value = text.trim();
        input.dispatchEvent(new Event("input"));
        input.focus();
      }
    } catch (_) {
      input.focus();
    }
  });
}

// 21 Platforms Grid Rendering
function initPlatformsGrid() {
  const grid = document.getElementById("platformsGrid");
  const filterBtns = document.querySelectorAll(".filter-btn");

  function render(category = "all") {
    grid.innerHTML = "";
    const filtered = category === "all" 
      ? PLATFORMS_CATALOG 
      : PLATFORMS_CATALOG.filter(p => p.category === category);

    filtered.forEach(p => {
      const card = document.createElement("div");
      card.className = "platform-card";
      card.innerHTML = `
        <div>
          <div class="platform-card-header">
            <h4 class="platform-card-title">${p.name}</h4>
            <div class="platform-badge-circle" style="background-color: ${p.color}; box-shadow: 0 0 10px ${p.color};"></div>
          </div>
          <p class="platform-card-features">${p.features}</p>
        </div>
        <div class="platform-card-footer">
          <span class="platform-card-cat">${p.badge}</span>
          <button type="button" class="btn-card-try" data-url="${p.sampleUrl}">Try Sample</button>
        </div>
      `;
      grid.appendChild(card);
    });

    // Attach click listeners to "Try Sample" buttons
    grid.querySelectorAll(".btn-card-try").forEach(btn => {
      btn.addEventListener("click", (e) => {
        const url = e.target.getAttribute("data-url");
        loadAndExtractSample(url);
      });
    });
  }

  filterBtns.forEach(btn => {
    btn.addEventListener("click", () => {
      filterBtns.forEach(b => b.classList.remove("active"));
      btn.classList.add("active");
      render(btn.getAttribute("data-category"));
    });
  });

  render("all");
}

function initQuickChips() {
  document.querySelectorAll(".sample-chip").forEach(chip => {
    chip.addEventListener("click", () => {
      const url = chip.getAttribute("data-url");
      loadAndExtractSample(url);
    });
  });
}

function loadAndExtractSample(url) {
  const input = document.getElementById("mediaUrlInput");
  input.value = url;
  input.dispatchEvent(new Event("input"));
  window.scrollTo({ top: document.getElementById("downloader").offsetTop - 40, behavior: "smooth" });
  document.getElementById("downloadForm").dispatchEvent(new Event("submit"));
}

// Media Extraction Form
function initFormHandler() {
  const form = document.getElementById("downloadForm");
  const input = document.getElementById("mediaUrlInput");
  const formatSelect = document.getElementById("formatSelect");
  const submitBtn = document.getElementById("submitBtn");

  const statusBox = document.getElementById("statusBox");
  const statusTitle = document.getElementById("statusTitle");
  const statusSub = document.getElementById("statusSub");

  const errorBox = document.getElementById("errorBox");
  const errorMessage = document.getElementById("errorMessage");

  const resultCard = document.getElementById("resultCard");
  const resultThumbnail = document.getElementById("resultThumbnail");
  const resultTitle = document.getElementById("resultTitle");
  const resultPlatformTag = document.getElementById("resultPlatformTag");
  const resultQualityTag = document.getElementById("resultQualityTag");
  const resultAuthorTag = document.getElementById("resultAuthorTag");
  const downloadButtonsGrid = document.getElementById("downloadButtonsGrid");

  const togglePreviewBtn = document.getElementById("togglePreviewBtn");
  const previewPlayerWrapper = document.getElementById("previewPlayerWrapper");
  const videoPreview = document.getElementById("videoPreview");
  const audioPreview = document.getElementById("audioPreview");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const url = input.value.trim();
    if (!url) return;

    // Reset views
    errorBox.classList.add("hidden");
    resultCard.classList.add("hidden");
    previewPlayerWrapper.classList.add("hidden");
    videoPreview.pause();
    audioPreview.pause();
    videoPreview.src = "";
    audioPreview.src = "";

    // Show loading
    statusBox.classList.remove("hidden");
    statusTitle.textContent = "Connecting to Cloudflare Worker API...";
    statusSub.textContent = "Dispatching request to muddy-scene-0ff7.alexraselchodhury.workers.dev...";
    submitBtn.disabled = true;

    const detected = detectPlatform(url);

    try {
      setTimeout(() => {
        if (!statusBox.classList.contains("hidden")) {
          statusTitle.textContent = "Resolving high-speed media stream...";
          statusSub.textContent = `Resolving audio/video stream for ${detected ? detected.name : "Platform"}...`;
        }
      }, 1200);

      const resp = await fetch("/api/extract", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          url: url,
          mode: formatSelect.value,
          quality: formatSelect.value === "audio" ? "320" : formatSelect.value
        })
      });

      const data = await resp.json();

      if (!resp.ok || !data.success) {
        throw new Error(data.message || data.error || "Failed to resolve media stream");
      }

      // Hide loading
      statusBox.classList.add("hidden");

      // Display results
      resultCard.classList.remove("hidden");
      resultTitle.textContent = data.title || "OmniStream Media File";
      resultPlatformTag.textContent = data.platform || (detected ? detected.name : "Universal");
      resultQualityTag.textContent = data.quality || "1080p HD";
      resultAuthorTag.textContent = data.author || "Creator";

      // Thumbnail
      if (data.thumbnail) {
        resultThumbnail.src = data.thumbnail;
        resultThumbnail.classList.remove("hidden");
      } else {
        resultThumbnail.src = "/static/logo.jpg";
      }

      // Preview setup
      const streamUrl = data.videoUrl || data.streamUrl || data.downloadUrl;
      const audioUrl = data.audioUrl;

      togglePreviewBtn.onclick = () => {
        const isCurrentlyHidden = previewPlayerWrapper.classList.contains("hidden");
        if (isCurrentlyHidden) {
          previewPlayerWrapper.classList.remove("hidden");
          if (streamUrl && !streamUrl.endsWith(".mp3")) {
            videoPreview.classList.remove("hidden");
            audioPreview.classList.add("hidden");
            videoPreview.src = streamUrl;
            videoPreview.play().catch(() => {});
          } else if (audioUrl || (streamUrl && streamUrl.endsWith(".mp3"))) {
            audioPreview.classList.remove("hidden");
            videoPreview.classList.add("hidden");
            audioPreview.src = audioUrl || streamUrl;
            audioPreview.play().catch(() => {});
          }
          togglePreviewBtn.querySelector("span").textContent = "Hide Preview";
        } else {
          previewPlayerWrapper.classList.add("hidden");
          videoPreview.pause();
          audioPreview.pause();
          togglePreviewBtn.querySelector("span").textContent = "Preview in Browser";
        }
      };

      // Populate Download Buttons
      downloadButtonsGrid.innerHTML = "";

      // Primary Video/Media Download
      if (streamUrl) {
        const dlBtn = document.createElement("a");
        dlBtn.href = streamUrl;
        dlBtn.target = "_blank";
        dlBtn.rel = "noopener noreferrer";
        dlBtn.setAttribute("download", `${sanitizeFilename(data.title || "OmniStream_Download")}.mp4`);
        dlBtn.className = "btn-stream-dl btn-stream-video";
        dlBtn.innerHTML = `
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
          <span>🎬 Download Video (${data.quality || "HD"})</span>
        `;
        downloadButtonsGrid.appendChild(dlBtn);
      }

      // Audio Download (if distinct audio stream exists)
      if (audioUrl) {
        const audioBtn = document.createElement("a");
        audioBtn.href = audioUrl;
        audioBtn.target = "_blank";
        audioBtn.rel = "noopener noreferrer";
        audioBtn.setAttribute("download", `${sanitizeFilename(data.title || "OmniStream_Audio")}.mp3`);
        audioBtn.className = "btn-stream-dl btn-stream-audio";
        audioBtn.innerHTML = `
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 18V5l12-2v13"/><circle cx="6" cy="18" r="3"/><circle cx="18" cy="16" r="3"/></svg>
          <span>🎵 Download MP3 Audio</span>
        `;
        downloadButtonsGrid.appendChild(audioBtn);
      }

      // Copy Stream Link Button
      const copyBtn = document.createElement("button");
      copyBtn.type = "button";
      copyBtn.className = "btn-stream-dl btn-stream-copy";
      copyBtn.innerHTML = `
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>
        <span>Copy Direct Link</span>
      `;
      copyBtn.onclick = () => {
        navigator.clipboard.writeText(streamUrl || audioUrl || url);
        copyBtn.querySelector("span").textContent = "Copied to Clipboard!";
        setTimeout(() => {
          copyBtn.querySelector("span").textContent = "Copy Direct Link";
        }, 2000);
      };
      downloadButtonsGrid.appendChild(copyBtn);

      // Scroll smoothly to result
      resultCard.scrollIntoView({ behavior: "smooth", block: "nearest" });

    } catch (err) {
      statusBox.classList.add("hidden");
      errorBox.classList.remove("hidden");
      errorMessage.textContent = err.message || "An unexpected error occurred while resolving this link. Please retry or try another link.";
    } finally {
      submitBtn.disabled = false;
    }
  });
}

function sanitizeFilename(name) {
  return name.replace(/[^a-zA-Z0-9_\-]/g, "_").substring(0, 50);
}

// Live Statistics Polling
async function pollLiveStats() {
  try {
    const res = await fetch("/api/stats");
    if (res.ok) {
      const data = await res.json();
      if (data.botStatus) {
        const pill = document.getElementById("tgBotStatusPill");
        if (pill) pill.textContent = data.botStatus;
      }
      if (data.totalUsers !== undefined) {
        const usersCount = document.getElementById("tgBotUsersCount");
        if (usersCount) usersCount.textContent = `${data.totalUsers} Active Users`;
      }
    }
  } catch (_) {}
}
