import { useRef, useState } from 'react'
import './App.css'
import MenuButton from './components/MenuButton'
import Modal from './components/Modal'
import Apple from './components/modals/Apple'
import Microsoft from './components/modals/Microsoft'
import Folder from './components/modals/Folder'
import Hourglass from './components/modals/Hourglass'
import Calendar from './components/modals/Calendar'
import Notepad from './components/modals/Notepad'
import Pollo from './components/modals/Pollo'
import Instagram from './components/modals/Instagram'
import InstagramViewer from './components/modals/InstagramViewer'
import Linux from './components/modals/Linux'

function App() {
    const logoRef = useRef(null)
    const instagramDate = useState("")
    const instagramList = useState({})

    function toggleMenu() {
        logoRef.current && logoRef.current.click()
    }

    const modals = [
        {
            name: 'Apple',
            icon: <i className="fa-brands fa-apple"></i>,
            ref: useRef(null),
            component: Apple,
            onClick: () => { toggleMenu() }
        },
        {
            name: 'Microsoft',
            icon: <i className="fa-brands fa-microsoft"></i>,
            ref: useRef(null),
            component: Microsoft,
            onClick: ref => { toggleMenu(); ref.current && ref.current.getDirectories() }
        },
        {
            name: 'Folder',
            icon: <i className="fa-solid fa-folder-closed"></i>,
            ref: useRef(null),
            component: Folder,
            onClick: () => { toggleMenu() }
        },
        {
            name: 'Hourglass',
            icon: <i className="fa-regular fa-hourglass-half"></i>,
            ref: useRef(null),
            component: Hourglass,
            onClick: () => { toggleMenu() }
        },
        {
            name: 'Calendar',
            icon: <i className="fa-regular fa-calendar-days"></i>,
            ref: useRef(null),
            component: Calendar,
            onClick: ref => { toggleMenu(); ref.current && ref.current.getActions() }
        },
        {
            name: 'Notepad',
            icon: <i className="fa-regular fa-clipboard"></i>,
            ref: useRef(null),
            component: Notepad,
            onClick: ref => { toggleMenu(); ref.current && ref.current.gatherTaskList() }
        },
        {
            name: 'Instagram',
            icon: <i className="fa-brands fa-instagram"></i>,
            ref: useRef(),
            component: Instagram,
            onClick: ref => { toggleMenu(); ref.current && ref.current.getListOfDates() }
        },
        {
            name: 'Pollo',
            icon: <i className="fa-solid fa-drumstick-bite"></i>,
            ref: useRef(null),
            component: Pollo,
            onClick: () => { toggleMenu() }
        },
        {
            name: 'Linux',
            icon: <i className="fa-brands fa-linux"></i>,
            ref: useRef(null),
            component: Linux,
            onClick: ref => { toggleMenu(); ref.current && ref.current.send() }
        }
    ]

    const nestedModals = [
        {
            name: 'InstagramViewer',
            icon: <i className="fa-brands fa-instagram"></i>,
            ref: useRef(null),
            component: InstagramViewer
        }
    ]

    return (
        <>
            <div className="position-relative">
                <div className="position-absolute top-0 end-0">
                    <MenuButton ref={logoRef} />
                </div>
            </div>

            <div className="centered">
                <div className="collapse collapse-horizontal" id="collapseExample">
                    <div className="btn-group-vertical">
                        {modals.map((modal, index) => (
                            <button key={index} onClick={() => modal.onClick(modal.ref)} type="button" className="btn btn-dark btn-lg" data-bs-toggle="modal" data-bs-target={"#staticBackdrop" + modal.name}>
                                {modal.icon}
                            </button>
                        ))}
                    </div>
                </div>
            </div>

            {modals.map((modal, index) => (
                <Modal
                    key={index}
                    name={modal.name}
                    title={modal.icon}
                    Content={modal.component}
                    toggleMenu={toggleMenu}
                    instagramDate={instagramDate}
                    instagramList={instagramList}
                    ref={modal.ref}
                />
            ))}

            {nestedModals.map((nestedModal, index) => (
                <Modal
                    key={index}
                    name={nestedModal.name}
                    title={nestedModal.icon}
                    Content={nestedModal.component}
                    toggleMenu={toggleMenu}
                    instagramDate={instagramDate}
                    instagramList={instagramList}
                    ref={nestedModal.ref}
                />
            ))}
        </>
    )
}

export default App
