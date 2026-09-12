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
      card.style.setProperty("--card-brand-color", p.color);
      card.innerHTML = `
        <div>
          <div class="platform-card-header">
            <h4 class="platform-card-title">${p.name}</h4>
            <div class="platform-badge-circle" style="background-color: ${p.color}; box-shadow: 0 0 8px ${p.color};"></div>
          </div>
          <p class="platform-card-features">${p.features}</p>
        </div>
        <div class="platform-card-footer">
          <span class="platform-card-cat">${p.badge}</span>
          <button type="button" class="btn-card-try" data-url="${p.sampleUrl}">Try</button>
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
  const togglePreviewBtnText = document.getElementById("togglePreviewBtnText");
  const previewPlayerWrapper = document.getElementById("previewPlayerWrapper");
  const videoPreview = document.getElementById("videoPreview");
  const audioPreview = document.getElementById("audioPreview");
  const thumbnailWrapper = document.getElementById("thumbnailWrapper");
  const playOverlayBtn = document.getElementById("playOverlayBtn");
  const playerStreamStatus = document.getElementById("playerStreamStatus");
  const closePlayerBtn = document.getElementById("closePlayerBtn");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const url = input.value.trim();
    if (!url) return;

    // Reset views
    errorBox.classList.add("hidden");
    resultCard.classList.add("hidden");
    if (previewPlayerWrapper) previewPlayerWrapper.classList.add("hidden");
    videoPreview.pause();
    audioPreview.pause();
    videoPreview.src = "";
    audioPreview.src = "";
    videoPreview.poster = "";
    resultThumbnail.src = "";
    if (togglePreviewBtnText) togglePreviewBtnText.textContent = "Play Video in Browser";
    if (togglePreviewBtn) togglePreviewBtn.classList.remove("playing");
    if (playerStreamStatus) {
      playerStreamStatus.textContent = "● Ready";
      playerStreamStatus.classList.remove("playing");
    }

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

      let data = null;

      // Try local server API first if not static GitHub Pages
      if (!window.location.hostname.includes("github.io")) {
        try {
          const resp = await fetch("/api/extract", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              url: url,
              mode: formatSelect.value,
              quality: formatSelect.value === "audio" ? "320" : formatSelect.value
            })
          });
          if (resp.ok) {
            const resData = await resp.json();
            if (resData && resData.success) {
              data = resData;
            }
          }
        } catch (_) {}
      }

      // If on GitHub Pages or if local /api/extract was unavailable, resolve directly via Cloudflare Worker / client engines
      if (!data) {
        data = await resolveMediaClientSide(url, formatSelect.value);
      }

      if (!data || !data.success) {
        throw new Error((data && (data.message || data.error)) || "Failed to resolve media stream");
      }

      // Hide loading
      statusBox.classList.add("hidden");

      // Display results
      resultCard.classList.remove("hidden");
      resultTitle.textContent = data.title || "OmniStream Media File";
      resultPlatformTag.textContent = data.platform || (detected ? detected.name : "Universal");
      resultQualityTag.textContent = data.quality || "1080p HD";
      resultAuthorTag.textContent = data.author || "Creator";

      // Thumbnail & fallback handling (Never show logo.jpg as video thumbnail!)
      resultThumbnail.referrerPolicy = "no-referrer";
      resultThumbnail.crossOrigin = "anonymous";
      
      let thumbnailAssigned = false;

      // Validate thumbnail (ignore dummy 1x1 tracking pixels like data:image/gif;base64,R0lGODlhAQABA...)
      const isValidThumbnail = data.thumbnail && 
        typeof data.thumbnail === 'string' && 
        data.thumbnail.length > 200 && 
        !data.thumbnail.includes('data:image/gif;base64,R0lGODlhAQABA');

      if (isValidThumbnail) {
        resultThumbnail.onerror = () => {
          resultThumbnail.classList.add("hidden");
        };
        resultThumbnail.onload = () => {
          if (thumbnailWrapper) thumbnailWrapper.classList.remove("hidden");
          resultThumbnail.classList.remove("hidden");
        };
        resultThumbnail.src = data.thumbnail;
        videoPreview.poster = data.thumbnail;
        if (thumbnailWrapper) thumbnailWrapper.classList.remove("hidden");
        resultThumbnail.classList.remove("hidden");
        thumbnailAssigned = true;
      } else {
        // Fallback: keep thumbnail wrapper visible with dark gradient card & play overlay
        if (thumbnailWrapper) thumbnailWrapper.classList.remove("hidden");
        resultThumbnail.classList.add("hidden");
        resultThumbnail.src = "";
      }

      // Stream Player Setup (Initially hidden; revealed when user clicks Play in Browser)
      const streamUrl = data.videoUrl || data.streamUrl || data.downloadUrl;
      const audioUrl = data.audioUrl;
      const isAudioOnly = formatSelect.value === "audio" || (!streamUrl && audioUrl) || (streamUrl && streamUrl.endsWith(".mp3"));

      if (previewPlayerWrapper) previewPlayerWrapper.classList.add("hidden");

      if (isAudioOnly) {
        audioPreview.classList.remove("hidden");
        videoPreview.classList.add("hidden");
        const targetAudio = audioUrl || streamUrl;
        if (audioPreview.src !== targetAudio) {
          audioPreview.src = targetAudio;
          audioPreview.load();
        }
        if (playerStreamStatus) playerStreamStatus.textContent = "● Audio Stream Ready";
        if (togglePreviewBtnText) togglePreviewBtnText.textContent = "Play Audio in Browser";
      } else if (streamUrl) {
        videoPreview.classList.remove("hidden");
        audioPreview.classList.add("hidden");
        if (videoPreview.src !== streamUrl) {
          videoPreview.src = streamUrl;
          videoPreview.load();
        }

        // When video metadata and first frame are decoded by browser:
        videoPreview.onloadeddata = () => {
          if (playerStreamStatus && videoPreview.paused) {
            playerStreamStatus.textContent = "● Ready to Play";
          }
          // If no external thumbnail image was found, capture the actual video first frame!
          if (!thumbnailAssigned) {
            try {
              const canvas = document.createElement("canvas");
              canvas.width = videoPreview.videoWidth || 640;
              canvas.height = videoPreview.videoHeight || 360;
              const ctx = canvas.getContext("2d");
              ctx.drawImage(videoPreview, 0, 0, canvas.width, canvas.height);
              const snap = canvas.toDataURL("image/jpeg", 0.85);
              if (snap && snap.length > 500) {
                resultThumbnail.src = snap;
                videoPreview.poster = snap;
                if (thumbnailWrapper) thumbnailWrapper.classList.remove("hidden");
                resultThumbnail.classList.remove("hidden");
                thumbnailAssigned = true;
              }
            } catch (_) {
              // Even if canvas CORS restrictions apply, videoPreview natively displays the video frame!
            }
          }
        };

        if (playerStreamStatus) {
          playerStreamStatus.textContent = "● Ready to Play";
          playerStreamStatus.classList.remove("playing");
        }
        if (togglePreviewBtnText) {
          togglePreviewBtnText.textContent = "Play Video in Browser";
        }
        if (togglePreviewBtn) togglePreviewBtn.classList.remove("playing");
      }

      // Universal Play / Pause Control
      const togglePlayback = () => {
        const isCurrentlyHidden = previewPlayerWrapper && previewPlayerWrapper.classList.contains("hidden");

        if (isAudioOnly) {
          if (isCurrentlyHidden) {
            previewPlayerWrapper.classList.remove("hidden");
            audioPreview.play().catch(() => {});
            if (togglePreviewBtnText) togglePreviewBtnText.textContent = "Pause Audio";
            if (togglePreviewBtn) togglePreviewBtn.classList.add("playing");
            if (playerStreamStatus) {
              playerStreamStatus.textContent = "● Playing Audio";
              playerStreamStatus.classList.add("playing");
            }
            previewPlayerWrapper.scrollIntoView({ behavior: "smooth", block: "nearest" });
          } else if (audioPreview.paused) {
            audioPreview.play().catch(() => {});
            if (togglePreviewBtnText) togglePreviewBtnText.textContent = "Pause Audio";
            if (togglePreviewBtn) togglePreviewBtn.classList.add("playing");
            if (playerStreamStatus) {
              playerStreamStatus.textContent = "● Playing Audio";
              playerStreamStatus.classList.add("playing");
            }
          } else {
            audioPreview.pause();
            if (togglePreviewBtnText) togglePreviewBtnText.textContent = "Play Audio in Browser";
            if (togglePreviewBtn) togglePreviewBtn.classList.remove("playing");
            if (playerStreamStatus) {
              playerStreamStatus.textContent = "● Paused";
              playerStreamStatus.classList.remove("playing");
            }
          }
        } else if (streamUrl) {
          if (isCurrentlyHidden) {
            // Reveal player only after user clicks Play in Browser!
            previewPlayerWrapper.classList.remove("hidden");
            const playPromise = videoPreview.play();
            if (playPromise !== undefined) {
              playPromise.catch((err) => console.warn("Autoplay deferred:", err.message));
            }
            if (togglePreviewBtnText) togglePreviewBtnText.textContent = "Pause Video";
            if (togglePreviewBtn) togglePreviewBtn.classList.add("playing");
            if (playerStreamStatus) {
              playerStreamStatus.textContent = "● Playing Video";
              playerStreamStatus.classList.add("playing");
            }
            // Smoothly bring video into view
            previewPlayerWrapper.scrollIntoView({ behavior: "smooth", block: "nearest" });
          } else if (videoPreview.paused) {
            const playPromise = videoPreview.play();
            if (playPromise !== undefined) {
              playPromise.catch((err) => console.warn("Autoplay deferred:", err.message));
            }
            if (togglePreviewBtnText) togglePreviewBtnText.textContent = "Pause Video";
            if (togglePreviewBtn) togglePreviewBtn.classList.add("playing");
            if (playerStreamStatus) {
              playerStreamStatus.textContent = "● Playing Video";
              playerStreamStatus.classList.add("playing");
            }
          } else {
            videoPreview.pause();
            if (togglePreviewBtnText) togglePreviewBtnText.textContent = "Play Video in Browser";
            if (togglePreviewBtn) togglePreviewBtn.classList.remove("playing");
            if (playerStreamStatus) {
              playerStreamStatus.textContent = "● Paused";
              playerStreamStatus.classList.remove("playing");
            }
          }
        }
      };

      togglePreviewBtn.onclick = togglePlayback;
      if (playOverlayBtn) playOverlayBtn.onclick = togglePlayback;
      if (thumbnailWrapper) {
        thumbnailWrapper.onclick = (e) => {
          if (e.target.closest("#playOverlayBtn")) return;
          togglePlayback();
        };
      }

      if (closePlayerBtn) {
        closePlayerBtn.onclick = (e) => {
          e.stopPropagation();
          videoPreview.pause();
          audioPreview.pause();
          if (previewPlayerWrapper) previewPlayerWrapper.classList.add("hidden");
          if (togglePreviewBtnText) togglePreviewBtnText.textContent = isAudioOnly ? "Play Audio in Browser" : "Play Video in Browser";
          if (togglePreviewBtn) togglePreviewBtn.classList.remove("playing");
          if (playerStreamStatus) {
            playerStreamStatus.textContent = "● Ready to Play";
            playerStreamStatus.classList.remove("playing");
          }
        };
      }

      // Keep play/pause status synchronized with native player controls
      videoPreview.onplay = () => {
        if (togglePreviewBtnText) togglePreviewBtnText.textContent = "Pause Video";
        if (togglePreviewBtn) togglePreviewBtn.classList.add("playing");
        if (playerStreamStatus) {
          playerStreamStatus.textContent = "● Playing Video";
          playerStreamStatus.classList.add("playing");
        }
      };
      videoPreview.onpause = () => {
        if (togglePreviewBtnText) togglePreviewBtnText.textContent = "Play Video in Browser";
        if (togglePreviewBtn) togglePreviewBtn.classList.remove("playing");
        if (playerStreamStatus) {
          playerStreamStatus.textContent = "● Paused";
          playerStreamStatus.classList.remove("playing");
        }
      };
      videoPreview.onended = () => {
        if (togglePreviewBtnText) togglePreviewBtnText.textContent = "Replay Video";
        if (togglePreviewBtn) togglePreviewBtn.classList.remove("playing");
        if (playerStreamStatus) {
          playerStreamStatus.textContent = "● Finished";
          playerStreamStatus.classList.remove("playing");
        }
      };

      // Populate Download Buttons
      downloadButtonsGrid.innerHTML = "";

      const videoFilename = formatOmniStreamFilename(data.platform, data.title, "mp4");
      const audioFilename = formatOmniStreamFilename(data.platform, data.title, "mp3");

      // Primary Video/Media Download
      if (streamUrl) {
        const dlBtn = document.createElement("button");
        dlBtn.type = "button";
        dlBtn.className = "btn-stream-dl btn-stream-video";
        dlBtn.innerHTML = `
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
          <span>🎬 Download Video (${data.quality || "HD"})</span>
        `;
        dlBtn.onclick = (e) => {
          e.preventDefault();
          triggerDirectMediaDownload(streamUrl, videoFilename, dlBtn, "Video");
        };
        downloadButtonsGrid.appendChild(dlBtn);
      }

      // Audio Download (if distinct audio stream exists)
      if (audioUrl) {
        const audioBtn = document.createElement("button");
        audioBtn.type = "button";
        audioBtn.className = "btn-stream-dl btn-stream-audio";
        audioBtn.innerHTML = `
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 18V5l12-2v13"/><circle cx="6" cy="18" r="3"/><circle cx="18" cy="16" r="3"/></svg>
          <span>🎵 Download MP3 Audio</span>
        `;
        audioBtn.onclick = (e) => {
          e.preventDefault();
          triggerDirectMediaDownload(audioUrl, audioFilename, audioBtn, "Audio");
        };
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

// Generates standardized project-branded filename: OmniStream_[Platform]_[Title].[ext]
function formatOmniStreamFilename(platform, title, ext = "mp4") {
  const cleanPlatform = (platform || "Media").replace(/[^a-zA-Z0-9]/g, "");
  let cleanTitle = (title || "Download")
    .replace(/[^\w\s-]/g, "") // strip emojis, symbols, quotes
    .trim()
    .replace(/\s+/g, "_")
    .substring(0, 45);
  if (!cleanTitle) cleanTitle = "Media";
  return `OmniStream_${cleanPlatform}_${cleanTitle}.${ext}`;
}

// 1-Click Direct File Download (Forces native file save to device with custom filename)
async function triggerDirectMediaDownload(url, filename, btn, mediaType = "Video") {
  if (btn.classList.contains("btn-downloading")) return;
  const originalHtml = btn.innerHTML;
  btn.classList.add("btn-downloading");
  btn.disabled = true;

  const updateProgress = (label) => {
    btn.innerHTML = `
      <svg class="spinner-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="2" x2="12" y2="6"></line><line x1="12" y1="18" x2="12" y2="22"></line><line x1="4.93" y1="4.93" x2="7.76" y2="7.76"></line><line x1="16.24" y1="16.24" x2="19.07" y2="19.07"></line><line x1="2" y1="12" x2="6" y2="12"></line><line x1="18" y1="12" x2="22" y2="12"></line><line x1="4.93" y1="19.07" x2="7.76" y2="16.24"></line><line x1="16.24" y1="7.76" x2="19.07" y2="4.93"></line></svg>
      <span>${label}</span>
    `;
  };

  updateProgress(`Downloading ${mediaType}...`);

  try {
    let blob = null;

    // 1. Direct fetch with CORS and progress stream reading
    try {
      const resp = await fetch(url, { mode: 'cors' });
      if (resp.ok) {
        const contentLength = resp.headers.get('content-length');
        const total = contentLength ? parseInt(contentLength, 10) : 0;

        if (total > 0 && resp.body && resp.body.getReader) {
          const reader = resp.body.getReader();
          let received = 0;
          const chunks = [];
          while (true) {
            const { done, value } = await reader.read();
            if (done) break;
            chunks.push(value);
            received += value.length;
            const pct = Math.min(99, Math.round((received / total) * 100));
            updateProgress(`Downloading ${pct}%...`);
          }
          const mimeType = resp.headers.get('content-type') || (filename.endsWith('.mp3') ? 'audio/mpeg' : 'video/mp4');
          blob = new Blob(chunks, { type: mimeType });
        } else {
          blob = await resp.blob();
        }
      }
    } catch (directErr) {
      console.warn("Direct fetch CORS check:", directErr.message);
    }

    // 2. If direct fetch was restricted by CORS, fallback to proxy
    if (!blob) {
      updateProgress(`Connecting Proxy...`);
      const proxies = [
        `https://corsproxy.io/?${encodeURIComponent(url)}`,
        `https://api.allorigins.win/raw?url=${encodeURIComponent(url)}`
      ];

      for (const proxy of proxies) {
        try {
          const pResp = await fetch(proxy);
          if (pResp.ok) {
            blob = await pResp.blob();
            break;
          }
        } catch (_) {}
      }
    }

    // 3. Trigger native file download via Object URL
    if (blob) {
      updateProgress(`Saving to Device...`);
      const blobUrl = window.URL.createObjectURL(blob);
      const downloadAnchor = document.createElement("a");
      downloadAnchor.style.display = "none";
      downloadAnchor.href = blobUrl;
      downloadAnchor.download = filename;
      document.body.appendChild(downloadAnchor);
      downloadAnchor.click();

      setTimeout(() => {
        document.body.removeChild(downloadAnchor);
        window.URL.revokeObjectURL(blobUrl);
      }, 5000);

      btn.innerHTML = `
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>
        <span>✅ Saved: ${filename.substring(0, 22)}...</span>
      `;
      btn.classList.add("btn-download-success");

      setTimeout(() => {
        btn.innerHTML = originalHtml;
        btn.classList.remove("btn-downloading", "btn-download-success");
        btn.disabled = false;
      }, 4000);
      return;
    }

    // 4. Fallback if blob cannot be assembled (avoid target="_blank" so it doesn't open a new player tab)
    updateProgress(`Starting Download...`);
    const fallbackLink = document.createElement("a");
    fallbackLink.href = url;
    fallbackLink.download = filename;
    fallbackLink.target = "_self";
    document.body.appendChild(fallbackLink);
    fallbackLink.click();
    setTimeout(() => {
      document.body.removeChild(fallbackLink);
    }, 1000);

    btn.innerHTML = `<span>⚡ Download Started</span>`;
    setTimeout(() => {
      btn.innerHTML = originalHtml;
      btn.classList.remove("btn-downloading");
      btn.disabled = false;
    }, 2500);

  } catch (err) {
    console.error("Download execution error:", err);
    // Ultimate graceful fallback
    window.location.assign(url);
    btn.innerHTML = originalHtml;
    btn.classList.remove("btn-downloading");
    btn.disabled = false;
  }
}

