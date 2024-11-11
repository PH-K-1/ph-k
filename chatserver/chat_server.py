import asyncio
import websockets

# 연결된 클라이언트를 저장할 딕셔너리
connected_clients = {}

async def chat_handler(websocket, path):
    # 클라이언트와 연결되면 이름을 받는다
    await websocket.send("이름을 입력하세요")  # 이름을 요청하는 메시지
    username = await websocket.recv()  # 클라이언트에서 이름을 받는다

    # 클라이언트를 연결된 목록에 추가
    connected_clients[websocket] = username
    print(f"{username} has joined the chat")

    try:
        # 클라이언트로부터 메시지를 받는다
        async for message in websocket:
            print(f"Received message from {username}: {message}")

            # 해당 클라이언트 외의 모든 클라이언트에게 메시지 전송
            for client_ws, client_username in connected_clients.items():
                if client_ws != websocket:
                    broadcast_message = f"{username} : {message}"
                    await client_ws.send(broadcast_message)
    except Exception as e:
        print(f"Connection error: {e}")
    finally:
        # 연결 종료 시 클라이언트 목록에서 제거
        del connected_clients[websocket]
        print(f"{username} has left the chat")

# 서버 시작
async def start_server():
    server = await websockets.serve(chat_handler, "localhost", 8765)
    print("Chat server started on ws://localhost:8765")
    await server.wait_closed()

# 비동기 루프 실행
asyncio.get_event_loop().run_until_complete(start_server())
