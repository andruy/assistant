import { useState, useEffect, forwardRef, useImperativeHandle } from "react"

const Folder = forwardRef(({ isDisabled }, ref) => {
    const [inputValue, setInputValue] = useState("")
    const [folderName, setFolderName] = useState("{}")
    const [plusIsDisabled, setPlusIsDisabled] = useState(true)

    async function send() {
        const formData = new FormData()
        formData.append('name', folderName)
        const queryString = new URLSearchParams(formData).toString()
        const response = await fetch('/newDirectory' + `?${queryString}`, {
            method: "POST"
        })
        if (response.ok) {
            const result = await response.json()
            console.log(result.report)
            setFolderName("{}")
            return result
        } else {
            console.error(response)
            console.log(folderName)
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
        folderName === '{}' ? isDisabled(true) : isDisabled(false)
    }, [folderName])

    const handleChange = event => {
        setInputValue(event.target.value)
    }

    function handleAddFolder() {
        if (inputValue.trim() !== '{}') {
            setFolderName(inputValue)
            setInputValue('')
        }
    }

    const handleKeyDown = event => {
        if (event.key === 'Enter') {
            handleAddFolder()
        }
    }

    return (
        <>
            <div className="input-group mb-3">
                <input value={inputValue} onKeyDown={handleKeyDown} onChange={handleChange} className="form-control form-control-lg" type="text" placeholder="Name of the folder?" />
                <button onClick={handleAddFolder} type="button" className="btn btn-dark" disabled={plusIsDisabled}>
                    <i class="bi bi-plus-lg"></i>
                </button>
            </div>
            <ul className="list-group list-group-flush">
                <li className="list-group-item d-flex justify-content-between align-items-center">
                    <div style={{ overflowX: 'auto', whiteSpace: 'nowrap', flex: 1 }}>
                        <span style={{color: "#6c757d"}}>/the/new/folder/</span>{folderName}
                    </div>
                    <button type="button" className="btn btn-outline-danger btn-sm ms-2" onClick={() => setFolderName("{}")} disabled={folderName === '{}'}>
                        <i class="bi bi-arrow-counterclockwise"></i>
                    </button>
                </li>
            </ul>
        </>
    )
})

export default Folder
