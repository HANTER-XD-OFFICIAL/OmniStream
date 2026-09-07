#!/usr/bin/env bash
# Exit on error
set -o errexit

echo "🚀 Building OmniStream Telegram Downloader Bot..."

echo "📥 Installing npm packages..."
npm install --omit=dev

echo "🎬 Ensuring yt-dlp binary is installed..."
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [ ! -f "$SCRIPT_DIR/yt-dlp" ]; then
  curl -L --max-time 30 https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -o "$SCRIPT_DIR/yt-dlp"
fi
chmod +x "$SCRIPT_DIR/yt-dlp"
echo "✅ yt-dlp is ready!"

echo "✅ Build completed successfully!"
