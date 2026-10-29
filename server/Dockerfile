FROM node:22-slim

RUN apt-get update && apt-get install -y \
    ca-certificates curl ffmpeg python3 python3-pip \
    --no-install-recommends \
    && pip3 install --break-system-packages --no-cache-dir pyrogram tgcrypto \
    && rm -rf /var/lib/apt/lists/*

ENV PORT=10000

WORKDIR /app
COPY package.json ./
RUN npm install --omit=dev
COPY . .

RUN chmod +x render-build.sh && ./render-build.sh

EXPOSE 10000
CMD ["node", "bot.mjs"]
