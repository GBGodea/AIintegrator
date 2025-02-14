import React, { useState } from "react";
import { fetchWithAuth } from "../Api/api.js";
import "./Main.css"

// TODO Сделать AutoUpdate Refresh Token
export default () => {
    const [id, setId] = useState("");
    const [user, setUser] = useState(null);
    const [error, setError] = useState("");

    // const handleSearch = async (e) => {
    //     e.preventDefault(); 

    //     try {
    //         const response = await fetchWithAuth(`http://localhost:8080/api/users/${id}`, {
    //             method: "GET",
    //         });

    //         if (!response.ok) {
    //             throw new Error(`Ошибка: ${response.status} ${await response.text()}`);
    //         }

    //         const data = await response.json();
    //         setUser(data);
    //         setError("");
    //     } catch (err) {
    //         setError(err.message);
    //         setUser(null);
    //     }
    // };

    return (
        <div className="main">
            <header>
                <button>Open</button>
                <logo></logo>
                <newchat></newchat>
            </header>
            <div class="wrapper">
                <div className="sidebar">
                    <div className="upper"></div>
                    <div className="middle"></div>
                    <div className="footer"></div>
                </div>
                <div className="main-window">
                    <div className="answers">

                    </div>
                    <div className="chat-wrapper">
                        <div className="message">
                            <div className="button-wrapper">
                                <textarea className="chat">

                                </textarea>
                                <div className="buttons">
                                    <button>Send</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        // <div>
        //     <form onSubmit={handleSearch} method="get">
        //         <h1>Затестируй поиск пользователя</h1>
        //         <input
        //             type="text"
        //             value={id}
        //             onChange={(e) => setId(e.target.value)}
        //             placeholder="Введите ID пользователя"
        //         />
        //         <button type="submit">Найти</button>
        //     </form>

        //     {error && <p style={{ color: "red" }}>{error}</p>} 

        //     {user && ( 
        //         <div>
        //             <h2>Данные пользователя:</h2>
        //             <pre>{JSON.stringify(user, null, 2)}</pre>
        //         </div>
        //     )}
        // </div>
    );
};