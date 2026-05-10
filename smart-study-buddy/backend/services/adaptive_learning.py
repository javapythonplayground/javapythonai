def adjust_difficulty(score):
    if score > 80:
        return "hard"
    elif score > 50:
        return "medium"
    return "easy"
