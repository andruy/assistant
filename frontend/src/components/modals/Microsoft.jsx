import { useState, useEffect, useRef, forwardRef, useImperativeHandle } from "react"

const Microsoft = forwardRef(({ isDisabled }, ref) => {
    const idSuffix = "Microsoft"
    const [buttonText, setButtonText] = useState("Empty")
    const [inputValue, setInputValue] = useState("")
    const [selectValue, setSelectValue] = useState("")
    const [linksObject, setLinksObject] = useState({})
    const [plusIsDisabled, setPlusIsDisabled] = useState(true)
    const [accordionIsDisabled, setAccordionIsDisabled] = useState(true)
    const [directories, setDirectories] = useState([])
    const buttonRef = useRef(null)

    async function send() {
        const response = await fetch('/yt', {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(linksObject)
        })
        if (response.ok) {
            const result = await response.json()
            console.log(result.report)
            setLinksObject({})
            return result
        } else {
            console.error(response)
            console.log(linksObject)
            return "Something went wrong"
        }
    }

    useImperativeHandle(ref, () => ({
        send,
        getDirectories,
    }))

    async function getDirectories() {
        const response = await fetch('/ytd')
        let data = await response.json()

        data.sort((a, b) => a.name.localeCompare(b.name))
        setDirectories(data.map(item => item.name))
    }

    useEffect(() => {
        setPlusIsDisabled(inputValue.trim() === '' || selectValue === '' ? true : false)
    }, [inputValue, selectValue])

    useEffect(() => {
        setButtonText(Object.keys(linksObject).length > 0 ? `Total links: ${Object.keys(linksObject).length}, ${Object.values(linksObject).reduce((acc, arr) => acc + arr.length, 0)}` : 'Empty')
        if (Object.keys(linksObject).length === 0) {
            if (buttonRef.current && !buttonRef.current.classList.contains('collapsed')) {
                buttonRef.current.click()
            }
        }

        if (Object.keys(linksObject).length > 0) {
            setAccordionIsDisabled(false)
            isDisabled(false)
        } else {
            setAccordionIsDisabled(true)
            isDisabled(true)
        }
    }, [linksObject])

    const handleSelectChange = event => {
        setSelectValue(event.target.value)
    }

    const handleInputChange = event => {
        setInputValue(event.target.value)
    }

    const handleAddLink = () => {
        if (inputValue.trim() !== '' && selectValue !== '') {
            setLinksObject(prevLinksObject => ({
                ...prevLinksObject,
                [selectValue]: [...(prevLinksObject[selectValue] || []), inputValue]
            }))
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
            <select value={selectValue} onChange={handleSelectChange} className="form-select form-select-lg mb-3" aria-label="Default select example">
                <option value="" disabled hidden>Choose directory...</option>
                {directories.map((directory, index) => (
                    <option key={index} value={directory}>{directory}</option>
                ))}
            </select>
            <div className="input-group mb-3">
                <input value={inputValue} onKeyDown={handleKeyDown} onChange={handleInputChange} className="form-control form-control-lg" type="text" placeholder="Enter links..." />
                <button onClick={handleAddLink} type="button" className="btn btn-dark" disabled={plusIsDisabled}>
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
                                {
                                    Object.keys(linksObject).map((key, index) => (
                                        <li key={index} className="list-group-item">
                                            <h5 className="card-title">{key}</h5>
                                            <ul className="list-group list-group-flush">
                                                {
                                                    linksObject[key].map((link, linkIndex) => (
                                                        <li key={linkIndex} className="list-group-item d-flex justify-content-between align-items-center">
                                                            {link}
                                                            <button type="button" className="btn btn-outline-danger btn-sm ms-2" onClick={() => {
                                                                setLinksObject(prevLinksObject => {
                                                                    const updatedLinks = { ...prevLinksObject }
                                                                    updatedLinks[key] = updatedLinks[key].filter((_, i) => i !== linkIndex)
                                                                    if (updatedLinks[key].length === 0) {
                                                                        delete updatedLinks[key]
                                                                    }
                                                                    return updatedLinks
                                                                })
                                                            }}>
                                                                <i className="fa-solid fa-trash"></i>
                                                            </button>
                                                        </li>
                                                    ))
                                                }
                                            </ul>
                                        </li>
                                    ))
                                }
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </>
    )
})

export default Microsoft
