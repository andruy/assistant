import { useState, useEffect, forwardRef, useImperativeHandle } from "react"

const Instagram = forwardRef(({ isDisabled }, ref) => {
    const [selectValue, setSelectValue] = useState("")
    const [datesList, setDatesList] = useState([])

    async function send() {
        const formData = new FormData()
        formData.append('date', selectValue)
        const queryString = new URLSearchParams(formData).toString()

        const response = await fetch('/listOfAccounts' + `?${queryString}`)

        if (response.ok) {
            const result = await response.json()
            // TODO
            return result
        } else {
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
        <select value={selectValue} onChange={handleSelectChange} className="form-select form-select-lg mb-3" aria-label="Default select example">
            <option value="" disabled hidden>Open for selection</option>
            {datesList.map((date, index) => (
                <option key={index} value={date}>{date}</option>
            ))}
        </select>
    )
})

export default Instagram
