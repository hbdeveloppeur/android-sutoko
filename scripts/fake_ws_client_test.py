#!/usr/bin/env python3
"""
Self-test client for fake_ws_server.py. Simulates the Sutoko app frame flow
and asserts the server behaves per scenario. Exits non-zero on failure.

Usage: python3 scripts/fake_ws_client_test.py <scenario> [port]
Run the matching server scenario first.
"""

import asyncio
import json
import sys
import uuid

import websockets

SCENARIO = sys.argv[1] if len(sys.argv) > 1 else "ack"
PORT = int(sys.argv[2]) if len(sys.argv) > 2 else 8765
URL = f"ws://127.0.0.1:{PORT}/connect/ws"
TIMEOUT = 5.0


async def recv_action(ws, want, timeout=TIMEOUT):
    """Read frames until one has action == want; return it."""
    while True:
        frame = json.loads(await asyncio.wait_for(ws.recv(), timeout))
        print("  recv:", frame)
        if frame.get("action") == want:
            return frame


def send_message_frame(with_username=False):
    msg_id = str(uuid.uuid4())
    frame = {
        "action": "send_message",
        "characterId": 7,
        "mrpId": str(uuid.uuid4()),
        "uid": "test-uid",
        "token": "test-token",
        "appVersion": "0.0-test",
        "texts": [{"id": msg_id, "text": "hello fake", "role": "user"}],
        "timeZoneId": "Europe/Paris",
        "langCode": "en",
    }
    if with_username:
        frame["userName"] = "Hocine"
    return frame, msg_id


async def main():
    async with websockets.connect(URL) as ws:
        await recv_action(ws, "connected")
        await ws.send(json.dumps({
            "action": "authenticate", "token": "test-token", "uid": "test-uid",
        }))
        await recv_action(ws, "authenticated")
        print("handshake OK")

        if SCENARIO == "ack":
            frame, msg_id = send_message_frame()
            await ws.send(json.dumps(frame))
            ack = await recv_action(ws, "message_ack")
            assert ack["serialIds"] == [msg_id], f"bad serialIds: {ack}"
            reply = await recv_action(ws, "new_message")
            assert reply["role"] == "assistant"
            print("ack scenario OK")

        elif SCENARIO == "username":
            frame, msg_id = send_message_frame(with_username=False)
            await ws.send(json.dumps(frame))
            err = await recv_action(ws, "error_code")
            assert err["code"] == "username_required", f"bad error: {err}"
            # retry with userName (what the app does after popup confirm)
            frame, msg_id2 = send_message_frame(with_username=True)
            frame["texts"][0]["id"] = msg_id  # app resends with SAME id
            await ws.send(json.dumps(frame))
            ack = await recv_action(ws, "message_ack")
            assert msg_id in ack["serialIds"], f"resent id not acked: {ack}"
            await recv_action(ws, "new_message")
            print("username scenario OK (reject -> popup retry -> ack, same id)")

        elif SCENARIO == "silent":
            frame, _ = send_message_frame()
            await ws.send(json.dumps(frame))
            try:
                await recv_action(ws, "message_ack", timeout=2.0)
            except asyncio.TimeoutError:
                print("silent scenario OK (no ack)")
            else:
                raise AssertionError("silent server acked!")

        elif SCENARIO == "funds":
            frame, _ = send_message_frame()
            await ws.send(json.dumps(frame))
            err = await recv_action(ws, "error_code")
            assert err["code"] == "insufficient_funds"
            print("funds scenario OK")

        else:
            raise AssertionError(f"unknown scenario {SCENARIO!r}")


if __name__ == "__main__":
    asyncio.run(main())
    print("PASS")
