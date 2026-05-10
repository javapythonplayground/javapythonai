import sqlite3

conn = sqlite3.connect("study.db", check_same_thread=False)
cursor = conn.cursor()

cursor.execute("""
CREATE TABLE IF NOT EXISTS progress (
    user_id TEXT,
    topic TEXT,
    score INTEGER
)
""")

def save_progress(data):
    cursor.execute("INSERT INTO progress VALUES (?, ?, ?)",
                   (data["user_id"], data["topic"], data["score"]))
    conn.commit()

def get_progress(user_id):
    cursor.execute("SELECT * FROM progress WHERE user_id=?", (user_id,))
    return cursor.fetchall()
