#!/usr/bin/env bash
# Exit on error
set -o errexit

echo "🚀 Building OmniStream Telegram Downloader Bot..."

echo "📥 Installing npm packages..."
npm install --omit=dev

echo "✅ Build completed successfully!"
