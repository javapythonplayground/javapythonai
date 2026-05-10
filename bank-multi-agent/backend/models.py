from pydantic import BaseModel

class TransactionRequest(BaseModel):
    sender: str
    receiver: str
    amount: float
