/**
 * Cloudflare Worker: omnistream-telegram-api
 * Securely hosts and delivers the Telegram Bot Token using Authorization Password.
 *
 * Environment Variables / Secrets required in Cloudflare Dashboard:
 * 1. TELEGRAM_BOT_TOKEN : Your Telegram Bot Token from @BotFather (e.g. 123456789:ABCdefGHI...)
 * 2. API_SECRET : Your Authorization Password (default: 432872)
 */

export default {
  async fetch(request, env, ctx) {
    const corsHeaders = {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
      "Access-Control-Allow-Headers": "Authorization, X-API-Key, X-Worker-Secret, Content-Type",
      "Access-Control-Max-Age": "86400"
    };

    // Handle CORS preflight
    if (request.method === "OPTIONS") {
      return new Response(null, {
        status: 204,
        headers: corsHeaders
      });
    }

    const expectedSecret = (env.API_SECRET || env.WORKER_API_SECRET || "432872").trim();

    // Extract authorization credentials from Headers or Query Parameters
    const url = new URL(request.url);
    const authHeader = (request.headers.get("Authorization") || "").trim();
    const apiKeyHeader = (request.headers.get("X-API-Key") || request.headers.get("X-Worker-Secret") || "").trim();
    const queryAuth = (url.searchParams.get("auth") || url.searchParams.get("secret") || url.searchParams.get("password") || "").trim();

    const cleanAuth = authHeader.replace(/^Bearer\s+/i, "").trim();

    const isAuthorized =
      cleanAuth === expectedSecret ||
      apiKeyHeader === expectedSecret ||
      queryAuth === expectedSecret;

    if (!isAuthorized) {
      return new Response("Unauthorized", {
        status: 401,
        headers: {
          "Content-Type": "text/plain;charset=UTF-8",
          ...corsHeaders
        }
      });
    }

    const botToken = (env.TELEGRAM_BOT_TOKEN || env.BOT_TOKEN || "").trim();
    if (!botToken) {
      return new Response(JSON.stringify({ error: "TELEGRAM_BOT_TOKEN is not configured in Cloudflare Worker secrets" }), {
        status: 500,
        headers: {
          "Content-Type": "application/json",
          ...corsHeaders
        }
      });
    }

    // Return bot token as plain text
    return new Response(botToken, {
      status: 200,
      headers: {
        "Content-Type": "text/plain;charset=UTF-8",
        "Cache-Control": "no-store, no-cache, must-revalidate",
        ...corsHeaders
      }
    });
  }
};
