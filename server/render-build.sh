#!/usr/bin/env bash
# Exit on error
set -o errexit

STORAGE_DIR=/opt/render/project/.render

echo "📦 Cleaning up and creating fresh Chrome storage directory..."
rm -rf "$STORAGE_DIR/chrome"
mkdir -p "$STORAGE_DIR/chrome"

echo "📥 Installing npm packages..."
npm install

echo "🌐 Installing Chrome binary cleanly..."
npx @puppeteer/browsers install chrome@126.0.6478.182 --path "$STORAGE_DIR/chrome"

echo "✅ Build completed successfully!"
