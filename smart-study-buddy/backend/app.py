from fastapi import FastAPI
from pydantic import BaseModel
from services.content_analyzer import extract_key_points
from services.question_generator import generate_questions
from services.progress_tracker import save_progress, get_progress

app = FastAPI()

class StudyInput(BaseModel):
    content: str

@app.post("/analyze")
def analyze(data: StudyInput):
    key_points = extract_key_points(data.content)
    questions = generate_questions(key_points)
    return {"key_points": key_points, "questions": questions}

@app.post("/progress")
def progress(user: dict):
    save_progress(user)
    return {"status": "saved"}

@app.get("/progress/{user_id}")
def progress_get(user_id: str):
    return get_progress(user_id)
