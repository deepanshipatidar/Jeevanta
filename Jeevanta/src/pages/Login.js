import { useState } from "react";

const Login = () => {

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    const handleSubmit = async (e) => {

        e.preventDefault();

        const payload = {
            action: "login",
            email,
            password
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

            alert("Login Successful!");

            console.log(json);

        } catch (err) {

            console.log(err);

        }

    };

    return (

        <div className="auth-container">

            <form className="auth-form" onSubmit={handleSubmit}>

                <h2>Login</h2>

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

                <button type="submit">
                    Login
                </button>

            </form>

        </div>

    );

};

export default Login;