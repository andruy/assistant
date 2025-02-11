import { useState, useEffect, useRef, forwardRef, useImperativeHandle } from "react"

const Apple = forwardRef(({ isDisabled }, ref) => {
    const idSuffix = "Apple"
    const [buttonText, setButtonText] = useState("Empty")
    const [inputValue, setInputValue] = useState("")
    const [linksArray, setLinksArray] = useState([])
    const [plusIsDisabled, setPlusIsDisabled] = useState(true)
    const [accordionIsDisabled, setAccordionIsDisabled] = useState(true)
    const buttonRef = useRef(null)

    async function send() {
        const data = {
            links: linksArray
        }

        const response = await fetch('/yte', {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(data)
        })
        if (response.ok) {
            const result = await response.json()
            console.log(result.report)
            setLinksArray([])
            return result
        } else {
            console.error(response)
            console.log(data)
            return "Something went wrong"
        }
    }

    useImperativeHandle(ref, () => ({
        send,
    }))

    useEffect(() => {
        setPlusIsDisabled(inputValue.trim() === '' ? true : false)
    }, [inputValue])

    useEffect(() => {
        setButtonText(linksArray.length > 0 ? `Total links: ${linksArray.length}` : 'Empty')
        if (linksArray.length === 0) {
            if (buttonRef.current && !buttonRef.current.classList.contains('collapsed')) {
                buttonRef.current.click()
            }
        }

        if (linksArray.length > 0) {
            setAccordionIsDisabled(false)
            isDisabled(false)
        } else {
            setAccordionIsDisabled(true)
            isDisabled(true)
        }
    }, [linksArray])

    const handleChange = event => {
        setInputValue(event.target.value)
    }

    const handleAddLink = () => {
        if (inputValue.trim() !== '') {
            setLinksArray([...linksArray, inputValue])
            setInputValue('')
        }
    }

    const handleKeyDown = event => {
        if (event.key === 'Enter') {
            handleAddLink()
        }
    }

    return (
        <>
            <div className="input-group mb-3">
                <input value={inputValue} onKeyDown={handleKeyDown} onChange={handleChange} className="form-control form-control-lg" type="text" placeholder="Enter links..." />
                <button onClick={handleAddLink} type="button" className="btn btn-outline-secondary" disabled={plusIsDisabled}>
                    <i className="fa-solid fa-plus"></i>
                </button>
            </div>
            <div className="accordion" id={"accordionExample" + idSuffix}>
                <div className="accordion-item">
                    <h2 className="accordion-header">
                        <button ref={buttonRef} className="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target={"#collapseBox" + idSuffix} aria-expanded="false" aria-controls={"collapseBox" + idSuffix} disabled={accordionIsDisabled}>
                            {buttonText}
                        </button>
                    </h2>
                    <div id={"collapseBox" + idSuffix} className="accordion-collapse collapse" data-bs-parent={"#accordionExample" + idSuffix}>
                        <div className="accordion-body">
                            <ul className="list-group list-group-flush">
                                {linksArray.map((link, index) => (
                                    <li key={index} className="list-group-item d-flex justify-content-between align-items-center">
                                        <div style={{ overflowX: 'auto', whiteSpace: 'nowrap', flex: 1 }}>
                                            {link}
                                        </div>
                                        <button onClick={() => setLinksArray(linksArray.filter((_, i) => i !== index))} className="btn btn-outline-danger btn-sm ms-2">
                                            <i className="fa-solid fa-trash"></i>
                                        </button>
                                    </li>
                                ))}
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </>
    )
})

export default Apple
