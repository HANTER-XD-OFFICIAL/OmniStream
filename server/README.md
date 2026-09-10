# OmniStream - 24/7 Official Telegram Video Downloader Bot
Official Telegram Downloader Bot for OmniStream.
**Bot Username:** [@OmniStream34_bot](https://t.me/OmniStream34_bot)  
**Developer:** MD Rasel ([@HANTER_XD_OFFICIAL](https://t.me/HANTER_XD_OFFICIAL))  
**GitHub Releases:** [HANTER-XD-OFFICIAL/OmniStream](https://github.com/HANTER-XD-OFFICIAL/OmniStream/releases)

---

## 🚀 Key Features

1. **Direct Official Android APK Delivery:**
   - Users can tap **📱 Download Official App** from the bottom menu or use `/app`.
   - The bot automatically connects to the official GitHub repository (`HANTER-XD-OFFICIAL/OmniStream/releases/latest`), fetches the latest release APK, and delivers it directly to Telegram so users can install it on their device with 1 tap.

2. **Automatic GitHub Release Monitor & Broadcast:**
   - The bot monitors GitHub releases every 5 minutes.
   - When a new version is published, it automatically sends an update notification and download prompt to all active users.
   - Admin can manually check and trigger broadcasts anytime via `/check_update` or the Admin Dashboard.

3. **1-to-1 Direct Messaging & Broadcast System:**
   - **Direct Message:** Admin can message any individual user directly using `/msg <userId> <message>` or via the interactive **Registered Users** list.
   - **Two-Way Support:** When users send messages or questions, the admin receives instant notification with a 1-click reply button.
   - **Mass Broadcast:** Broadcast announcements to all active users with `/broadcast <message>`.

4. **Developer Support In-Chat Button:**
   - Restored direct Developer Support button `[👨‍💻 Developer (@HANTER_XD_OFFICIAL)]` under the `/start` welcome greeting and help commands.

---

## 🌐 How to Keep the Bot Running 24/7 Free

### Method 1: Deploy on Render.com (Best 24/7 Free Hosting)
1. Sign up for a free account at [Render.com](https://render.com).
2. Click **New +** and select **Web Service** or **Background Worker**.
3. Connect your GitHub repository or upload this `server` directory.
4. **Build Command:** `echo "Ready"`
5. **Start Command:** `node bot.mjs`
6. **Environment Variable:** `BOT_TOKEN` = (Optional: securely injected via Render Secret, or runs from internal encrypted vault).
7. That's it! Your official bot will remain online 24/7/365 to process video downloads and deliver APK updates.

---

### Method 2: Run 24/7 via Termux on Android
1. Open the **Termux** app on your Android device.
2. Run the following commands sequentially:
```bash
pkg update -y && pkg install nodejs git -y
git clone <your-repo-link>
cd server
node bot.mjs
```
The bot will run continuously in the background!

---

## 🌟 Supported Platforms
- **TikTok** (Watermark-Free HD MP4 + Audio MP3)
- **Instagram** (Reels, Posts, Stories)
- **Facebook** (Reels, Videos, Public Watch)
- **YouTube** (Shorts, Videos)
- **TeraBox** (Direct Fast Download)
- **Twitter / X**
- **Official APK Direct Download & Auto-Update Notifications**
