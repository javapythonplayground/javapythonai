import sqlite3

conn = sqlite3.connect("bank.db", check_same_thread=False)
cursor = conn.cursor()

cursor.execute("""
CREATE TABLE IF NOT EXISTS accounts (
    name TEXT PRIMARY KEY,
    balance REAL
)
""")

cursor.execute("INSERT OR IGNORE INTO accounts VALUES ('Alice', 1000)")
cursor.execute("INSERT OR IGNORE INTO accounts VALUES ('Bob', 500)")
conn.commit()

def get_balance(name):
    cursor.execute("SELECT balance FROM accounts WHERE name=?", (name,))
    result = cursor.fetchone()
    return result[0] if result else None

def update_balance(name, amount):
    cursor.execute("UPDATE accounts SET balance=? WHERE name=?", (amount, name))
    conn.commit()
