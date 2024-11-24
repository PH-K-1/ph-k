import mysql.connector
from mysql.connector import Error

# MySQL 서버 연결 설정
host = "203.250.133.118"  # << 외부접속 ip 이걸로 열어둠 이걸로 하셈
user = "host"  # MySQL 사용자 이름 << vscode든 뭐든 mysql추가 설치해서 유저 만들면 됨
password = "0731"  # MySQL 비밀번호 << 비밀번호 이걸로 고정
database = "auction_db"  # db는 더 건들지 말것. 테이블만 건드리셈
try:
    # MySQL 서버에 연결
    connection = mysql.connector.connect(
        host=host,
        user=user,
        password=password,
        database=database
    )

    # 연결 확인
    if connection.is_connected():
        print("MySQL 연결 성공!")
        cursor = connection.cursor()
        cursor.execute("SELECT DATABASE();")  # 현재 연결된 데이터베이스 확인
        db_info = cursor.fetchone()
        print("현재 연결된 데이터베이스:", db_info[0])

except Error as e:
    print("MySQL 연결 실패:", e)

finally:
    # 연결 종료
    if connection.is_connected():
        cursor.close()
        connection.close()
        print("MySQL 연결 종료.")
