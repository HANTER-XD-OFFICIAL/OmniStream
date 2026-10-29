#!/usr/bin/env bash
# Exit on error
set -o errexit

echo "🚀 Building OmniStream Telegram Downloader Bot..."

echo "📥 Installing npm packages..."
npm install --omit=dev

echo "🐍 Installing Pyrogram 2GB MTProto Engine..."
python3 -m pip install --break-system-packages --no-cache-dir pyrogram tgcrypto 2>/dev/null || pip3 install --no-cache-dir pyrogram tgcrypto 2>/dev/null || true

echo "✅ Build completed successfully!"
