import React, { useState, useEffect, useCallback } from "react";
import { fetchWithAuth } from "../Api/api.js";
import arrow from "./svg/arrow.svg";
import logo from "./svg/logo.svg";
import openSidebar from "./svg/opensidebar.svg"
import closeSidbear from "./svg/closesidebar.svg"
import newChat from "./svg/chat.svg"
import debounce from "lodash/debounce";

import "./Main.css";

export default () => {
    const [userMessage, setUserMessage] = useState("");
    const [chats, setChats] = useState([]);
    const [currentChatId, setCurrentChatId] = useState(null);
    const [error, setError] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [isSidebarOpen, setIsSidebarOpen] = useState(true);

    useEffect(() => {
        loadChats();
    }, []);

    const loadChats = async () => {
        setIsLoading(true);
        try {
            const response = await fetchWithAuth("http://localhost:8080/ai/history", {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                    "X-User-Id": "777@mail.ru",
                    "X-Chat-Id": currentChatId || ""
                },
            });
            if (!response.ok) throw new Error(`Failed to load chats: ${response.status}`);
            const chatsData = await response.json();
            console.log("Received chats:", JSON.stringify(chatsData, null, 2));

            const normalizedChats = chatsData.map(chat => {
                const messages = (chat.messages || []).map(msg => {
                    console.log("\n\n\n\n\n" + msg.fromUser + "\n\n\n\n\n");
                    const sender = msg.fromUser === true ? "user" : "assistant";
                    console.log("Normalizing message:", msg.content, "fromUser:", msg.fromUser, "sender:", sender);
                    return {
                        id: msg.id,
                        userId: msg.userId,
                        chatId: msg.chatId,
                        text: msg.content,
                        sender: sender,
                        createdAt: msg.createdAt
                    };
                });
                return {
                    id: chat.id,
                    userId: chat.userId,
                    createdAt: chat.createdAt,
                    messages: messages
                };
            });

            setChats(normalizedChats);
            if (!normalizedChats.some(chat => chat.id === currentChatId) && normalizedChats.length > 0) {
                setCurrentChatId(normalizedChats[0].id);
            }
        } catch (err) {
            setError(err.message);
            console.error("Error loading chats:", err);
        } finally {
            setIsLoading(false);
        }
    };

    const sendMsgToAi = async () => {
        if (!userMessage.trim() || isLoading || !currentChatId) return;

        setIsLoading(true);
        const newMessage = { sender: "user", text: userMessage };
        console.log("Sending message to chatId: " + currentChatId);

        setChats((prevChats) =>
            prevChats.map((chat) =>
                chat.id === currentChatId
                    ? { ...chat, messages: [...chat.messages, newMessage] }
                    : chat
            )
        );

        try {
            const requestBody = {
                messages: [{ content: userMessage }],
            };

            const response = await fetchWithAuth("http://localhost:8080/ai/duckduckgo", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "X-User-Id": "777@mail.ru",
                    "X-Chat-Id": currentChatId
                },
                body: JSON.stringify(requestBody),
            });

            if (!response.ok) {
                throw new Error(`Ошибка: ${response.status} ${await response.text()}`);
            }

            const data = await response.json();
            const aiMessage = { sender: "assistant", text: data.text };

            setChats((prevChats) =>
                prevChats.map((chat) =>
                    chat.id === currentChatId
                        ? { ...chat, messages: [...chat.messages, aiMessage] }
                        : chat
                )
            );
            await loadChats();
        } catch (err) {
            setError(err.message);
            console.error("Error sending message:", err);
        } finally {
            setIsLoading(false);
            setUserMessage("");
        }
    };

    const createNewChat = async () => {
        if (isLoading) return;
        setIsLoading(true);
        console.log("Attempting to create new chat...");
        try {
            const response = await fetchWithAuth("http://localhost:8080/ai/duckduckgo", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "X-User-Id": "777@mail.ru",
                    "X-Chat-Id": ""
                },
                body: JSON.stringify({ messages: [{ content: "" }] }),
            });
            if (!response.ok) throw new Error(`Failed to create new chat: ${response.status}`);
            const newChatData = await response.json();
            const newChatId = newChatData.chatId;

            const newChat = { id: newChatId, messages: [] };
            setChats((prevChats) => {
                if (prevChats.some(chat => chat.id === newChatId)) return prevChats;
                console.log("Adding new chat:", newChat);
                return [...prevChats, newChat];
            });
            setCurrentChatId(newChatId);
            console.log("New empty chat created with ID: " + newChatId);
        } catch (err) {
            setError("Failed to create new chat: " + err.message);
            console.error("Error creating chat:", err);
        } finally {
            setIsLoading(false);
        }
    };

    const createNewChatDebounced = useCallback(debounce(createNewChat, 300), [isLoading]);

    const switchChat = (chatId) => {
        setCurrentChatId(chatId);
        setUserMessage("");
        loadChats();
    };

    const toggleSidebar = () => {
        setIsSidebarOpen((prev) => !prev);
    };

    const currentChat = chats.find((chat) => chat.id === currentChatId) || { messages: [] };

    return (
        <div className="main">
            <header></header>
            <div className="wrapper">
                <div className={`sidebar ${isSidebarOpen ? "open" : "closed"}`}>
                    <div className="upper">
                        <img src={logo} alt="Logo" onClick={toggleSidebar} className="logo-clickable" />
                        <div className="buttons-container">
                            <img onClick={toggleSidebar} src={isSidebarOpen ? closeSidbear : openSidebar} className="sidebarbutton"/>
                            <img onClick={createNewChatDebounced} src={newChat} disabled={isLoading} className="sidebarbutton" />
                        </div>
                    </div>
                    {isSidebarOpen && (
                        <div className="middle">
                            <h3>Your Chats</h3>
                            <div className="flex-sidebar">
                                {isLoading && chats.length === 0 ? (
                                    <p>Loading chats...</p>
                                ) : chats.length === 0 ? (
                                    <p>No chats yet</p>
                                ) : (
                                    chats.map((chat) => {
                                        console.log("Rendering chat:", chat.id, "Messages:", chat.messages);
                                        return (
                                            <div
                                                key={chat.id}
                                                className={`chat-item ${chat.id === currentChatId ? "active" : ""}`}
                                                onClick={() => switchChat(chat.id)}
                                            >
                                                {chat.messages && chat.messages.length > 0 && chat.messages[0].text
                                                    ? chat.messages[0].text.slice(0, 20) + "..."
                                                    : "Empty Chat"}
                                            </div>
                                        );
                                    })
                                )}
                            </div>
                        </div>
                    )}
                    <div className="footer"></div>
                </div>
                <div className="main-window">
                    <div className="answers" style={{ whiteSpace: "pre-wrap" }}>
                        {currentChat.messages.map((msg, index) => {
                            console.log("Rendering message:", msg.sender, "Text:", msg.text);
                            return (
                                <div key={index} className={msg.sender}>
                                    <p>{msg.text}</p>
                                </div>
                            );
                        })}
                    </div>
                    <div className="chat-wrapper">
                        <div className="message">
                            <div className="button-wrapper">
                                <textarea
                                    className="chat"
                                    value={userMessage}
                                    onChange={(e) => setUserMessage(e.target.value)}
                                    disabled={isLoading || !currentChatId}
                                    placeholder={currentChatId ? "Type your message..." : "Select or create a chat"}
                                />
                                <div className="buttons">
                                    <button
                                        disabled={isLoading || !currentChatId}
                                        className="button"
                                        onClick={sendMsgToAi}
                                    >
                                        <img className="arrow-button" src={arrow} alt="Send" />
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            {error && <div className="error">{error}</div>}
        </div>
    );
};