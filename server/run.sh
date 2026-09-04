#!/bin/bash
# DOWNLOAD ALL IN ONE Bot Auto-Restart Daemon Wrapper
echo "Starting 24/7 Bot Daemon Supervisor..."

while true; do
  echo "[$(date)] Launching bot.mjs..."
  node --max-old-space-size=1024 ./server/bot.mjs
  EXIT_CODE=$?
  echo "[$(date)] bot.mjs stopped with code $EXIT_CODE. Auto-respawning in 2s..."
  sleep 2
done
