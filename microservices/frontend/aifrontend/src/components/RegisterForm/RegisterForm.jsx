import React, { useState } from "react";
import { useNavigate } from 'react-router-dom';
import "../../App.css";
import "./RegisterForm.css";
import "../../css/RegisterAndLogin.css";

export default () => {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const navigate = useNavigate();

    const handleClick = (e) => {
        e.preventDefault();
        const user = { email, password };
        console.log(user);
        fetch("http://localhost:8080/api/users", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(user)
        }).then(() => {
            console.log("New user added");
            navigate("/auth");
        })
    }

    return (
        <div className="background">
            <form action="" method="post">
                <h1 className="up-text">Регистрация</h1>
                <h1 className="left-align">Ваш Email:</h1>
                <input type="email" name="email" id="email" value={email} onChange={(e) => setEmail(e.target.value)} />

                <h1 className="left-align">Ваш Пароль:</h1>
                <input className="input-text" type="password" name="password" id="password" value={password} onChange={(e) => setPassword(e.target.value)} />

                <div>
                    <button type="submit" onClick={handleClick}>Регистрация</button>
                </div>
            </form>
        </div>
    );
}