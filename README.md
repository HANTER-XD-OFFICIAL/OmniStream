# ⚡ OmniStream — Ultimate Universal Video & Audio Downloader

<div align="center">

![OmniStream Banner](https://img.shields.io/badge/OmniStream-v3.0.0--Beta-6366f1?style=for-the-badge&logo=android&logoColor=white)
[![Platform](https://img.shields.io/badge/Platform-Android_8.0+-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin_2.0+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose_M3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM_%2B_Clean-blueviolet?style=for-the-badge)](https://developer.android.com/topic/architecture)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)

<br/>

> **OmniStream** is a high-performance, enterprise-grade Android media extraction and download suite. Built natively with **Kotlin** and **Jetpack Compose (Material Design 3)**, it powers ultra-fast, watermark-free media downloads across **YouTube, Facebook, Instagram, TikTok, Twitter/X, Pinterest, TeraBox, Reddit**, and 1000+ streaming sites using a multi-tiered Cloudflare Edge API and intelligent failover architecture.

</div>

<br/>

### 🚀 Quick Launch & Direct Downloads

Select your preferred platform below to start extracting and downloading media immediately:

> [!TIP]
> ### 🌐 [Launch Web Downloader (In-Browser)](https://hanter-xd-official.github.io/OmniStream/)
> **Zero Installation Required • Works on All Devices**  
> Direct media extraction engine for Android, iOS, Windows, macOS, and Linux. Download 4K / 1080p Full HD video streams and 320kbps MP3 audio directly in Chrome, Safari, or Edge.  
>  
> [![Launch Web Downloader](https://img.shields.io/badge/🌐_LAUNCH_WEB_APP-Live_in_Browser_↗-6366f1?style=for-the-badge&logo=googlechrome&logoColor=white&labelColor=0d1117)](https://hanter-xd-official.github.io/OmniStream/)

> [!NOTE]
> ### 🤖 [Telegram Downloader Bot (@OmniStream34_bot)](https://t.me/OmniStream34_bot)
> **Direct Chat Downloads • Fast, Lightweight & Always Online**  
> Simply paste any video link to `@OmniStream34_bot` on Telegram to receive original videos and audio files sent right into your chat with high-speed delivery.  
>  
> [![Open Telegram Bot](https://img.shields.io/badge/🤖_OPEN_TELEGRAM_BOT-@OmniStream34__bot_↗-0088cc?style=for-the-badge&logo=telegram&logoColor=white&labelColor=0d1117)](https://t.me/OmniStream34_bot)

> [!IMPORTANT]
> ### 📱 [Native Android Application (Pro APK v3.0.0-Beta)](https://github.com/HANTER-XD-OFFICIAL/OmniStream/releases/latest)
> **Hardware-Accelerated Mobile Client • Built with Jetpack Compose & M3**  
> Dedicated Android client featuring background download notifications, an AES-256 encrypted offline media vault, status bar playback controls, and an integrated media player.  
>  
> [![Download Android APK](https://img.shields.io/badge/⬇️_DOWNLOAD_ANDROID_APK-Direct_Release_v3.0.0--Beta_↗-00c853?style=for-the-badge&logo=android&logoColor=white&labelColor=0d1117)](https://github.com/HANTER-XD-OFFICIAL/OmniStream/releases/latest)

<br/>

---

## 🌟 Highlights & Key Capabilities

* 🚀 **Universal Platform Coverage**: Instant extraction from YouTube, Facebook, Instagram, TikTok (No Watermark), Twitter / X, Pinterest, TeraBox, Reddit, Vimeo, Dailymotion, and generic HLS/Direct MP4 links.
* 🎯 **Smart Quality Matrix System**: Automatically scans and displays all available stream tiers (4K 2160p, 2K 1440p, 1080p 60fps, 720p HD, 480p, 360p, and MP3 Audio 320kbps). Marks 100% verified streams as `AVAILABLE` / `RECOMMENDED` and grey-out unavailable options.
* 🛡️ **Multi-Tier Edge Failover Engine**:
  * **Tier 1 (Primary)**: Custom Cloudflare Edge Worker API (`omnistream-api.alexraselchodhury.workers.dev`) providing sub-second latency and zero rate-limit media resolution.
  * **Tier 2 (Cobalt & yt-dlp)**: High-availability VIP extraction nodes for full audio/video muxing.
  * **Tier 3 (Dedicated Stream Decoupler)**: Asynchronous polling engines (`Loader.to`, `SaveNow`) for ultra-high bitrate MP4 and 320kbps MP3 streams.
  * **Tier 4 (Rotating Proxy Scrapers)**: Multi-key RapidAPI and Piped/Invidious proxy fallbacks ensuring 99.9% download success rate.
* 💾 **Android 14 Scoped Storage & MediaStore**: Files are directly saved to public storage (`Download/OmniStream`, `Movies/OmniStream`, `Music/OmniStream`), immediately accessible by system gallery apps and music players without root or storage permission hassles.
* ⚡ **High-Speed Foreground Download Manager**: Native Android foreground service with live notification progress, speed indicators (MB/s), ETA calculations, and pause/resume/cancel controls.
* 🎨 **True-Black Liquid Glass UI (`LiquidGlassKit`)**: Engineered with Jetpack Compose & Material 3 on a pure `#000000` OLED dark backdrop with high-blur frosted translucent surfaces, multi-color cyan/purple specular borders, floating morphing pill navigation dock, and high-contrast white typography.
* 📏 **Real Server File Size Engine (`KB / MB / GB`)**: Probes live `HTTP HEAD` (`Content-Length`) and `Range: bytes=0-0` (`Content-Range`) headers plus native Piped/TikWM/yt-dlp/MEGA metadata to show exact file sizes without fake approximations.
* 🗄️ **Offline Room Database**: Complete local persistence storing download history, media metadata, file paths, thumbnails, and search history.
* 🎬 **Integrated Media Player**: Instant in-app playback with Media3 / ExoPlayer, fullscreen toggle, share sheet, and direct file management.
* 🔄 **GitHub OTA Auto-Updater & Telegram Sync**: Automatic `v3.0.0-Beta` release tracking with in-app APK installer and optional `@OmniStream34_bot` Telegram delivery.

---

## ✨ Release Changelog & What's New (`v3.0.0-Beta`)

### 🔥 What's New & Changed in `v3.0.0-Beta` (Latest — Build 3)
* **Liquid Glass Kit UI Overhaul (True Black OLED Edition)**:
  * Enforced a pure `#000000` dark canvas across the entire app, replacing flat dark-grey containers with deep frosted translucent glass panels, subtle radial caustics, and crisp cyan/white/purple specular rims.
  * Upgraded typography hierarchy and high-contrast solid white text across all screens, welcome onboarding, storage permission modals, and developer cards.
* **Floating Morphing Pill Dock & Clean Download Badge**:
  * Redesigned the bottom navigation bar into a floating animated Liquid Glass pill dock with spring physics and glowing droplet indicators.
  * Fixed the active download counter badge on the `Downloads` tab with a compact cutout-ring gradient pill so icons and labels remain perfectly aligned.
* **Signature Hero CTA & Quality Matrix Alignment**:
  * Styled the `ANALYZE & EXTRACT QUALITIES` button as a vibrant cyan-to-purple capsule pill with crisp high-contrast dark typography.
  * Refined **Dynamic Quality Matrix** & **Source Stream Intelligence**: clean horizontal `AUTHENTIC` status pill, aligned **Max Resolution** and **Video Aspect Ratio** telemetry cards, and accurate audio-only extraction buttons (`FLAC` / `MP3 320 kbps` / `AAC 192 kbps`).
* **Real File Size Verification Engine (Zero Fake Sizes)**:
  * Removed all hardcoded/synthetic file size placeholders across format extraction and quality tiers.
  * Added parallel coroutine probing (`HEAD` `Content-Length` + fallback `Range` `Content-Range`) and live `Content-Range` tracking inside `DownloadRepository`, verifying exact on-disk bytes upon completion and formatting accurately in `B / KB / MB / GB`.
* **Ecosystem Version Synchronization (`v3.0.0-Beta`)**:
  * Synchronized `versionName = "3.0.0-Beta"` (`versionCode = 3`) across Android Gradle, `BuildConfig`, API & Engine, Developer Hub, Welcome Dialog, Cloudflare Worker, Telegram Bot (`OmniStream_v3.0.0-Beta.apk`), GitHub Release workflow, and companion web portals.

### 🧱 Foundation From Earlier Releases (`v2.0.0-beta` — Build 2)
* **21+ Platform Universal Extractor**: YouTube, Shorts, TikTok (No Watermark), Instagram Reels/Stories, Facebook, Twitter/X, Pinterest, TeraBox, MEGA, Reddit, SoundCloud, Vimeo, Dailymotion, and direct streams.
* **Multi-Tier Edge Failover Pipeline**: Cloudflare Edge Worker primary API with Cobalt VIP, yt-dlp, Loader/SaveNow decoupler, and rotating Piped/TikWM fallbacks.
* **Resumable Background Download Service**: Android 14/15 `FOREGROUND_SERVICE_DATA_SYNC` worker with `PARTIAL_WAKE_LOCK`, `WIFI_MODE_FULL_HIGH_PERF`, `HTTP 206` byte-range resume, and `MediaScannerConnection` gallery indexing.
* **Built-in Media3 Playback & Developer Support Hub**: In-app video/audio preview modal plus direct Telegram, WhatsApp, Facebook, and Email developer support links.

---

## 📱 Supported Platforms & Extraction Engines

| Platform | Supported Formats | Max Resolution | Watermark Free? | Extraction Method |
| :--- | :--- | :--- | :---: | :--- |
| **YouTube** | MP4, WEBM, MP3, M4A | **4K 60fps / 1080p** | ✅ Yes | oEmbed + Edge Worker + Loader Engine |
| **YouTube Shorts** | MP4, MP3 | **1080p FHD** | ✅ Yes | Direct High-Res Stream Resolution |
| **TikTok** | MP4, MP3 Audio | **1080p HD** | ✅ **No Watermark** | TikWM / Edge Worker Decoupler |
| **Facebook** | MP4, HD/SD | **1080p HD** | ✅ Yes | GraphQL & Direct Stream Parser |
| **Instagram** | MP4 Reels, Stories, Carousel | **1080p** | ✅ Yes | Custom CDN Parser + Audio Mux |
| **Twitter / X** | Multi-bitrate MP4 | **1080p** | ✅ Yes | Syndication API + Video Stream Selector |
| **TeraBox** | MP4, MKV, Any Document | **Source Quality** | ✅ Bypass App | Direct Token Cloud Resolution |
| **Pinterest** | MP4 Videos, GIF, HD Images | **1080p / Original** | ✅ Yes | Pin Metadata JSON Extractor |
| **Reddit** | MP4 with merged sound | **1080p** | ✅ Yes | Native Video + Audio DASH Muxing |
| **Generic URLs** | MP4, M3U8, WebM, MP3 | **Source Quality** | ✅ Yes | Direct OkHttp Chunked Stream Engine |

---

## 📐 Architecture & Technology Stack

OmniStream is engineered using **Clean Architecture** principles and the **MVVM (Model-View-ViewModel)** design pattern for maximum scalability, maintainability, and testability.

```
┌──────────────────────────────────────────────────────────┐
│                   Jetpack Compose UI                     │
│  (HomeScreen, HistoryScreen, SettingsScreen, Components) │
└────────────────────────────┬─────────────────────────────┘
                             │ StateFlow & UI Events
┌────────────────────────────▼─────────────────────────────┐
│                    DownloadViewModel                     │
│          (Manages UI State, Search & Matrix)             │
└──────────────┬────────────────────────────┬──────────────┘
               │                            │
┌──────────────▼─────────────┐ ┌────────────▼──────────────┐
│     DownloadRepository     │ │      VideoInfoRepository   │
│ (Orchestrates Downloads)   │ │ (Resolves Stream Formats) │
└──────────────┬─────────────┘ └────────────┬──────────────┘
               │                            │
┌──────────────▼─────────────┐ ┌────────────▼──────────────┐
│  DownloadService & Room    │ │   YtDlpClient Multi-Tier  │
│  (Foreground Worker + DB)  │ │ (Cloudflare / Cobalt / Yt)│
└────────────────────────────┘ └───────────────────────────┘
```

### Core Technologies

* **Language**: [Kotlin 2.0+](https://kotlinlang.org/) — 100% Modern, Type-safe Kotlin.
* **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with [Material 3 (M3)](https://m3.material.io/).
* **Reactive State**: Kotlin Coroutines, `StateFlow`, `SharedFlow`, `collectAsStateWithLifecycle`.
* **Local Persistence**: [Android Jetpack Room 2.6+](https://developer.android.com/training/data-storage/room) via KSP (Kotlin Symbol Processing).
* **Networking**: [OkHttp 4.12+](https://square.github.io/okhttp/) with custom interceptors, connection pooling, and SSL tuning.
* **Image Loading**: [Coil Compose](https://coil-kt.github.io/coil/compose/) for instant caching and thumbnail rendering.
* **Media & Playback**: Media3 / ExoPlayer for smooth video and audio rendering.
* **Storage**: Android Scoped Storage + MediaStore API compliant with Android 8.0 through Android 15 (API 35/36).

---

## 💎 Features Deep Dive

### 1. Smart Quality Matrix (Resolution Discovery)
When a media link is entered, OmniStream contacts the extraction pipeline and constructs a **Quality Matrix**:
* **Real-time Stream Verification**: Checks which audio/video codecs and resolutions are actually available for that specific URL.
* **Tiered Format Badges**:
  * `4K 2160p` / `2K 1440p`: Ultra High Definition
  * `1080p FHD`: Full HD 60fps recommended for high-end viewing
  * `720p HD`: Perfect balance between data usage and crisp quality
  * `480p / 360p`: Data Saver options
  * `MP3 320kbps`: Studio audio extract
* **Availability Guard**: Grayed-out lock states prevent users from triggering broken or non-existent download links.

### 2. Custom Cloudflare Worker Edge Integration
The application connects directly to a dedicated Cloudflare Worker:
```
GET /?url={MEDIA_URL}&format={FORMAT}&token={AUTH}
```
* **Bypasses IP restrictions and rate limits**: Running across hundreds of Cloudflare data centers globally.
* **Zero Client Footprint**: The heavy lifting of decrypting signatures, parsing media manifests, and selecting streams happens at the Edge.

### 3. Dedicated YouTube Stream Decoupler
* Employs YouTube Official oEmbed protocol to fetch genuine video titles, channel authors, and high-resolution thumbnail graphics.
* Asynchronously communicates with backend CDN nodes (`savenow.to` & `loader.to`) to extract pristine MP4 direct streams and 320kbps MP3 audio files without requiring local Python or heavy binaries on the user's phone.

### 4. Background Download Engine & Screen-Off Persistence
* **Android 14+ Foreground Service**: Utilizes `DownloadForegroundService` registered with `FOREGROUND_SERVICE_DATA_SYNC` type, ensuring Android OS never kills or freezes download processes when switching apps or locking the device.
* **Partial WakeLock & High-Performance WifiLock**: Holds power management locks (`PowerManager.PARTIAL_WAKE_LOCK` and `WifiManager.WIFI_MODE_FULL_HIGH_PERF`) during active transfers, preventing device CPU from entering deep sleep and keeping radio interfaces active with screen off.
* **HTTP 206 Resumable Streaming**: Implements RFC 7233 Range headers (`Range: bytes=X-`). If the network fluctuates or user resumes a paused task, downloads resume from the exact byte offset without restarting from scratch.
* **Notification Controls**: Ongoing notification displays real-time percentage, downloaded bytes / total bytes, current download speed, and interactive Cancel/Pause buttons.
* **Instant Media Indexing**: Once completed, the file is automatically registered with `MediaScannerConnection` so it appears instantly in Google Photos, VLC, MX Player, or Samsung Gallery.

---

## 🛠️ Project Structure

```
OmniStream/
├── app/
│   ├── build.gradle.kts           # Module dependencies & build configurations
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml # App permissions, services & activities
│       │   ├── java/com/example/
│       │   │   ├── data/
│       │   │   │   ├── api/       # YtDlpClient, YtDlpModels, AppUpdateManager, TelegramBotClient
│       │   │   │   ├── local/     # Room AppDatabase, DownloadDao, DownloadEntity
│       │   │   │   └── repository/# DownloadRepository, SettingsRepository
│       │   │   ├── service/       # DownloadForegroundService
│       │   │   ├── ui/
│       │   │   │   ├── components/# QualityMatrix, MediaPreviewDialog, StoragePermissionHelper, Modals
│       │   │   │   ├── screens/   # HomeScreen, DownloadsScreen, ApiSettingsScreen, DeveloperScreen
│       │   │   │   ├── theme/     # LiquidGlassKit, Color, Theme, Typography
│       │   │   │   └── DownloadViewModel.kt
│       │   │   └── MainActivity.kt
│       │   └── res/               # Adaptive launcher icons, vector drawables, strings
├── public/ & docs/                # Companion Web Downloader Landing Pages (v3.0.0-Beta)
├── server/                        # 24/7 Telegram Bot & Web API Server (bot.mjs, web_server.mjs)
├── cloudflare-worker.js           # Cloudflare Edge Worker Extraction API
├── gradle/                        # Version Catalog (libs.versions.toml)
├── metadata.json                  # AI Studio Application metadata
└── README.md                      # Complete Project Documentation & Changelog
```

---

## 🚀 Getting Started & Build Instructions

### Prerequisites
* **Android Studio**: Ladybug (2024.2.1+) or newer
* **JDK**: OpenJDK 17 or 21
* **Android SDK**: `compileSdk 36`, `minSdk 24`, `targetSdk 36`
* **Gradle**: 8.8+

### 1. Clone the Repository
```bash
git clone https://github.com/HANTER-XD-OFFICIAL/OmniStream.git
cd OmniStream
```

### 2. Build Debug APK via Gradle
To compile and assemble the debug APK directly from the command line:
```bash
gradle :app:assembleDebug
```
The compiled APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```
Or use the pre-built release artifact:
```
.build-outputs/app-debug.apk
```

### 3. Run on Device or Emulator
```bash
adb install -r .build-outputs/app-debug.apk
```

---

## ⚙️ Configuration & Environment Variables

OmniStream works out-of-the-box with default fallback engines. For custom or private deployments, configure your `.env` or Secrets panel:

| Variable | Description | Default |
| :--- | :--- | :--- |
| `DEFAULT_API_URL` | Primary Cloudflare Worker API URL | `https://omnistream-api.alexraselchodhury.workers.dev` |
| `API_AUTH_TOKEN` | Bearer Token for secured instances | *(Optional)* |
| `COBALT_API_URL` | Cobalt VIP Fallback Engine | `https://cobalt-api.kwiatekm.tokyo` |

---

## 🛡️ Security, Privacy & Google Play Compliance

* 🔒 **Zero Data Collection**: OmniStream does not track, collect, or store any personal information, search queries, or media history. Everything resides locally in your on-device Room SQLite database.
* 🛡️ **Zero Dangerous Storage Permissions**: Fully compliant with Android 14/15 storage policies using Scoped Storage and the standard Android Photo Picker / MediaStore APIs. No `READ_EXTERNAL_STORAGE` or `WRITE_EXTERNAL_STORAGE` required on Android 10+.
* ⚡ **Secure SSL/TLS**: All network calls enforce HTTPS with strict certificate checking.

---

## 👨‍💻 Developer & Author

* **Lead Architect & Developer**: MD RASEL (@HANTER_XD_OFFICIAL)
* **Email**: [hanterxdofficial@gmail.com](mailto:hanterxdofficial@gmail.com)
* **Project**: OmniStream Universal Media Suite

---

## 📄 License

This project is licensed under the [MIT License](LICENSE) — free for educational, personal, and development use.

<div align="center">

**Built with ❤️ using Kotlin, Jetpack Compose, and Cloudflare Workers**

</div>
