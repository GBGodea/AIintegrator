import React, { useState } from "react"

export default function Input() {
    const [text, setText] = useState("");

    return (
        <div>
            <h1>{text}</h1>
            <input className='clazz' onInput={(event) => setText(event.target.value)}></input>
        </div>
    );
}