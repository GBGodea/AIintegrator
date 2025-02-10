import React, { useState } from "react"
import { useNavigate } from 'react-router-dom';

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
        <div>
            <form action="" method="post">
                <h1>Регистрация:</h1>
                <h1>Ваш Email</h1>
                <input type="email" name="email" id="email" value={email} onChange={(e) => setEmail(e.target.value)} />

                <h1>Ваш Пароль</h1>
                <input type="password" name="password" id="password" value={password} onChange={(e) => setPassword(e.target.value)} />

                <div>
                    <button type="submit" onClick={handleClick}>Отправить</button>
                </div>
            </form>
        </div>
    );
}