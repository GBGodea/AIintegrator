import React, { useState } from "react";
import { useNavigate } from 'react-router-dom';
import { fetchWithAuth } from "./api"; // Импортируем функцию

export default () => {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState(""); 
    const navigate = useNavigate();
    
    const handleClick = async (e) => {
        e.preventDefault();
        const user = { email, password };

        try {
            const response = await fetchWithAuth("http://localhost:8080/api/auth", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(user),
            });

            console.log("Отправляемые данные:", JSON.stringify(user));
            console.log(response);

            if (!response.ok) throw new Error("Ошибка авторизации");

            const data = await response.json();
            // Сохраняем accessToken в localStorage
            localStorage.setItem("accessToken", data.accessToken);
            // refreshToken будет автоматически отправлен сервером в cookie (HttpOnly)
            
            console.log("Успешная авторизация!");
            navigate("/main");
        } catch (error) {
            console.error("Ошибка входа:", error.message);
        }
    };

    return (
        <div>
            <form action="" method="post">
                <h1>Авторизация:</h1>

                <h1>Ваш Email</h1>
                <input
                    type="email"
                    name="email"
                    id="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                />

                <h1>Ваш Пароль</h1>
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
