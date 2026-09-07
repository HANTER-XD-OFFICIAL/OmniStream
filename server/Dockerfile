FROM node:22-slim

RUN apt-get update && apt-get install -y \
    ca-certificates curl \
    --no-install-recommends \
    && rm -rf /var/lib/apt/lists/*

ENV PORT=10000

WORKDIR /app
COPY package.json ./
RUN npm install --omit=dev
COPY . .

RUN chmod +x render-build.sh && ./render-build.sh

EXPOSE 10000
CMD ["node", "bot.mjs"]
