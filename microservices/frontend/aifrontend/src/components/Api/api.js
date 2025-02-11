// api.js
export const fetchWithAuth = async (url, options = {}) => {
    // const [email, setEmail] = useState("");
    // const [password, setPassword] = useState("");
    let accessToken = localStorage.getItem("accessToken");

    if (!options.headers) {
        options.headers = {};
    }

    // const user = { email, password };
    options.headers["Authorization"] = `Bearer ${accessToken}`;
    options.credentials = "include"; // Чтобы куки с refreshToken отправлялись

    console.log(accessToken)
    console.log(options.headers)

    let response = await fetch(url, options);
    // console.log(response)

    if (response.status === 401 || response.status === 403) { 
        // Если accessToken истек, пробуем обновить
        const refreshResponse = await fetch("http://localhost:8080/api/auth/refresh", {
            method: "POST",
            credentials: "include",
            // body: JSON.stringify(user)
        });

        console.log(refreshResponse)

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
