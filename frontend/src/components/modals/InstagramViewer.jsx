import { useState, useEffect, useRef, forwardRef, useImperativeHandle } from "react"

const InstagramViewer = forwardRef(({ isDisabled, instagramDate, instagramList }, ref) => {
    const idSuffix = 'InstagramViewer'
    const [date] = instagramDate
    const [incoming] = instagramList
    const [instagramAccountsList, setInstagramAccountsList] = useState({})
    const [toBeDeletedList, setToBeDeletedList] = useState({})
    const [topAccordionIsDisabled, setTopAccordionIsDisabled] = useState(true)
    const [bottomAccordionIsDisabled, setBottomAccordionIsDisabled] = useState(false)
    const topButtonRef = useRef(null)
    const bottomButtonRef = useRef(null)
    const [buttonIsDisabled, setButtonIsDisabled] = useState(false)
    const [startingPoint, setStartingPoint] = useState(0)
    const [endingPoint, setEndingPoint] = useState(1)

    async function send() {
        const formData = new FormData()
        formData.append('date', date)
        const queryString = new URLSearchParams(formData).toString()

        const response = await fetch('/deleteAccounts' + `?${queryString}`, {
            method: "DELETE",
            body: JSON.stringify(Object.keys(toBeDeletedList)),
            headers: {
                'Content-Type': 'application/json'
            },
        })

        if (response.ok) {
            const result = await response.json()
            setInstagramAccountsList({})
            setToBeDeletedList({})
            return result
        } else {
            console.error(response)
            return "Something went wrong"
        }
    }

    useImperativeHandle(ref, () => ({
        send,
    }))

    useEffect(() => {
        setInstagramAccountsList(incoming)
        setToBeDeletedList([])
    }, [incoming])

    useEffect(() => {
        if (Object.keys(toBeDeletedList).length === 0) {
            if (topButtonRef.current && !topButtonRef.current.classList.contains('collapsed')) {
                topButtonRef.current.click()
            }
        }

        if (Object.keys(instagramAccountsList).length === 0) {
            if (bottomButtonRef.current && !bottomButtonRef.current.classList.contains('collapsed')) {
                bottomButtonRef.current.click()
            }
        }

        if (Object.keys(toBeDeletedList).length > 0) {
            setTopAccordionIsDisabled(false)
            isDisabled(false)
        } else {
            setTopAccordionIsDisabled(true)
            isDisabled(true)
        }

        if (Object.keys(instagramAccountsList).length > 0) {
            setBottomAccordionIsDisabled(false)
        } else {
            setBottomAccordionIsDisabled(true)
        }
    }, [toBeDeletedList, instagramAccountsList])

    function sortInstagramAccountsList() {
        setInstagramAccountsList(prevList => {
            const sortedList = Object.keys(prevList).sort().reduce((acc, key) => {
                acc[key] = prevList[key]
                return acc
            }, {})
            return sortedList
        })
    }

    function sortToBeDeletedList() {
        setToBeDeletedList(prevList => {
            const sortedList = Object.keys(prevList).sort().reduce((acc, key) => {
                acc[key] = prevList[key]
                return acc
            }, {})
            return sortedList
        })
    }

    const handleFirstChange = event => {
        const num = parseInt(event.target.value)
        setStartingPoint(num - 1)
    }

    const handleSecondChange = event => {
        const num = parseInt(event.target.value)
        setEndingPoint(num - 1)
    }

    function openLinksBatch() {
        for (let i = startingPoint; i < endingPoint; i++) {
            if (i >= Object.values(instagramAccountsList).length) {
                return
            }
            window.open(Object.values(instagramAccountsList)[i], "_blank")
        }
    }

    useEffect(() => {
        setButtonIsDisabled(startingPoint > endingPoint ? true : false)
    }, [startingPoint, endingPoint])

    return (
        <>
            <div className="accordion mb-3" id={"accordionExample1" + idSuffix}>
                <div className="accordion-item">
                    <h2 className="accordion-header">
                        <button ref={topButtonRef} className="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target={"#collapseBox1" + idSuffix} aria-expanded="false" aria-controls={"collapseBox1" + idSuffix} disabled={topAccordionIsDisabled}>
                            {"To be deleted: " + Object.keys(toBeDeletedList).length}
                        </button>
                    </h2>
                    <div id={"collapseBox1" + idSuffix} className="accordion-collapse collapse" data-bs-parent={"#accordionExample1" + idSuffix}>
                        <div className="accordion-body">
                            <ul className="list-group">
                                {
                                    Object.entries(toBeDeletedList).map(([key, value]) => (
                                        <li key={key} className="list-group-item d-flex justify-content-between align-items-center">
                                            <span>{Object.keys(toBeDeletedList).indexOf(key) + 1}</span>
                                            <a href={value} target="_blank">{key}</a>
                                            <button type="button" className="btn btn-outline-info btn-sm ms-2" onClick={() => {
                                                setToBeDeletedList(prevList => {
                                                    const { [key]: removed, ...rest } = prevList
                                                    setInstagramAccountsList(prevDeleted => ({
                                                        ...prevDeleted,
                                                        [key]: [...(prevDeleted[key] || []), value]
                                                    }))
                                                    sortInstagramAccountsList()
                                                    return rest
                                                })
                                            }}>
                                                <i className="fa-solid fa-arrow-rotate-left"></i>
                                            </button>
                                        </li>
                                    ))
                                }
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
            <div className="accordion" id={"accordionExample2" + idSuffix}>
                <div className="accordion-item">
                    <h2 className="accordion-header">
                        <button ref={bottomButtonRef} className="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target={"#collapseBox2" + idSuffix} aria-expanded="false" aria-controls={"collapseBox2" + idSuffix} disabled={bottomAccordionIsDisabled}>
                            {"To be kept: " + Object.keys(instagramAccountsList).length}
                        </button>
                    </h2>
                    <div id={"collapseBox2" + idSuffix} className="accordion-collapse collapse" data-bs-parent={"#accordionExample2" + idSuffix}>
                        <div className="accordion-body">
                            <div className="input-group input-group-sm mb-3">
                                <button onClick={openLinksBatch} type="button" className="btn btn-outline-secondary" disabled={buttonIsDisabled}>
                                    Open links
                                </button>
                                <input onChange={handleFirstChange} className="form-control" type="number" inputMode="numeric" pattern="\d*" min={1} placeholder="Starting index" />
                                <input onChange={handleSecondChange} className="form-control" type="number" inputMode="numeric" pattern="\d*" min={2} placeholder="Ending index" />
                            </div>
                            <ul className="list-group">
                                {
                                    Object.entries(instagramAccountsList).map(([key, value]) => (
                                        <li key={key} className="list-group-item d-flex justify-content-between align-items-center">
                                            <span>{Object.keys(instagramAccountsList).indexOf(key) + 1}</span>
                                            <a href={value} target="_blank">{key}</a>
                                            <button type="button" className="btn btn-outline-warning btn-sm ms-2" onClick={() => {
                                                setInstagramAccountsList(prevList => {
                                                    const { [key]: removed, ...rest } = prevList
                                                    setToBeDeletedList(prevDeleted => ({
                                                        ...prevDeleted,
                                                        [key]: [...(prevDeleted[key] || []), value]
                                                    }))
                                                    sortToBeDeletedList()
                                                    return rest
                                                })
                                            }}>
                                                <i className="fa-solid fa-eraser"></i>
                                            </button>
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

export default InstagramViewer
