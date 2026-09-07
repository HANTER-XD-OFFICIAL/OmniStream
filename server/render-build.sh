#!/usr/bin/env bash
# Exit on error
set -o errexit

STORAGE_DIR=/opt/render/project/src/server/chrome

echo "📦 Setting up Chrome cache directory in project..."
mkdir -p "$STORAGE_DIR"

echo "📥 Installing npm packages..."
npm install

echo "🌐 Installing Chrome binary cleanly via Puppeteer..."
# Let puppeteer install the exact browser version it needs into the project chrome folder
npx puppeteer browsers install chrome --path "$STORAGE_DIR"

echo "✅ Build completed successfully!"
