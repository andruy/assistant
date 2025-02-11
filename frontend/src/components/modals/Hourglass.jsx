import { useState, useEffect, useRef, forwardRef, useImperativeHandle } from "react"

const Hourglass = forwardRef(({ isDisabled }, ref) => {
    const idSuffix = "Hourglass"
    const [buttonText, setButtonText] = useState("Empty")
    const [inputValue, setInputValue] = useState("")
    const [tasksArray, setTasksArray] = useState([])
    const [plusIsDisabled, setPlusIsDisabled] = useState(true)
    const [accordionIsDisabled, setAccordionIsDisabled] = useState(true)
    const buttonRef = useRef(null)
    const inputRef = useRef(null)

    async function send() {
        const currentTime = Date.now()
        const taskList = [...tasksArray]
        taskList.forEach(task => {
            task.timeframe += currentTime
        })

        const response = await fetch('/emailtask', {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(taskList)
        })
        if (response.ok) {
            const result = await response.json()
            console.log(result.report)
            setTasksArray([])
            return result
        } else {
            console.error(response)
            console.log(taskList)
            return "Something went wrong"
        }
    }

    useImperativeHandle(ref, () => ({
        send,
    }))

    const handleInputChange = event => {
        setInputValue(event.target.value)
    }

    const handleAddTask = () => {
        if (inputValue.trim() !== '' && Number(inputValue) > 0) {
            const newItem = {
                timeframe: minutesToMilliseconds(Number(inputValue)),
                email: {
                    to: "andruycira@icloud.com",
                    subject: inputRef.current && inputRef.current.checked ? "Turn AC off" : "Turn AC on",
                    body: "Lorem ipsum"
                }
            }

            setTasksArray([...tasksArray, tasksArray.length > 0 ? newItemUpdated(newItem) : newItem])
            setInputValue('')
        }
    }

    function newItemUpdated(newTask) {
        if (tasksArray[tasksArray.length - 1].email.subject === "Turn AC on" && newTask.email.subject === "Turn AC on") {
            newTask.timeframe += minutesToMilliseconds(120)
        }

        newTask.timeframe += tasksArray[tasksArray.length - 1].timeframe

        return newTask
    }

    const handleKeyDown = event => {
        if (event.key === 'Enter') {
            handleAddTask()
        }
    }

    function minutesToMilliseconds(minutes) {
        return minutes * 60000
    }

    useEffect(() => {
        setPlusIsDisabled(inputValue.trim() !== '' && Number(inputValue) > 0 ? false : true)
    }, [inputValue])

    useEffect(() => {
        setButtonText(tasksArray.length > 0 ? `Total tasks: ${tasksArray.length}` : 'Empty')
        if (tasksArray.length === 0) {
            if (buttonRef.current && !buttonRef.current.classList.contains('collapsed')) {
                buttonRef.current.click()
            }
        }
        setAccordionIsDisabled(tasksArray.length > 0 ? false : true)

        tasksArray.length > 0 ? isDisabled(false) : isDisabled(true)
    }, [tasksArray])

    return (
        <>
            <div className="d-flex justify-content-evenly mb-3">
                <input type="radio" className="btn-check" name="options-outlined" id={"success-outlined" + idSuffix} autoComplete="off" />
                <label className="btn btn-outline-success" htmlFor={"success-outlined" + idSuffix}>
                    <i className="fa-solid fa-play"></i>
                </label>
                <input ref={inputRef} type="radio" className="btn-check" name="options-outlined" id={"danger-outlined" + idSuffix} autoComplete="off" defaultChecked />
                <label className="btn btn-outline-danger" htmlFor={"danger-outlined" + idSuffix}>
                    <i className="fa-solid fa-stop"></i>
                </label>
            </div>
            <div className="input-group mb-3">
                <input value={inputValue} onKeyDown={handleKeyDown} onChange={handleInputChange} className="form-control form-control-lg" type="number" inputMode="numeric" pattern="\d*" min={1} placeholder="How long? (minutes)" />
                <button onClick={handleAddTask} type="button" className="btn btn-outline-secondary" disabled={plusIsDisabled}>
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
                                {tasksArray.map((task, index) => (
                                    <li key={index} className="list-group-item d-flex justify-content-between align-items-center">
                                        <div style={{ overflowX: 'auto', whiteSpace: 'nowrap', flex: 1 }}>
                                            {
                                                task.email.subject  === "Turn AC on" ?
                                                    `AC will start ${new Date(task.timeframe + Date.now()).toLocaleString()}`
                                                    :
                                                    `AC will stop ${new Date(task.timeframe + Date.now()).toLocaleString()}`
                                            }
                                        </div>
                                        <button onClick={() => {
                                            const newTasksArray = tasksArray.filter((_, i) => i !== index)
                                            setTasksArray(newTasksArray)
                                        }} className="btn btn-outline-danger btn-sm ms-2">
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

export default Hourglass
