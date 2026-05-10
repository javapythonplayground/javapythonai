def check_fraud(data):
    if data.amount > 10000:
        return False, "Transaction flagged as fraud"
    return True, "Safe"
