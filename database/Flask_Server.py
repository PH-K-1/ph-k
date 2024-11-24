from flask import Flask, request, jsonify
from flask_cors import CORS
import mysql.connector

app = Flask(__name__)
CORS(app)  # CORS 설정

# MySQL 연결 설정
connection = mysql.connector.connect(
    host="203.250.133.118",
    user="host",
    password="0731",
    database="auction_db"
)

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
        return jsonify({"message": "로그인 실패", "error": str(e)}), 400

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=7310)
