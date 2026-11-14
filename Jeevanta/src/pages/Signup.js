import { useState } from "react";

const Signup = () => {

    const [name, setName] = useState("");

    const [email, setEmail] = useState("");

    const [password, setPassword] = useState("");

    const [file, setFile] = useState(null);

    const handleSubmit = async (e) => {

        e.preventDefault();

        const payload = {
            action: "create",
            user: {
                name,
                email,
                password,
                // Passing filename or could be expanded to Base64 later
                uploadedFile: file ? file.name : null 
            }
        };

        try {

            const response = await fetch(
                "http://localhost:8084/Hackathon/api/users",
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify(payload)
                }
            );

            const json = await response.json();

            if (!response.ok) {

                alert(json.message || "An error occurred");
                return;

            }

            alert("Signup Successful!");

            console.log(json);

        } catch (err) {

            console.log(err);

        }

    };

    return (

        <div className="auth-container">

            <form className="auth-form" onSubmit={handleSubmit}>

                <h2>Create Account</h2>

                <label>Name</label>

                <input
                    type="text"
                    required
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                />

                <label>Email</label>

                <input
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                />

                <label>Password</label>

                <input
                    type="password"
                    required
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />

                <label>Upload Document (Optional)</label>

                <input
                    type="file"
                    onChange={(e) => setFile(e.target.files[0])}
                    style={{ padding: "10px", background: "transparent", boxShadow: "none" }}
                />

                <button type="submit">
                    Sign Up
                </button>

            </form>

        </div>

    );

};

export default Signup;