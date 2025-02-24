import React, { useState } from "react";
import { createCookie, useNavigate } from 'react-router-dom';
import { fetchWithAuth } from "../Api/api";
import "../../App.css"
import "../../css/RegisterAndLogin.css";

export default () => {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState(""); 
    const navigate = useNavigate();
    
    const handleClick = async (e) => {
        e.preventDefault();
        const user = { email, password };

        try {
            const response = await fetch("http://localhost:8080/api/auth", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(user),
                credentials: "include"
            });

            console.log("Отправляемые данные:", JSON.stringify(user));
            console.log(response);

            if (!response.ok) throw new Error("Ошибка авторизации");

            if (response.status === 400) throw new Error("Email is already taken");

            const data = await response.json();
            localStorage.setItem("accessToken", data.accessToken)
            
            console.log("Успешная авторизация!");
            navigate("/main");
        } catch (error) {
            console.error("Ошибка входа:", error.message);
        }
    };

    return (
        <div className="background">
            <form action="" method="post">
                <h1 className="up-text">Добро пожаловать</h1>

                <h1 className="left-align">Ваш Email</h1>
                <input
                    type="email"
                    name="email"
                    id="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                />

                <h1 className="left-align">Ваш Пароль</h1>
                <input
                    type="password"
                    name="password"
                    id="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />

                <div>
                    <button type="submit" onClick={handleClick}>
                        Отправить
                    </button>
                </div>
            </form>
        </div>
    );
};
