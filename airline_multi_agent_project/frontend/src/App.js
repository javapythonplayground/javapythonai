import React, { useState } from "react";
import axios from "axios";

function App() {
  const [form, setForm] = useState({
    customer_name: "",
    source: "",
    destination: ""
  });

  const [result, setResult] = useState(null);

  const submitBooking = async () => {
    const response = await axios.post("http://localhost:8000/book", form);
    setResult(response.data);
  };

  return (
    <div style={{ padding: 40 }}>
      <h1>Airline Booking Multi-Agent System</h1>

      <input
        placeholder="Customer Name"
        onChange={(e) =>
          setForm({ ...form, customer_name: e.target.value })
        }
      />
      <br /><br />

      <input
        placeholder="Source"
        onChange={(e) =>
          setForm({ ...form, source: e.target.value })
        }
      />
      <br /><br />

      <input
        placeholder="Destination"
        onChange={(e) =>
          setForm({ ...form, destination: e.target.value })
        }
      />
      <br /><br />

      <button onClick={submitBooking}>Book Ticket</button>

      {result && (
        <div>
          <h3>Booking Result</h3>
          <pre>{JSON.stringify(result, null, 2)}</pre>
        </div>
      )}
    </div>
  );
}

export default App;