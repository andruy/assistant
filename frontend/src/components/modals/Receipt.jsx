import { useState, useEffect, forwardRef, useImperativeHandle } from "react"

const Receipt = forwardRef(({ isDisabled, parentButtonRef }, ref) => {
    const [inputValue, setInputValue] = useState("")
    const [mealSelectValue, setMealSelectValue] = useState("")
    const [typeSelectValue, setTypeSelectValue] = useState("")

    async function send() {
        const content = {
            code: inputValue,
            meal: mealSelectValue,
            visit: typeSelectValue
        }

        const response = await fetch('/Receipt', {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(content)
        })
        if (response.ok) {
            const result = await response.json()
            console.log(result.report)
            setInputValue("")
            return result
        } else {
            console.error(response)
            console.log(content)
            return "Something went wrong"
        }
    }

    useImperativeHandle(ref, () => ({
        send,
    }))

    useEffect(() => {
        inputValue.length === 16 && mealSelectValue && typeSelectValue ? isDisabled(false) : isDisabled(true)
    }, [inputValue, mealSelectValue, typeSelectValue])

    const handleChange = event => {
        setInputValue(event.target.value)
    }

    const handleKeyDown = event => {
        if (event.key === 'Enter' && parentButtonRef.current) {
            parentButtonRef.current.click()
        }
    }

    const handleMealSelectChange = event => {
        setMealSelectValue(event.target.value)
    }

    const handleTypeSelectChange = event => {
        setTypeSelectValue(event.target.value)
    }

    return (
        <>
            <input value={inputValue} onKeyDown={handleKeyDown} onChange={handleChange} className="form-control form-control-lg mb-3" type="number" inputMode="numeric" pattern="\d*" placeholder="Enter code" />
            <select value={mealSelectValue} onChange={handleMealSelectChange} className="form-select form-select-lg mb-3" aria-label="Example select with button addon">
                <option value="" disabled hidden>Choose meal...</option>
                <option value="Chicken Platter">Chicken Platter</option>
                <option value="TropiChop">TropiChop</option>
                <option value="Whole Chicken Family Meal">Whole Chicken Family Meal</option>
                <option value="Wrap/Sandwich">Wrap/Sandwich</option>
                <option value="Salad">Salad</option>
                <option value="Mojo Pork Platter">Mojo Pork Platter</option>
                <option value="Master Trio Platter">Master Trio Platter</option>
            </select>
            <select value={typeSelectValue} onChange={handleTypeSelectChange} className="form-select form-select-lg" aria-label="Example select with button addon">
                <option value="" disabled hidden>Choose visit...</option>
                <option value="Dine-In">Dine-In</option>
                <option value="To-go">To-go</option>
            </select>
        </>
    )
})

export default Receipt
