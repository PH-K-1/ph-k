from flask import Flask, request, jsonify
import mysql.connector
from mysql.connector import Error

app = Flask(__name__)

# MySQL 연결 설정
host = "203.250.133.118"  # MySQL 서버 IP 주소
db_user = "host"  # MySQL 사용자 이름
db_password = "0731"  # MySQL 비밀번호
database = "auction_db"  # 데이터베이스 이름

# 회원가입 라우트
@app.route('/register', methods=['POST'])
def register():
    connection = None
    cursor = None
    try:
        # MySQL 서버에 연결
        connection = mysql.connector.connect(
            host=host,
            user=db_user,
            password=db_password,
            database=database
        )

        if connection.is_connected():
            data = request.get_json()  # 안드로이드에서 보내는 JSON 데이터 받기
            print(f"Received data: {data}")  # 수신한 데이터 출력

            if not data:
                return jsonify({"message": "No JSON data received"}), 400

            username = data.get('username')
            password = data.get('password')

            if not username or not password:
                return jsonify({"message": "Missing required fields"}), 400

            cursor = connection.cursor()
            cursor.execute("SELECT * FROM users WHERE username = %s", (username,))
            existing_user = cursor.fetchone()

            if existing_user:
                return jsonify({"message": "Username already exists!"}), 400

            cursor.execute("INSERT INTO users (username, password) VALUES (%s, %s)", (username, password))
            connection.commit()

            return jsonify({"message": "User registered successfully!"}), 200

    except mysql.connector.Error as e:
        return jsonify({"message": f"Database error: {e}"}), 500
    except Exception as e:
        return jsonify({"message": f"Error: {str(e)}"}), 500
    finally:
        if cursor:
            cursor.close()
        if connection and connection.is_connected():
            connection.close()

@app.route('/login', methods=['POST'])
def login():
    # Content-Type이 'application/json'일 때만 데이터를 파싱
    if request.content_type != 'application/json':
        return jsonify({"message": "Content-Type must be application/json"}), 415

    connection = None
    cursor = None
    try:
        # MySQL 서버에 연결
        connection = mysql.connector.connect(
            host=host,
            user=db_user,
            password=db_password,
            database=database
        )

        if connection.is_connected():
            data = request.get_json()  # 안드로이드에서 보내는 JSON 데이터 받기
            if not data:
                return jsonify({"message": "No JSON data received"}), 400

            username = data.get('username')
            password = data.get('password')

            if not username or not password:
                return jsonify({"message": "Username or password missing"}), 400

            cursor = connection.cursor()
            cursor.execute("SELECT * FROM users WHERE username = %s", (username,))
            user = cursor.fetchone()

            if user and user[2] == password:  # user[2]는 'password' 필드
                return jsonify({"message": "Login successful!"}), 200
            else:
                return jsonify({"message": "Invalid username or password!"}), 400

    except mysql.connector.Error as e:
        return jsonify({"message": f"Database error: {e}"}), 500
    except Exception as e:
        return jsonify({"message": f"Error: {str(e)}"}), 500
    finally:
        if cursor:
            cursor.close()
        if connection and connection.is_connected():
            connection.close()

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)
