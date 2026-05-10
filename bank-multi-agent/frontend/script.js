async function send() {
    const data = {
        sender: document.getElementById("sender").value,
        receiver: document.getElementById("receiver").value,
        amount: parseFloat(document.getElementById("amount").value)
    };

    const res = await fetch("http://localhost:8000/transfer", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(data)
    });

    const result = await res.json();
    document.getElementById("output").innerText = JSON.stringify(result, null, 2);
}
