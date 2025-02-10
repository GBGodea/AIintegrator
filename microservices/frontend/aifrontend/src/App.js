import React, { use, useState } from "react"
import './App.css';

import RegisterForm from './components/RegisterForm.jsx';
import AuthForm from "./components/AuthForm.jsx";
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import Main from "./components/Main.jsx";


function App() {
  return (
    <div className="App">
      {/* <RegisterForm /> */}
      <Router>
            <Routes>
                <Route path="/" element={<RegisterForm />} />
                <Route path="/auth" element={<AuthForm />} />
                <Route path="/main" element={<Main />} />
            </Routes>
        </Router>
    </div>
  );
}

export default App;
