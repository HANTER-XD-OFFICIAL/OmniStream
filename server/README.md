# OmniStream - 24/7 Official Telegram Video Downloader Bot
Official Telegram Downloader Bot for OmniStream.
**Bot Username:** [@OmniStream34_bot](https://t.me/OmniStream34_bot)  
**Developer:** MD Rasel ([@HANTER_XD_OFFICIAL](https://t.me/HANTER_XD_OFFICIAL))  
**GitHub Releases:** [HANTER-XD-OFFICIAL/OmniStream](https://github.com/HANTER-XD-OFFICIAL/OmniStream/releases)

---

## 🚀 Newly Added Features

1. **Direct Official Android APK Delivery:**
   - Users can tap **📱 Download Official App** from the bottom menu or use `/app`.
   - The bot automatically connects to the official GitHub repository (`HANTER-XD-OFFICIAL/OmniStream/releases/latest`), fetches the latest release APK, and delivers it directly to Telegram so users can install it on their device with 1 tap.

2. **Automatic GitHub Release Monitor & Broadcast:**
   - The bot monitors GitHub releases every 5 minutes.
   - When a new version is published, it automatically sends an update notification and download prompt to all active users.
   - Admin can manually check and trigger broadcasts anytime via `/check_update` or the Admin Dashboard.

3. **Developer Support In-Chat Button:**
   - Restored direct Developer Support button `[👨‍💻 Developer (@HANTER_XD_OFFICIAL)]` under the `/start` welcome greeting and help commands.

---

## 🇧🇩 অলটাইম ২৪/৭ ফ্রিতে রান রাখার উপায় (How to run 24/7 Free)

### পদ্ধতি ১: Render.com এ আজীবন ফ্রিতে রান রাখুন (Best 24/7 Free Hosting)
1. [Render.com](https://render.com) এ গিয়ে ফ্রি একাউন্ট খুলুন।
2. **New +** এ ক্লিক করে **Background Worker** বা **Web Service** সিলেক্ট করুন।
3. আপনার GitHub রিপোজিটরি কানেক্ট করুন অথবা এই `server` ফোল্ডার আপলোড করুন।
4. **Build Command:** `echo "Ready"`
5. **Start Command:** `node bot.mjs`
6. **Environment Variable:** `BOT_TOKEN` = (Optional: securely injected via Render Secret, or runs from internal encrypted vault)
7. ব্যস! আপনার অফিসিয়াল বট ২৪ ঘণ্টা ৩৬৫ দিন অনলাইনে থাকবে এবং সবাই ভিডিও ডাউনলোড করতে পারবে!

---

### পদ্ধতি ২: Android মোবাইলের Termux দিয়ে অলটাইম রানিং রাখুন
১. মোবাইল থেকে **Termux** অ্যাপ ওপেন করুন।  
২. নিচের কমান্ডগুলো পরপর রান করুন:
```bash
pkg update -y && pkg install nodejs git -y
git clone <your-repo-link>
cd server
node bot.mjs
```
বট অলটাইম ব্যাকগ্রাউন্ডে চলতে থাকবে!

---

## 🌟 Supported Platforms
- **TikTok** (Watermark-Free HD MP4 + Audio MP3)
- **Instagram** (Reels, Posts, Stories)
- **Facebook** (Reels, Videos, Public Watch)
- **YouTube** (Shorts, Videos)
- **TeraBox** (Direct Fast Download)
- **Twitter / X**
- **Official APK Direct Download & Auto-Update Notifications**
