#!/usr/bin/env bash
# Exit on error
set -o errexit

STORAGE_DIR=/opt/render/project/.render

echo "📦 Setting up Chrome cache directory..."
mkdir -p "$STORAGE_DIR/chrome"

echo "📥 Installing npm packages..."
npm install

echo "🌐 Installing Chrome binary..."
npx @puppeteer/browsers install chrome@stable --path "$STORAGE_DIR/chrome"