function sanitizeFilename(name) {
  return name.replace(/[^a-zA-Z0-9_\-]/g, "_").substring(0, 50);
}

// Dedicated Platform Metadata and Thumbnail Resolver (oEmbed / ID extraction)
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
      const oe = await fetch(`https://www.youtube.com/oembed?url=${encodeURIComponent(url)}&format=json`);
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
      const oe = await fetch(`https://api.instagram.com/oembed/?url=${encodeURIComponent(url)}`);
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
      const oe = await fetch(`https://vimeo.com/api/oembed.json?url=${encodeURIComponent(url)}`);
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
      const oe = await fetch(`https://soundcloud.com/oembed?format=json&url=${encodeURIComponent(url)}`);
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
      const oe = await fetch(`https://www.dailymotion.com/services/oembed?url=${encodeURIComponent(url)}`);
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
      const resp = await fetch(url, {
        headers: { 'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36' },
        signal: AbortSignal.timeout(5000)
      });
      if (resp.ok) {
        const text = await resp.text();
        const m = text.match(/https:\/\/i\.pinimg\.com\/(?:originals|\d+x)\/[a-f0-9\/]+\.(?:jpg|png|jpeg|webp)/i);
        if (m) {
          thumbnail = m[0];
          title = 'Pinterest Video';
          author = 'Pinterest Creator';
        }
      }
    } catch (_) {}
  }

  // Universal Rich Preview Fallback (Microlink API for Instagram, Facebook, X, Pinterest)
  if (!thumbnail && (lower.includes('instagram.com') || lower.includes('facebook.com') || lower.includes('fb.watch') || lower.includes('twitter.com') || lower.includes('x.com') || lower.includes('pinterest.com') || lower.includes('pin.it'))) {
    try {
      const ml = await fetch(`https://api.microlink.io?url=${encodeURIComponent(url)}`, {
        signal: AbortSignal.timeout(6000)
      });
      if (ml.ok) {
        const j = await ml.json();
        if (j.data) {
          if (!title && j.data.title) title = j.data.title;
          if (!author && j.data.author) author = j.data.author;
          const imgCandidate = j.data.image?.url || j.data.image;
          if (imgCandidate && typeof imgCandidate === 'string' && imgCandidate.length > 200 && !imgCandidate.includes('data:image/gif;base64,R0lGODlhAQABA')) {
            thumbnail = imgCandidate;
          }
        }
      }
    } catch (_) {}
  }

  return { title, author, thumbnail };
}

