async function analyze() {
    const content = document.getElementById("content").value;

    const res = await fetch("http://localhost:8000/analyze", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({content})
    });

    const data = await res.json();
    document.getElementById("output").innerText = JSON.stringify(data, null, 2);
}
