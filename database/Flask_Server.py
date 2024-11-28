from flask import Flask, request, jsonify
from flask_cors import CORS
import mysql.connector
import os

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
UPLOAD_FOLDER = './uploads'
os.makedirs(UPLOAD_FOLDER, exist_ok=True)  # uploads 디렉토리 없으면 생성


@app.route('/register', methods=['POST'])
def register_user():
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
        print("Register Error:", str(e))  # 서버 로그에 출력
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

        # 이미지 저장
        image_path = os.path.join(UPLOAD_FOLDER, image.filename)
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

        return jsonify({"items": items}), 200
    except Exception as e:
        return jsonify({"message": "게시글 조회 실패", "error": str(e)}), 400


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=7310)
