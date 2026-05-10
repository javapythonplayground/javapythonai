from agents.validation_agent import validate
from agents.fraud_agent import check_fraud
from agents.transaction_agent import process_transaction
from agents.notification_agent import notify

def handle_transaction(data):
    valid, msg = validate(data)
    if not valid:
        return {"status": "failed", "reason": msg}

    safe, msg = check_fraud(data)
    if not safe:
        return {"status": "failed", "reason": msg}

    success, msg = process_transaction(data)
    if not success:
        return {"status": "failed", "reason": msg}

    note = notify(msg)

    return {
        "status": "success",
        "message": msg,
        "notification": note
    }
