import { useState, useEffect, forwardRef, useImperativeHandle } from "react"

const Instagram = forwardRef(({ isDisabled, instagramDate, instagramList }, ref) => {
    const [selectValue, setSelectValue] = instagramDate
    const [datesList, setDatesList] = useState([])
    const [selected, setSelected] = useState("")
    const [instagramAccountsList, setInstagramAccountsList] = instagramList

    async function send() {
        const formData = new FormData()
        formData.append('date', selectValue)
        const queryString = new URLSearchParams(formData).toString()

        const response = await fetch('/listOfAccounts' + `?${queryString}`)

        if (response.ok) {
            const result = await response.json()
            setInstagramAccountsList(result)
            setSelected(selectValue)
            return { report: "Got the list" }
        } else {
            console.error(response)
            setInstagramAccountsList({})
            setSelected("")
            return "Something went wrong"
        }
    }

    useImperativeHandle(ref, () => ({
        send,
        getListOfDates,
    }))

    async function getListOfDates() {
        const response = await fetch('/listOfDates')
        const data = await response.json()

        setDatesList(data)
    }

    useEffect(() => {
        selectValue !== '' ? isDisabled(false) : isDisabled(true)
    }, [selectValue])

    const handleSelectChange = event => {
        setSelectValue(event.target.value)
    }

    return (
        <div className="input-group">
            <select value={selectValue} onChange={handleSelectChange} className="form-select form-select-lg" aria-label="Default select example">
                <option value="" disabled hidden>Open for selection</option>
                {datesList.map((date, index) => (
                    <option key={index} value={date}>{date}</option>
                ))}
            </select>
            {
                Object.keys(instagramAccountsList).length > 0 && <button data-bs-toggle="modal" data-bs-target={"#staticBackdrop" + "InstagramViewer"} type="button" className="btn btn-dark">
                    {"Open " + selected}
                </button>
            }
        </div>
    )
})

export default Instagram
