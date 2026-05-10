from db.database import get_balance, update_balance

def process_transaction(data):
    sender_balance = get_balance(data.sender)
    receiver_balance = get_balance(data.receiver)

    if sender_balance is None or receiver_balance is None:
        return False, "Account not found"

    if sender_balance < data.amount:
        return False, "Insufficient funds"

    update_balance(data.sender, sender_balance - data.amount)
    update_balance(data.receiver, receiver_balance + data.amount)

    return True, "Transaction successful"
