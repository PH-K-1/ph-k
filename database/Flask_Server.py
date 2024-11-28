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
        query = "SELECT * FROM users WHERE username = %s AND password = %s"
        cursor.execute(query, (username, password))
        user = cursor.fetchone()

        if user:
            return jsonify({"message": "로그인 성공"}), 200
        else:
            return jsonify({"message": "로그인 실패, 아이디 또는 비밀번호가 잘못되었습니다."}), 401
    except Exception as e:
        print("Login Error:", str(e))  # 서버 로그에 출력
        return jsonify({"message": "로그인 실패", "error": str(e)}), 400


@app.route('/upload', methods=['POST'])
def upload_item():
    global connection  # 전역 변수 connection 선언
    try:
        title = request.form['title']
        description = request.form['description']
        price = request.form['price']
        image = request.files['image']

        # 고유한 파일 이름 생성
        extension = os.path.splitext(image.filename)[1]  # 파일 확장자 추출
        unique_filename = f"{datetime.now().strftime('%Y%m%d%H%M%S')}_{uuid.uuid4().hex}{extension}"
        image_path = os.path.join(app.config['UPLOAD_FOLDER'], unique_filename)

        # 이미지 저장
        image.save(image_path)

        # MySQL 연결 확인 및 재연결
        if not connection.is_connected():
            connection.close()  # 기존 연결 닫기
            connection = create_connection()  # 새 연결 생성

        # 데이터베이스 저장
        cursor = connection.cursor()
        query = """
        INSERT INTO items (title, description, price, image_path) 
        VALUES (%s, %s, %s, %s)
        """
        cursor.execute(query, (title, description, price, image_path))
        connection.commit()

        return jsonify({"message": "등록 성공"}), 201
    except Exception as e:
        print("Upload Error:", str(e))  # 서버 로그에 출력
        return jsonify({"message": "등록 실패", "error": str(e)}), 400

@app.route('/get_items', methods=['GET'])
def get_items():
    try:
        global connection
        if not connection.is_connected():
            connection.close()
            connection = create_connection()

        cursor = connection.cursor(dictionary=True)
        query = "SELECT id, title, description, price, image_path FROM items"
        cursor.execute(query)
        items = cursor.fetchall()

        # 이미지를 클라이언트가 접근할 수 있도록 절대 경로 제공
        for item in items:
            item['image_url'] = f'http://192.168.200.114:7310/static/upload/{os.path.basename(item["image_path"])}'

        return jsonify({"items": items}), 200
    except Exception as e:
        return jsonify({"message": "게시글 조회 실패", "error": str(e)}), 400


# 이미지 제공 경로
@app.route('/static/upload/<filename>')
@cross_origin()  # CORS 적용
def serve_image(filename):
    return send_from_directory(app.config['UPLOAD_FOLDER'], filename)


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=7310)