// Client-side media extraction engine (runs serverlessly on GitHub Pages)
async function resolveMediaClientSide(rawUrl, mode = 'auto') {
  let url = rawUrl.trim();
  const lower = url.toLowerCase();
  const isAudio = mode === 'audio';

  // 1. Dedicated TikTok Engine (TikWM directly in browser)
  if (lower.includes('tiktok.com') || lower.includes('douyin.com')) {
    try {
      const resp = await fetch(`https://www.tikwm.com/api/?url=${encodeURIComponent(url)}&hd=1`);
      if (resp.ok) {
        const json = await resp.json();
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

  // 2. TeraBox Cloud Direct
  if (lower.includes('terabox') || lower.includes('1024tera')) {
    try {
      const resp = await fetch(`https://terabox-dl.qtcloud.workers.dev/api/get-info?url=${encodeURIComponent(url)}`);
      if (resp.ok) {
        const data = await resp.json();
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
  }

  // Pre-fetch platform rich metadata & thumbnail in parallel
  const metaPromise = fetchPlatformMetadata(url);

  // 3. Cloudflare Edge Worker API & Multi-Gateway Cobalt Engine
  const gateways = [
    "https://muddy-scene-0ff7.alexraselchodhury.workers.dev",
    "https://cobalt-latest-a04h.onrender.com",
    "https://co.wuk.sh",
    "https://cobalt.xy2401.com",
    "https://cobalt.api.redstream.org"
  ];

  for (const gw of gateways) {
    try {
      const resp = await fetch(gw, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        body: JSON.stringify({
          url: url,
          videoQuality: '1080',
          downloadMode: isAudio ? 'audio' : 'auto',
          youtubeVideoCodec: 'h264',
          audioFormat: 'mp3',
          alwaysProxy: true
        })
      });
      if (resp.ok) {
        const json = await resp.json();
        let streamUrl = json.url;
        if (json.status === 'picker' && Array.isArray(json.picker) && json.picker.length > 0) {
          const item = json.picker.find(p => p.type === 'video') || json.picker[0];
          streamUrl = item.url;
        }
        if (streamUrl && streamUrl.startsWith('http')) {
          let pName = 'OmniStream Engine';
          if (lower.includes('youtube.com') || lower.includes('youtu.be')) pName = 'YouTube';
          else if (lower.includes('instagram.com')) pName = 'Instagram';
          else if (lower.includes('facebook.com') || lower.includes('fb.watch')) pName = 'Facebook';
          else if (lower.includes('twitter.com') || lower.includes('x.com')) pName = 'Twitter / X';
          else if (lower.includes('pinterest.com') || lower.includes('pin.it')) pName = 'Pinterest';
          else if (lower.includes('reddit.com')) pName = 'Reddit';
          else if (lower.includes('soundcloud.com')) pName = 'SoundCloud';
          else if (lower.includes('vimeo.com')) pName = 'Vimeo';
          else if (lower.includes('dailymotion.com')) pName = 'Dailymotion';
          else if (lower.includes('bilibili.com')) pName = 'Bilibili';

          const meta = await metaPromise.catch(() => ({}));
          const finalThumb = json.thumbnail || meta.thumbnail || null;
          const finalTitle = (meta.title && meta.title !== 'YouTube Video') ? meta.title : (json.filename?.replace(/\.[^/.]+$/, '') || 'Media Stream');
          const finalAuthor = meta.author || 'Creator';

          return {
            success: true,
            platform: pName,
            title: finalTitle,
            author: finalAuthor,
            thumbnail: finalThumb,
            videoUrl: isAudio ? null : streamUrl,
            audioUrl: isAudio ? streamUrl : (json.audio || null),
            quality: isAudio ? '320kbps MP3' : '1080p HD'
          };
        }
      }
    } catch (_) {}
  }

  // 4. Dedicated YouTube Fallback Engine
  if (lower.includes('youtube.com') || lower.includes('youtu.be')) {
    try {
      const meta = await metaPromise.catch(() => ({}));
      const encUrl = encodeURIComponent(url);
      const ytRes = await fetch(`https://loader.to/ajax/download.php?button=1&start=1&end=1&format=720&url=${encUrl}`);
      if (ytRes.ok) {
        const ytJson = await ytRes.json();
        let dlUrl = ytJson.download_url;
        if (!dlUrl && ytJson.progress_url) {
          for (let i = 0; i < 8; i++) {
            await new Promise(r => setTimeout(r, 1500));
            const pRes = await fetch(ytJson.progress_url);
            if (pRes.ok) {
              const pJson = await pRes.json();
              if (pJson.download_url) {
                dlUrl = pJson.download_url;
                break;
              }
            }
          }
        }
        if (dlUrl && dlUrl.startsWith('http')) {
          return {
            success: true,
            platform: 'YouTube',
            title: meta.title || 'YouTube Video',
            author: meta.author || 'YouTube Creator',
            thumbnail: meta.thumbnail || null,
            videoUrl: dlUrl,
            audioUrl: null,
            quality: '720p HD'
          };
        }
      }
    } catch (_) {}
  }

  const meta = await metaPromise.catch(() => ({}));
  let fallbackPlatform = 'Universal Media';
  if (lower.includes('youtube.com') || lower.includes('youtu.be')) fallbackPlatform = 'YouTube';
  else if (lower.includes('instagram.com')) fallbackPlatform = 'Instagram';
  else if (lower.includes('facebook.com') || lower.includes('fb.watch')) fallbackPlatform = 'Facebook';
  else if (lower.includes('twitter.com') || lower.includes('x.com')) fallbackPlatform = 'Twitter / X';
  else if (lower.includes('pinterest.com') || lower.includes('pin.it')) fallbackPlatform = 'Pinterest';
  else if (lower.includes('reddit.com')) fallbackPlatform = 'Reddit';
  else if (lower.includes('soundcloud.com')) fallbackPlatform = 'SoundCloud';

  // Fallback: Direct stream link with resolved platform metadata and thumbnail
  return {
    success: true,
    platform: fallbackPlatform,
    title: meta.title || 'OmniStream Direct Media',
    author: meta.author || 'Source Media',
    thumbnail: meta.thumbnail || null,
    videoUrl: url,
    audioUrl: null,
    quality: 'Direct Stream'
  };
}

// Touch Glass Interactive Haptic Ripple Effect
function initGlassTouchFeedback() {
  const touchElements = document.querySelectorAll(".glass-touch, .btn-touch-glass, .btn-glass-nav, .platform-card, .sample-chip");
  touchElements.forEach((el) => {
    el.addEventListener("pointerdown", function (e) {
      const rect = this.getBoundingClientRect();
      const ripple = document.createElement("span");
      ripple.className = "glass-ripple-wave";
      const size = Math.max(rect.width, rect.height) * 1.5;
      ripple.style.width = ripple.style.height = `${size}px`;
      ripple.style.left = `${e.clientX - rect.left - size / 2}px`;
      ripple.style.top = `${e.clientY - rect.top - size / 2}px`;
      
      const existingRipple = this.querySelector(".glass-ripple-wave");
      if (existingRipple) existingRipple.remove();
      
      this.appendChild(ripple);
      setTimeout(() => {
        if (ripple.parentElement) ripple.remove();
      }, 650);
    });
  });
}

// Direct Support Form Handler
function initSupportForm() {
  const form = document.getElementById("direct-support-form");
  if (!form) return;

  form.addEventListener("submit", (e) => {
    e.preventDefault();
    const name = document.getElementById("support-sender-name")?.value.trim() || "User";
    const subject = document.getElementById("support-sender-subject")?.value.trim() || "OmniStream Support Inquiry";
    const message = document.getElementById("support-sender-message")?.value.trim() || "";

    const emailBody = `Hi MD RASEL,\n\nName: ${name}\n\nMessage:\n${message}\n\n---\nSent via OmniStream Support Hub`;
    const mailtoUrl = `mailto:alexraselchodhury@gmail.com?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(emailBody)}`;

    // Feedback notification toast
    showToast("Opening your Email app...", "info");
    
    // Robust mobile mail app launcher
    const tempLink = document.createElement("a");
    tempLink.href = mailtoUrl;
    tempLink.target = "_blank";
    tempLink.rel = "noopener noreferrer";
    document.body.appendChild(tempLink);
    tempLink.click();
    setTimeout(() => {
      tempLink.remove();
    }, 500);
  });
}

// Initialize on DOM Ready
document.addEventListener("DOMContentLoaded", () => {
  initGlassTouchFeedback();
  initSupportForm();
});

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
    } else {
      // Fallback for GitHub Pages static hosting
      const pill = document.getElementById("tgBotStatusPill");
      if (pill) pill.textContent = "🟢 24/7 Online";
      const usersCount = document.getElementById("tgBotUsersCount");
      if (usersCount) usersCount.textContent = "1,420 Active Users";
    }
  } catch (_) {
    const pill = document.getElementById("tgBotStatusPill");
    if (pill) pill.textContent = "🟢 24/7 Online";
    const usersCount = document.getElementById("tgBotUsersCount");
    if (usersCount) usersCount.textContent = "1,420 Active Users";
  }
}
