from flask import Flask, request, jsonify, send_from_directory
from flask_cors import CORS, cross_origin
import mysql.connector
import os
import uuid  # 고유 ID 생성
from datetime import datetime  # 현재 시간 사용

app = Flask(__name__)
CORS(app)  # CORS 설정

# MySQL 연결 설정
def create_connection():
    return mysql.connector.connect(
        host="203.250.133.118",
        user="host",
        password="0731",
        database="auction_db"
    )

# 전역 변수로 connection을 설정
connection = create_connection()

# 이미지 저장 경로 설정
UPLOAD_FOLDER = './static/upload'
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
os.makedirs(UPLOAD_FOLDER, exist_ok=True)  # upload 디렉토리 없으면 생성

@app.route('/signup', methods=['POST'])
def signup_user():
    try:
        data = request.get_json()
        username = data['username']
        password = data['password']

        cursor = connection.cursor()
        query = "INSERT INTO users (username, password) VALUES (%s, %s)"
        cursor.execute(query, (username, password))
        connection.commit()
        return jsonify({"message": "회원가입 성공"}), 201
    except Exception as e:
        print("signup Error:", str(e))  # 서버 로그에 출력
        return jsonify({"message": "회원가입 실패", "error": str(e)}), 400

@app.route('/login', methods=['POST'])
def login_user():
    try:
        data = request.get_json()
        username = data['username']
        password = data['password']

        cursor = connection.cursor()
        query = "SELECT id FROM users WHERE username = %s AND password = %s"
        cursor.execute(query, (username, password))
        user = cursor.fetchone()

        if user:
            return jsonify({"message": "로그인 성공", "user_id": user[0]}), 200
        else:
            return jsonify({"message": "로그인 실패, 아이디 또는 비밀번호가 잘못되었습니다."}), 401
    except Exception as e:
        print("Login Error:", str(e))  # 서버 로그에 출력
        return jsonify({"message": "로그인 실패", "error": str(e)}), 400

@app.route('/upload', methods=['POST'])
def upload_item():
    global connection
    try:
        title = request.form['title']
        description = request.form['description']
        price = request.form['price']
        user_id = request.form['user_id']  # 사용자의 이름 (user_id)로 받아옴

        saved_image_paths = []
        for file_key in request.files:
            image = request.files[file_key]
            extension = os.path.splitext(image.filename)[1]
            unique_filename = f"{datetime.now().strftime('%Y%m%d%H%M%S')}_{uuid.uuid4().hex}{extension}"
            image_path = os.path.join(app.config['UPLOAD_FOLDER'], unique_filename)
            image.save(image_path)
            saved_image_paths.append(image_path)

        # MySQL 연결 확인 및 재연결
        if not connection.is_connected():
            connection.close()
            connection = create_connection()

        # 이미지 경로들을 하나의 문자열로 합침
        image_paths_string = ','.join(saved_image_paths)

        cursor = connection.cursor()
        query = """
        INSERT INTO items (title, description, price, image_path, user_id)
        VALUES (%s, %s, %s, %s, %s)
        """
        cursor.execute(query, (title, description, price, image_paths_string, user_id))  # user_id는 사용자 이름
        connection.commit()

        return jsonify({"message": "등록 성공", "image_count": len(saved_image_paths)}), 201
    except Exception as e:
        print("Upload Error:", str(e))
        return jsonify({"message": "등록 실패", "error": str(e)}), 400


@app.route('/get_items', methods=['GET'])
def get_items():
    global connection
    try:
        if not connection.is_connected():
            connection.close()
            connection = create_connection()

        # user_id 없이 모든 아이템 반환
        cursor = connection.cursor(dictionary=True)  # dictionary=True 추가
        query = "SELECT id, title, description, price, image_path, user_id FROM items"
        cursor.execute(query)
        items = cursor.fetchall()

        # 이미지를 클라이언트가 접근할 수 있도록 절대 경로 제공
        for item in items:
            server_ip = request.host_url.strip('/')  # 현재 서버의 IP와 포트 가져오기

            # 여러 이미지 경로 처리
            image_paths = item['image_path'].split(',')
            image_urls = []
            for path in image_paths:
                image_urls.append(f'{server_ip}/static/upload/{os.path.basename(path)}')

            item['image_urls'] = image_urls  # 이미지 URL 배열로 반환

        return jsonify({"items": items}), 200
    except Exception as e:
        return jsonify({"message": "게시글 조회 실패", "error": str(e)}), 400

# 게시글 삭제 기능
@app.route('/posts/<int:item_id>', methods=['DELETE'])
def delete_item_by_id(item_id):
    global connection
    try:
        # MySQL 연결 확인 및 재연결.
        if not connection.is_connected():
            connection = create_connection()

        # 아이템 존재 확인
        cursor = connection.cursor(dictionary=True)  # dictionary=True 추가
        cursor.execute("SELECT user_id, image_path FROM items WHERE id = %s", (item_id,))
        item = cursor.fetchone()

        if not item:
            return jsonify({"message": "아이템이 존재하지 않습니다."}), 404

        # 아이템 삭제
        cursor.execute("DELETE FROM items WHERE id = %s", (item_id,))
        connection.commit()

        # 이미지 파일 삭제
        image_paths = item['image_path'].split(',')
        for path in image_paths:
            try:
                os.remove(path)
            except OSError as e:
                print("File Delete Error:", e)

        return jsonify({"message": "게시글 삭제 성공"}), 200
    except Exception as e:
        print("Delete Item Error:", str(e))
        return jsonify({"message": "게시글 삭제 실패", "error": str(e)}), 400

# 게시글 수정 기능 (PUT)
@app.route('/posts/<int:item_id>', methods=['PUT'])
def update_item_by_id(item_id):
    global connection
    try:
        # item_id가 유효한지 확인
        if item_id <= 0:
            return jsonify({"message": "잘못된 아이템 ID입니다."}), 400

        data = request.get_json()
        title = data['title']
        description = data['description']
        price = data['price']
        image_urls = data['imageUrls']

        # 기존 user_id는 데이터베이스에서 조회하여 사용
        cursor = connection.cursor(dictionary=True)
        cursor.execute("SELECT user_id FROM items WHERE id = %s", (item_id,))
        item = cursor.fetchone()

        if not item:
            return jsonify({"message": "아이템이 존재하지 않습니다."}), 404

        user_id = item['user_id']  # 기존 user_id 가져오기

        # 이미지 경로를 처리 (필요시)
        image_paths_string = ','.join(image_urls)

        if not connection.is_connected():
            connection = create_connection()

        cursor = connection.cursor()
        query = """
        UPDATE items
        SET title = %s, description = %s, price = %s, image_path = %s, user_id = %s
        WHERE id = %s
        """
        cursor.execute(query, (title, description, price, image_paths_string, user_id, item_id))
        connection.commit()

        return jsonify({"message": "게시글 수정 성공"}), 200
    except Exception as e:
        print("Update Item Error:", str(e))
        return jsonify({"message": "게시글 수정 실패", "error": str(e)}), 400



# 이미지 제공 경로
@app.route('/static/upload/<filename>')
@cross_origin()  # CORS 적용
def serve_image(filename):
    return send_from_directory(app.config['UPLOAD_FOLDER'], filename)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=7310)
