import asyncio
import websockets

# 현재 연결된 클라이언트 목록을 저장하는 집합
connected_clients = set()

async def chat_handler(websocket, path):
    """클라이언트와의 연결을 처리하는 핸들러"""
    # 연결된 클라이언트를 목록에 추가
    connected_clients.add(websocket)
    print(f"New client connected: {websocket.remote_address}")

    try:
        # 클라이언트가 보낸 메시지를 계속해서 수신
        async for message in websocket:
            print(f"Received message: {message}")

            # 모든 연결된 클라이언트에게 메시지를 전송
            broadcast_message = f"Broadcast from {websocket.remote_address}: {message}"
            
            # 각 클라이언트에게 메시지를 비동기적으로 전송
            tasks = [asyncio.create_task(client.send(broadcast_message)) for client in connected_clients]
            await asyncio.gather(*tasks)  # 모든 클라이언트에게 메시지 전송
    except Exception as e:
        print(f"Error: {e}")
    finally:
        # 연결 종료 후 클라이언트 목록에서 제거
        connected_clients.remove(websocket)
        print(f"Client disconnected: {websocket.remote_address}")

# WebSocket 서버 시작 (localhost:8765)
async def start_server():
    server = await websockets.serve(chat_handler, "localhost", 8765)
    print("Chat server started on ws://localhost:8765")
    await server.wait_closed()

# 서버 실행
asyncio.run(start_server())
