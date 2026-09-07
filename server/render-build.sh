#!/usr/bin/env bash
# Exit on error
set -o errexit

echo "📦 Installing npm dependencies..."
npm install

echo "🌐 Installing Chrome for Puppeteer..."
# Store in a clean local cache directory
npx @puppeteer/browsers install chrome@stable
