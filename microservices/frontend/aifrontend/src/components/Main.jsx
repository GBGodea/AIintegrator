import React, { useState } from "react";
import { fetchWithAuth } from "./api"; // Импортируем обертку

// TODO Сделать AutoUpdate Refresh Token
export default () => {
    const [id, setId] = useState(""); // Состояние для хранения id
    const [user, setUser] = useState(null); // Состояние для хранения данных пользователя
    const [error, setError] = useState(""); // Состояние для хранения ошибки

    const handleSearch = async (e) => {
        e.preventDefault(); // Предотвращаем перезагрузку страницы

        try {
            const response = await fetchWithAuth(`http://localhost:8080/api/users/${id}`, {
                method: "GET"
            });

            if (!response.ok) {
                throw new Error(`Ошибка: ${response.status} ${await response.text()}`);
            }

            const data = await response.json();
            setUser(data);
            setError("");
        } catch (err) {
            setError(err.message);
            setUser(null);
        }
    };

    return (
        <div>
            <form onSubmit={handleSearch} method="get">
                <h1>Затестируй поиск пользователя</h1>
                <input
                    type="text"
                    value={id}
                    onChange={(e) => setId(e.target.value)}
                    placeholder="Введите ID пользователя"
                />
                <button type="submit">Найти</button>
            </form>

            {error && <p style={{ color: "red" }}>{error}</p>} {/* Отображаем ошибку, если она есть */}

            {user && ( // Отображаем данные пользователя, если они есть
                <div>
                    <h2>Данные пользователя:</h2>
                    <pre>{JSON.stringify(user, null, 2)}</pre> {/* Форматируем JSON для удобного отображения */}
                </div>
            )}
        </div>
    );
};