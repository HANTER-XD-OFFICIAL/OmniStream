#!/usr/bin/env python3
"""
OmniStream Pyrogram Video Uploader Engine
Uses Pyrogram (client.send_video) to stream and upload videos up to 2GB via MTProto,
bypassing Telegram's standard 50MB Bot API upload limit.
"""

import sys
import os
import json
import asyncio
from pyrogram import Client
from pyrogram.types import InlineKeyboardMarkup, InlineKeyboardButton

def build_inline_keyboard(markup_data):
    if not markup_data or not isinstance(markup_data, dict):
        return None
    rows = markup_data.get("inline_keyboard", [])
    if not rows:
        return None
    keyboard = []
    for row in rows:
        btn_row = []
        for btn in row:
            text = btn.get("text", "")
            url = btn.get("url")
            callback_data = btn.get("callback_data")
            if url:
                btn_row.append(InlineKeyboardButton(text=text, url=url))
            elif callback_data:
                btn_row.append(InlineKeyboardButton(text=text, callback_data=callback_data))
        if btn_row:
            keyboard.append(btn_row)
    return InlineKeyboardMarkup(keyboard) if keyboard else None

async def send_video_main():
    if len(sys.argv) < 2:
        print(json.dumps({"ok": False, "error": "No payload argument provided"}))
        sys.exit(1)

    payload_arg = sys.argv[1]
    try:
        if payload_arg.startswith("@"):
            with open(payload_arg[1:], "r", encoding="utf-8") as f:
                data = json.load(f)
        else:
            data = json.loads(payload_arg)
    except Exception as err:
        print(json.dumps({"ok": False, "error": f"Failed to parse payload: {err}"}))
        sys.exit(1)

    bot_token = (
        data.get("bot_token")
        or os.environ.get("BOT_TOKEN")
        or os.environ.get("TELEGRAM_BOT_TOKEN")
        or ""
    ).strip()

    if not bot_token:
        print(json.dumps({"ok": False, "error": "Missing bot_token for Pyrogram client"}))
        sys.exit(1)

    api_id_val = (
        os.environ.get("TELEGRAM_API_ID")
        or os.environ.get("API_ID")
        or data.get("api_id")
        or "2040"
    )
    api_hash_val = (
        os.environ.get("TELEGRAM_API_HASH")
        or os.environ.get("API_HASH")
        or data.get("api_hash")
        or "b18441a1ff607e10a989891a5462e627"
    )

    try:
        api_id = int(str(api_id_val).strip())
    except Exception:
        api_id = 2040
    api_hash = str(api_hash_val).strip()

    chat_id_raw = data.get("chat_id")
    try:
        chat_id = int(chat_id_raw)
    except Exception:
        chat_id = str(chat_id_raw)

    video_path = data.get("video_path")
    if not video_path or not os.path.exists(video_path):
        print(json.dumps({"ok": False, "error": f"Video file not found at: {video_path}"}))
        sys.exit(1)

    caption = data.get("caption", "")
    duration = int(data.get("duration") or 0)
    width = int(data.get("width") or 0)
    height = int(data.get("height") or 0)

    thumb_path = data.get("thumb_path")
    if thumb_path and not os.path.exists(thumb_path):
        thumb_path = None

    reply_markup = build_inline_keyboard(data.get("reply_markup"))

    session_name = "omnistream_uploader"
    workdir = "/tmp/pyrogram_session"
    os.makedirs(workdir, exist_ok=True)

    app = Client(
        name=session_name,
        api_id=api_id,
        api_hash=api_hash,
        bot_token=bot_token,
        workdir=workdir,
        in_memory=False
    )

    try:
        await app.start()
        # Pyrogram client.send_video - streams and uploads up to 2GB via MTProto
        msg = await app.send_video(
            chat_id=chat_id,
            video=video_path,
            caption=caption,
            duration=duration if duration > 0 else None,
            width=width if width > 0 else None,
            height=height if height > 0 else None,
            thumb=thumb_path,
            supports_streaming=True,
            reply_markup=reply_markup
        )
        msg_id = getattr(msg, "id", None) or getattr(msg, "message_id", None)
        print(json.dumps({"ok": True, "result": {"message_id": msg_id}}))
    except Exception as e:
        print(json.dumps({"ok": False, "error": str(e)}))
    finally:
        try:
            await app.stop()
        except Exception:
            pass

if __name__ == "__main__":
    asyncio.run(send_video_main())
