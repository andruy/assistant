import { useState, useRef, forwardRef } from 'react'

const Modal = forwardRef(({ name, title, Content, toggleMenu, instagramDate, instagramList }, ref) => {
    const [isDisabled, setIsDisabled] = useState(true)
    const [showCheckmark, setShowCheckmark] = useState(false)
    const [showX, setShowX] = useState(false)
    const spanRef = useRef(null)
    const buttonRef = useRef(null)
    const showingTime = 3000
    const submitButtonText = name === 'Linux' ? 'Refresh' : 'Submit'

    const toggleCheckmark = () => {
        setShowCheckmark(prev => !prev)
    }

    const toggleX = () => {
        setShowX(prev => !prev)
    }

    async function submit() {
        if (spanRef.current && ref.current) {
            setIsDisabled(true)
            spanRef.current.classList.toggle('visually-hidden')

            const response = await ref.current.send()

            if (response.report) {
                spanRef.current.classList.toggle('visually-hidden')
                toggleCheckmark()

                setTimeout(() => {
                    toggleCheckmark()
                }, showingTime)
            } else {
                spanRef.current.classList.toggle('visually-hidden')
                toggleX()

                setTimeout(() => {
                    toggleX()
                    setIsDisabled(false)
                }, showingTime)
            }
        }
    }

    return (
        <div className="modal fade" id={"staticBackdrop" + name} data-bs-backdrop="static" data-bs-keyboard="false" tabIndex="-1" aria-labelledby={"staticBackdropLabel" + name} aria-modal>
            <div className="modal-dialog modal-dialog-centered modal-dialog-scrollable"
                style={{
                    maxWidth: name === 'Linux' && '100%',
                    margin: name === 'Linux' && 'var(--bs-modal-margin)'
                }}
            >
                <div className="modal-content"
                    style={{
                        background: 'rgba(53, 54, 72, 0.2)',
                        backdropFilter: 'blur(10px)',
                        border: '1px solid rgba(255, 255, 255, 0.5)'
                    }}
                >
                    <div className="modal-header">
                        <h3 className="modal-title" id={"staticBackdropLabel" + name}>{title}</h3>
                        <button onClick={() => { toggleMenu() }} type="button" className="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div className="modal-body">
                        <Content isDisabled={setIsDisabled} parentButtonRef={buttonRef} instagramDate={instagramDate} instagramList={instagramList} ref={ref} />
                    </div>
                    <div className="modal-footer">
                        <span ref={spanRef} className="spinner-border text-primary visually-hidden" role="status"></span>
                        {
                            showCheckmark && <div>
                                <svg className="checkmark" viewBox="0 0 52 52">
                                    <path d="M14 27 L22 35 L38 17" />
                                </svg>
                            </div>
                        }
                        {
                            showX && <div>
                                <svg className="xmark" viewBox="0 0 52 52">
                                    <path d="M14 14 L38 38" />
                                    <path d="M38 14 L14 38" />
                                </svg>
                            </div>
                        }
                        <button ref={buttonRef} onClick={submit} type="button" className="btn btn-outline-primary" disabled={isDisabled}>{submitButtonText}</button>
                    </div>
                </div>
            </div>
        </div>
    )
})

export default Modal
