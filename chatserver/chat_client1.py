import asyncio
import websockets

async def chat_client():
    try:
        # 서버에 연결
        async with websockets.connect("ws://localhost:8765") as websocket:
            print("Client connected to the chat server!")

            # 서버에서 이름을 요청하는 메시지를 받음
            greeting = await websocket.recv()
            print(greeting)  # 서버에서 보내는 'Enter your name:' 출력

            # 사용자에게 이름 입력받기
            username = input(">>: ")

            # 이름을 서버로 전송
            await websocket.send(username)

            # 채팅 메시지 전송 및 수신
            while True:
                message = input(f"{username}: ")
                if message.lower() == 'exit':
                    print("Exiting...")
                    break

                await websocket.send(message)  # 메시지 전송
                # 서버로부터 받은 메시지 출력
                response = await websocket.recv()
                print(response)  # 서버에서 받은 메시지를 즉시 출력

    except Exception as e:
        print(f"Connection error: {e}")

# 클라이언트 실행
asyncio.run(chat_client())
