def extract_key_points(content: str):
    sentences = content.split(".")
    return sentences[:5]
