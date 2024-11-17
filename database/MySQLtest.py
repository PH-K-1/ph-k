import mysql.connector
from mysql.connector import Error

# 데이터베이스 설정
db_config = {
    'host': '203.250.133.118',
    'user': 'host',
    'password': '0731',
    'database': 'auction_db'
}

def connect_to_db():
    """MySQL 데이터베이스 연결 함수"""
    try:
        connection = mysql.connector.connect(**db_config)
        if connection.is_connected():
            print("Successfully connected to the database")
            return connection
    except Error as e:
        print(f"Error: {e}")
        return None

def reset_table():
    """테이블을 재설정하는 함수 (기존 테이블 삭제 후 재생성)"""
    connection = connect_to_db()
    if connection:
        cursor = connection.cursor()
        # 기존 테이블 삭제
        cursor.execute("DROP TABLE IF EXISTS users;")
        print("Table 'users' dropped if it existed.")
        
        # 테이블 재생성
        cursor.execute("""
            CREATE TABLE users (
                id VARCHAR(50) PRIMARY KEY,
                password VARCHAR(255) NOT NULL
            );
        """)
        connection.commit()
        print("Table 'users' has been recreated.")
        cursor.close()
        connection.close()

def insert_user(user_id, password):
    """새로운 사용자 추가 함수"""
    connection = connect_to_db()
    if connection:
        cursor = connection.cursor()
        cursor.execute("""
            INSERT INTO users (id, password) 
            VALUES (%s, %s);
        """, (user_id, password))
        connection.commit()
        print(f"User {user_id} added.")
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

def delete_all_users():
    """모든 사용자 삭제 함수"""
    connection = connect_to_db()
    if connection:
        cursor = connection.cursor()
        cursor.execute("DELETE FROM users;")
        connection.commit()
        print("All users have been deleted.")
        cursor.close()
        connection.close()

if __name__ == '__main__':
    # 1. 테이블 재설정
    reset_table()

    # 2. 사용자 추가
    insert_user('user1', 'password123')
    insert_user('user2', 'mypassword456')

    # 3. 사용자 조회
    fetch_users()

    # 4. 모든 사용자 삭제
    delete_all_users()

    # 5. 사용자 조회 (삭제 후)
    fetch_users()
