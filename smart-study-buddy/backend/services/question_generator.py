def generate_questions(key_points):
    questions = []
    for point in key_points:
        questions.append(f"What is meant by: {point.strip()}?")
    return questions
