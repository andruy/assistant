import { createBrowserRouter } from "react-router"
import Layout from "./components/Layout"
import Home from "./pages/Home"
import About from "./pages/About"
import Dashboard from "./pages/Dashboard"
import Settings from "./pages/Settings"
import Apple from "./pages/Apple"
import Microsoft from "./pages/Microsoft"
import Folder from "./pages/Folder"
import Hourglass from "./pages/Hourglass"
import Calendar from "./pages/Calendar"
import Notepad from "./pages/Notepad"
import Instagram from "./pages/Instagram"
import Terminal from "./pages/Terminal"

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
                path: "dashboard",
                element: <Dashboard />
            },
            {
                path: "settings",
                element: <Settings />
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
            }
        ]
    }
])
