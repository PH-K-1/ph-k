import asyncio
import websockets

async def chat_client():
    """서버와 연결하고 메시지를 보내고 받는 클라이언트"""
    try:
        # 서버에 연결
        async with websockets.connect("ws://localhost:8765") as websocket:
            print("Connected to the chat server!")

            # 클라이언트가 보낼 메시지를 입력받고 전송
            while True:
                message = input("You: ")
                if message.lower() == 'exit':
                    print("Exiting chat...")
                    break

                # 메시지 서버로 전송
                await websocket.send(message)

                # 서버에서 받은 메시지 출력
                response = await websocket.recv()
                print("Server:", response)

    except Exception as e:
        print(f"Connection error: {e}")

# 클라이언트 실행
asyncio.run(chat_client())
