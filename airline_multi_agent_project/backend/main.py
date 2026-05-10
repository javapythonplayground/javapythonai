from fastapi import FastAPI
from pydantic import BaseModel
from agents import RootAgent
from database import engine
from models import Base

Base.metadata.create_all(bind=engine)

app = FastAPI()

root_agent = RootAgent()

class BookingRequest(BaseModel):
    customer_name: str
    source: str
    destination: str

@app.get("/")
def home():
    return {"message": "Multi-Agent Airline Booking System"}

@app.post("/book")
def book_ticket(request: BookingRequest):
    result = root_agent.process_booking(
        request.customer_name,
        request.source,
        request.destination
    )
    return result

@app.get("/rag")
def rag(question: str):
    answer = root_agent.rag_agent.search_knowledge(question)
    return {"answer": answer}