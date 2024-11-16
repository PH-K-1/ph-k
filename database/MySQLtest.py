import mysql.connector
from mysql.connector import Error

# 데이터베이스 설정
db_config = {
    'host': '203.250.133.118',  # 노트북 ip 203.250.133.118:3306 라즈베리파이:192.168.0.216
    'user': 'host',                 # MySQL 사용자 이름
    'password': '0731',         # MySQL 비밀번호
    'database': 'auction_db'             # 데이터베이스 이름
}

def connect_to_db():
    """MySQL 데이터베이스 연결 함수"""
    try:
        # 데이터베이스 연결
        connection = mysql.connector.connect(**db_config)
        if connection.is_connected():
            print("Successfully connected to the database")
            return connection
    except Error as e:
        print(f"Error: {e}")
        return None

def create_table():
    """간단한 테이블을 생성하는 함수"""
    connection = connect_to_db()
    if connection:
        cursor = connection.cursor()
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id VARCHAR(50) PRIMARY KEY,
                password VARCHAR(255) NOT NULL
            );
        """)
        connection.commit()
        print("Table 'users' created or already exists.")
        cursor.close()
        connection.close()

def insert_user(username, password):
    """새로운 사용자 추가 함수"""
    connection = connect_to_db()
    if connection:
        cursor = connection.cursor()
        cursor.execute("""
            INSERT INTO users (id, password) 
            VALUES (%s, %s);
        """, (username, password))
        connection.commit()
        print(f"User {username} added.")
        cursor.close()
        connection.close()

def fetch_users():
    """사용자 목록을 조회하는 함수"""
    connection = connect_to_db()
    if connection:
        cursor = connection.cursor()
        cursor.execute("SELECT * FROM users")
        result = cursor.fetchall()
        print("Users in the database:")
        for row in result:
            print(f"ID: {row[0]}, Password: {row[1]}")
        cursor.close()
        connection.close()

if __name__ == '__main__':
    # 1. 테이블 생성
    create_table()
    
    # 2. 사용자 추가
    insert_user('user1', 'password123')
    insert_user('user2', 'mypassword456')

    # 3. 사용자 조회
    fetch_users()
