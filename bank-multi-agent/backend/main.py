from fastapi import FastAPI
from models import TransactionRequest
from agents.root_agent import handle_transaction
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.post("/transfer")
def transfer(req: TransactionRequest):
    return handle_transaction(req)
