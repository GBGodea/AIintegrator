import React from "react"
import { Navigate } from "react-router-dom"

export default ({ children }) => {
    const accessToken = localStorage.getItem("accessToken");

    if(!accessToken) {
        return <Navigate to="/auth" replace />
    }

    return children;
}