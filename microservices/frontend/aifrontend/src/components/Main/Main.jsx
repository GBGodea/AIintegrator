import React, { useState, useEffect, useCallback } from "react";
import { fetchWithAuth } from "../Api/api.js";
import arrow from "./svg/arrow.svg";
import logo from "./svg/logo.svg";
import opensidebar from "./svg/opensidebar.svg";
import closesidebar from "./svg/closesidebar.svg";
import newChat from "./svg/chat.svg";
import userIcon from "./svg/user.svg";
import debounce from "lodash/debounce";
import "./Main.css";

export default () => {
    const [userMessage, setUserMessage] = useState("");
    const [chats, setChats] = useState([]);
    const [currentChatId, setCurrentChatId] = useState(null);
    const [error, setError] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [isSidebarOpen, setIsSidebarOpen] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [newEmail, setNewEmail] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [emailError, setEmailError] = useState("");
    const [userProfile, setUserProfile] = useState({ email: "777@mail.ru", password: "" });

    useEffect(() => {
        loadChats();
        loadUserProfile();
    }, []);

    const loadChats = async () => {
        setIsLoading(true);
        try {
            const response = await fetchWithAuth("http://localhost:8080/ai/history", {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                    "X-User-Id": userProfile.email,
                    "X-Chat-Id": currentChatId || ""
                },
            });
            if (!response.ok) throw new Error(`Failed to load chats: ${response.status}`);
            const chatsData = await response.json();

            const normalizedChats = chatsData.map(chat => {
                const messages = (chat.messages || []).map(msg => {
                    const sender = msg.fromUser === true ? "user" : "assistant";
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

    const loadUserProfile = async () => {
        setUserProfile({ email: "777@mail.ru", password: "" });
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
                    "X-User-Id": userProfile.email,
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
        try {
            const response = await fetchWithAuth("http://localhost:8080/ai/duckduckgo", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "X-User-Id": userProfile.email,
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
        } catch (err) {
            setError("Failed to create new chat: " + err.message);
            console.error("Error creating chat:", err);
        } finally {
            setIsLoading(false);
        }
    };

    const createNewChatDebounced = useCallback(debounce(createNewChat, 300), [isLoading, userProfile.email]);

    const switchChat = (chatId) => {
        setCurrentChatId(chatId);
        setUserMessage("");
        loadChats();
    };

    const toggleSidebar = () => {
        setIsSidebarOpen((prev) => !prev);
    };

    const toggleModal = () => {
        setIsModalOpen((prev) => !prev);
        setEmailError("");
        setNewEmail("");
        setNewPassword("");
    };

    const updateUserProfile = async () => {
        if (!newEmail && !newPassword) {
            setEmailError("Введите новый email или пароль");
            return;
        }

        try {
            const requestBody = {};
            if (newEmail) requestBody.email = newEmail;
            if (newPassword) requestBody.password = newPassword;

            const response = await fetchWithAuth("http://localhost:8080/api/users/update", {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    "X-User-Id": userProfile.email
                },
                body: JSON.stringify(requestBody),
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || `Ошибка обновления профиля: ${response.status}`);
            }

            setUserProfile((prev) => ({
                ...prev,
                email: newEmail || prev.email,
                password: newPassword || prev.password
            }));
            document.cookie = "accessToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
            document.cookie = "refreshToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
            window.location.href = "/auth";
        } catch (err) {
            setEmailError(err.message);
            console.error("Error updating profile:", err);
        }
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
                            <button onClick={toggleSidebar} className="toggle-button">
                                <img
                                    src={isSidebarOpen ? closesidebar : opensidebar}
                                    alt={isSidebarOpen ? "Close Sidebar" : "Open Sidebar"}
                                    className="toggle-icon"
                                />
                            </button>
                            <img
                                onClick={createNewChatDebounced}
                                src={newChat}
                                alt="New Chat"
                                className="sidebarbutton"
                                disabled={isLoading}
                            />
                        </div>
                    </div>
                        <div className="middle">
                            <h3>Your Chats</h3>
                            <div className="flex-sidebar">
                                {isLoading && chats.length === 0 ? (
                                    <p>Loading chats...</p>
                                ) : chats.length === 0 ? (
                                    <p>No chats yet</p>
                                ) : (
                                    chats.map((chat) => {
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
                    <div className="footer">
                        <img
                            src={userIcon}
                            alt="User Profile"
                            className="user-icon"
                            onClick={toggleModal}
                        />
                    </div>
                </div>
                <div className="main-window">
                    <div className="answers" style={{ whiteSpace: "pre-wrap" }}>
                        {currentChat.messages.map((msg, index) => {
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
            {isModalOpen && (
                <div className="modal-overlay">
                    <div className="modal">
                        <h2>Update Profile</h2>
                        <div className="modal-content">
                            <p>Current Email: {userProfile.email}</p>
                            <label>
                                New Email:
                                <input
                                    type="email"
                                    value={newEmail}
                                    onChange={(e) => setNewEmail(e.target.value)}
                                    placeholder="Enter new email"
                                />
                            </label>
                            <label>
                                New Password:
                                <input
                                    type="password"
                                    value={newPassword}
                                    onChange={(e) => setNewPassword(e.target.value)}
                                    placeholder="Enter new password"
                                />
                            </label>
                            {emailError && <p className="error-text">{emailError}</p>}
                            <div className="modal-buttons">
                                <button onClick={updateUserProfile}>Save</button>
                                <button onClick={toggleModal}>Cancel</button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};