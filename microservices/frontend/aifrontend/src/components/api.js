// api.js
export const fetchWithAuth = async (url, options = {}) => {
    let accessToken = localStorage.getItem("accessToken");

    if (!options.headers) {
        options.headers = {};
    }

    options.headers["Authorization"] = `Bearer ${accessToken}`;
    options.credentials = "include"; // Чтобы куки с refreshToken отправлялись

    let response = await fetch(url, options);

    if (response.status === 401) { 
        // Если accessToken истек, пробуем обновить
        const refreshResponse = await fetch("http://localhost:8080/api/auth/refresh", {
            method: "POST",
            credentials: "include"
        });

        if (!refreshResponse.ok) {
            console.error("Не удалось обновить токен");
            return response; // Отдаем старый ответ (401)
        }

        const newAccessToken = await refreshResponse.text();
        localStorage.setItem("accessToken", newAccessToken);

        // Повторяем запрос с новым accessToken
        options.headers["Authorization"] = `Bearer ${newAccessToken}`;
        response = await fetch(url, options);
    }

    return response;
};
