import { useState } from "react";
import "./AIConsultant.css";

const AIConsultant = () => {

    const [query, setQuery] = useState("");
    const [loading, setLoading] = useState(false);
    const [chatHistory, setChatHistory] = useState([]);

    const handleSubmit = async (e) => {

        e.preventDefault();

        if (!query.trim()) return;

        const patientId = localStorage.getItem("loggedInUserId") || 1;

        setLoading(true);

        // Add user message to chat history
        setChatHistory(prev => [...prev, { type: "user", text: query }]);

        try {

            const response = await fetch(
                "http://localhost:8084/Hackathon/api/ai-consult",
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        patientId: parseInt(patientId),
                        query: query.trim()
                    })
                }
            );

            const data = await response.json();

            if (!data.success) {
                setChatHistory(prev => [...prev, { type: "error", text: data.message || "Something went wrong." }]);
            } else {
                setChatHistory(prev => [...prev, {
                    type: "ai",
                    analysis: data.analysis,
                    criticalWarning: data.criticalWarning,
                    suggestedSpecialist: data.suggestedSpecialist
                }]);
            }

        } catch (err) {

            setChatHistory(prev => [...prev, { type: "error", text: "Failed to connect to the AI service: " + err.message }]);

        } finally {

            setLoading(false);
            setQuery("");

        }

    };

    return (

        <div className="ai-consultant">

            <div className="ai-header">
                <h1>🩺 AI Medical Consultant</h1>
                <p>Ask about medicines, symptoms, or health concerns</p>
            </div>

            {/* Chat History */}
            <div className="chat-container">

                {chatHistory.length === 0 && (
                    <div className="chat-empty">
                        <span className="empty-icon">💬</span>
                        <p>Start by asking a medical question below.</p>
                        <p className="examples">
                            Try: "Can I take Amoxicillin?" or "I have a persistent headache"
                        </p>
                    </div>
                )}

                {chatHistory.map((msg, index) => (

                    <div key={index} className={`chat-bubble ${msg.type}`}>

                        {msg.type === "user" && (
                            <div className="user-msg">
                                <span className="avatar">🧑</span>
                                <p>{msg.text}</p>
                            </div>
                        )}

                        {msg.type === "ai" && (
                            <div className="ai-msg">
                                <span className="avatar">🤖</span>
                                <div className="ai-content">

                                    {msg.criticalWarning && (
                                        <div className="critical-warning">
                                            <span className="warning-icon">⚠️</span>
                                            <div>
                                                <strong>CRITICAL WARNING</strong>
                                                <p>{msg.criticalWarning}</p>
                                            </div>
                                        </div>
                                    )}

                                    <div className="analysis-section">
                                        <h4>Analysis</h4>
                                        <p>{msg.analysis}</p>
                                    </div>

                                    <div className="specialist-section">
                                        <span className="specialist-icon">👨‍⚕️</span>
                                        <p>
                                            <strong>Suggested Specialist:</strong>{" "}
                                            {msg.suggestedSpecialist}
                                        </p>
                                    </div>

                                </div>
                            </div>
                        )}

                        {msg.type === "error" && (
                            <div className="error-msg">
                                <span className="avatar">❌</span>
                                <p>{msg.text}</p>
                            </div>
                        )}

                    </div>

                ))}

                {loading && (
                    <div className="chat-bubble ai">
                        <div className="ai-msg">
                            <span className="avatar">🤖</span>
                            <div className="typing-indicator">
                                <span></span>
                                <span></span>
                                <span></span>
                            </div>
                        </div>
                    </div>
                )}

            </div>

            {/* Input Form */}
            <form className="chat-input-form" onSubmit={handleSubmit}>

                <input
                    type="text"
                    placeholder="Ask about a medicine, symptom, or condition..."
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    disabled={loading}
                />

                <button type="submit" disabled={loading || !query.trim()}>
                    {loading ? "Thinking..." : "Send"}
                </button>

            </form>

        </div>

    );

};

export default AIConsultant;
