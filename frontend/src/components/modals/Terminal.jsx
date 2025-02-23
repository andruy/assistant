import { useState, useEffect, forwardRef, useImperativeHandle } from "react"

const Terminal = forwardRef(({ isDisabled }, ref) => {
    const [text, setText] = useState('Did not find any logs')

    async function send() {
        const response = await fetch('/logReader')
        
        if (response.ok) {
            const result = await response.json()
            setText(result.report)
            isDisabled(false)
            return result
        } else {
            return "Something went wrong"
        }
    }

    useImperativeHandle(ref, () => ({
        send,
    }))

    useEffect(() => {
        isDisabled(false)
    }, [])

    const styles = {
        fontFamily: "'Courier New', Courier, monospace",
        whiteSpace: "pre",
        overflowX: "auto"
    }

    return (
        <div className="card">
            <div className="card-body" style={styles}>
                {text}
            </div>
        </div>
    )
})

export default Terminal
