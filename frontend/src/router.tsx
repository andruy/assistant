import { createBrowserRouter } from "react-router"
import Layout from "./components/Layout"
import Home from "./pages/Home"
import About from "./pages/About"
import Apple from "./pages/Apple"
import Microsoft from "./pages/Microsoft"
import Folder from "./pages/Folder"
import Hourglass from "./pages/Hourglass"
import Calendar from "./pages/Calendar"
import Notepad from "./pages/Notepad"
import Instagram from "./pages/Instagram"
import Terminal from "./pages/Terminal"
import Media from "./pages/Media"

export const router = createBrowserRouter([
    {
        path: "/",
        element: <Layout />,
        children: [
            {
                index: true,
                element: <Home />
            },
            {
                path: "about",
                element: <About />
            },
            {
                path: "apple",
                element: <Apple />
            },
            {
                path: "microsoft",
                element: <Microsoft />
            },
            {
                path: "folder",
                element: <Folder />
            },
            {
                path: "hourglass",
                element: <Hourglass />
            },
            {
                path: "calendar",
                element: <Calendar />
            },
            {
                path: "notepad",
                element: <Notepad />
            },
            {
                path: "instagram",
                element: <Instagram />
            },
            {
                path: "terminal",
                element: <Terminal />
            },
            {
                path: "media",
                element: <Media />
            }
        ]
    }
])
