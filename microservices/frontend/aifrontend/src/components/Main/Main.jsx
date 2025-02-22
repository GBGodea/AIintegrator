import React, { useState } from "react";
import { fetchWithAuth } from "../Api/api.js";
import Arrow from "./svg/arrow.svg"
import "./Main.css";

export default () => {
    const [userMessage, setUserMessage] = useState("");
    const [messages, setMessages] = useState([]);
    const [error, setError] = useState("");
    const [isLoading, setIsLoading] = useState(false);

    const sendMsgToAi = async () => {
        if (!userMessage.trim() || isLoading) return;

        setIsLoading(true);
        if (!userMessage.trim()) return;

        setMessages((prevMessages) => [
            ...prevMessages,
            { sender: "user", text: userMessage }
        ]);

        try {
            const requestBody = {
                messages: [{ content: userMessage }]
            };

            const response = await fetchWithAuth("http://localhost:8080/ai/duckduckgo", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(requestBody)
            });

            if (!response.ok) {
                throw new Error(`Ошибка: ${response.status} ${await response.text()}`);
            }

            const data = await response.json();
            console.log("Response from backend:", data);

            setMessages((prevMessages) => [
                ...prevMessages,
                { sender: "assistant", text: data.text }
            ]);
        } catch (err) {
            setError(err.message);
        } finally {
            setIsLoading(false);
            setUserMessage("");
        }
    };

    return (
        <div className="main">
            <header>
                <button>Open</button>
                <logo></logo>
                <newchat></newchat>
            </header>
            <div className="wrapper">
                <div className="sidebar">
                    <div className="upper"></div>
                    <div className="middle"></div>
                    <div className="footer"></div>
                </div>
                <div className="main-window">
                    <div className="answers" style={{ whiteSpace: "pre-wrap" }}>
                        {messages.map((msg, index) => (
                            <div key={index} className={msg.sender}>
                                <p>{msg.text}</p>
                            </div>
                        ))}
                    </div>
                    <div className="chat-wrapper">
                        <div className="message">
                            <div className="button-wrapper">
                                <textarea
                                    className="chat"
                                    value={userMessage}
                                    onChange={(e) => setUserMessage(e.target.value)}
                                    disabled={isLoading} // Блокируем textarea во время загрузки
                                />
                                <div className="buttons">
                                    <button disabled={isLoading} className="button" onClick={sendMsgToAi}><img className="arrow-button" src={Arrow} /></button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};
