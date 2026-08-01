#!/usr/bin/env python3
"""
Fake AI-conversation WebSocket server for local testing of the Sutoko app.

Reproduces the frame protocol of the real backend (wss://ai-conversation.sutoko.app/connect/ws):

  server -> client : {"action":"connected"}            (immediately on connect)
  client -> server : {"action":"authenticate","token":...,"uid":...}
  server -> client : {"action":"authenticated"}
  client -> server : {"action":"send_message","texts":[{"id","text","role"},...], ...}
  server -> client : {"action":"message_ack","serialIds":["<texts[].id>", ...]}
  server -> client : {"action":"error_code","code":"username_required"}
  server -> client : {"action":"typing"} / {"action":"stop_typing"}
  server -> client : {"action":"new_message", ...}     (assistant echo reply)

Scenarios (pass as argv[1]):
  ack        (default) ack every send_message, then typing + echo reply.
  username   reject send_message frames WITHOUT "userName" with error_code
             username_required; once a frame carries userName, ack + echo.
             Reproduces the username-popup retry flow.
  silent     never ack, never reply. Verify the queue keeps messages.
  funds      always reply error_code insufficient_funds.

Usage:
  python3 scripts/fake_ws_server.py [scenario] [port]

Point the app at it (emulator):
  Server.webSocket() -> "ws://10.0.2.2:8765/connect/ws"
"""

import asyncio
import json
import sys
import time
import uuid

import websockets

SCENARIO = sys.argv[1] if len(sys.argv) > 1 else "ack"
PORT = int(sys.argv[2]) if len(sys.argv) > 2 else 8765


def log(direction, obj):
    print(f"{time.strftime('%H:%M:%S')} {direction} {json.dumps(obj)}", flush=True)


async def send(ws, obj):
    log("server ->", obj)
    await ws.send(json.dumps(obj))


async def echo_reply(ws, req):
    """Simulate the AI answering: typing, pause, stop_typing, new_message."""
    await send(ws, {"action": "typing"})
    await asyncio.sleep(1.0)
    await send(ws, {"action": "stop_typing"})
    texts = req.get("texts") or []
    quoted = texts[0]["text"] if texts else "..."
    await send(ws, {
        "action": "new_message",
        "id": str(uuid.uuid4()),
        "createdAt": int(time.time()),
        "text": f"[fake] you said: {quoted}",
        "aiCharacterId": str(req.get("characterId", 7)),
        "role": "assistant",
        "type": "text",
        "conversationId": "1",
        "userId": req.get("uid", "fake-uid"),
    })


async def handle_send_message(ws, req):
    ids = [t["id"] for t in req.get("texts", []) if "id" in t]

    if SCENARIO == "silent":
        print("   (silent: no ack)", flush=True)
        return

    if SCENARIO == "funds":
        await send(ws, {"action": "error_code", "code": "insufficient_funds"})
        return

    if SCENARIO == "username" and "userName" not in req:
        await send(ws, {"action": "error_code", "code": "username_required"})
        return

    if ids:
        await send(ws, {"action": "message_ack", "serialIds": ids})
    await echo_reply(ws, req)


async def handle(ws):
    log("server ->", {"info": f"client connected, path={ws.request.path}"})
    await send(ws, {"action": "connected"})
    try:
        async for raw in ws:
            try:
                req = json.loads(raw)
            except json.JSONDecodeError:
                print(f"!! non-JSON frame: {raw!r}", flush=True)
                continue
            log("client ->", req)
            action = req.get("action")
            if action == "authenticate":
                await send(ws, {"action": "authenticated"})
            elif action == "send_message":
                await handle_send_message(ws, req)
            elif action == "pong":
                pass
            else:
                print(f"   (unknown action {action!r}: ignored)", flush=True)
    except websockets.ConnectionClosed:
        print("client disconnected", flush=True)


async def main():
    print(f"fake WS server: scenario={SCENARIO} port={PORT}", flush=True)
    async with websockets.serve(handle, "0.0.0.0", PORT):
        await asyncio.Future()


if __name__ == "__main__":
    asyncio.run(main())
