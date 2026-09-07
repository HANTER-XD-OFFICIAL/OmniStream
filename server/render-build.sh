#!/usr/bin/env bash
# Exit on error
set -o errexit

PROJECT_CHROME_DIR="/opt/render/project/src/server/chrome"

echo "📥 Installing npm packages..."
npm install

echo "🧹 Clearing old or incomplete browser cache..."
rm -rf "$PROJECT_CHROME_DIR" ~/.cache/puppeteer
mkdir -p "$PROJECT_CHROME_DIR"

echo "🌐 Installing Chrome binary cleanly via Puppeteer..."
npx puppeteer browsers install chrome --path "$PROJECT_CHROME_DIR" || true

# Find the installed chrome binary and ensure it's executable
CHROME_BIN=$(find "$PROJECT_CHROME_DIR" -type f -name "chrome" 2>/dev/null | head -n 1 || true)
if [ -n "$CHROME_BIN" ]; then
  chmod +x "$CHROME_BIN"
  echo "✅ Found Chrome executable at: $CHROME_BIN"
else
  echo "⚠️ Puppeteer binary not installed in $PROJECT_CHROME_DIR, will use @sparticuz/chromium fallback"
fi

echo "✅ Build completed successfully!"
