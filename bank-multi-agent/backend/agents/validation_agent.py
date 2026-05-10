def validate(data):
    if data.amount <= 0:
        return False, "Invalid amount"
    if data.sender == data.receiver:
        return False, "Sender and receiver cannot be same"
    return True, "Valid"
