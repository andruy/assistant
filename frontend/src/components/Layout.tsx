import { useOutlet, useLocation } from "react-router"
import { useState, useEffect, cloneElement } from "react"
import { AnimatePresence } from "framer-motion"
import BackgroundCanvas from "../components/BackgroundCanvas"
import Menu, { pages } from "./Menu"
import PageTransition from "../components/PageTransition"
import { useAuth } from '../context/AuthContext'

export default function Layout() {
  const { isAuthenticated, checkAuth } = useAuth()
  const [menuOpen, setMenuOpen] = useState(false)
  const location = useLocation()

  /* -----------------------------
     Re-validate session on every navigation
  ----------------------------- */
  useEffect(() => {
    checkAuth()
  }, [location.pathname])

  /* -----------------------------
     Page title resolver
  ----------------------------- */
  function resolveTitle(path: string) {
    if (path === "/") return "Home"
    return path.replace("/", "")
  }

  function getPageData(path: string) {
    return pages.find(p => p.path === path)
  }

  const title = resolveTitle(location.pathname)
  const currentPage = getPageData(location.pathname)

  return (
    <div className="min-h-screen flex flex-col text-gray-300">
      <BackgroundCanvas />

      {/* Menu only when authenticated */}
      {isAuthenticated && (
        <Menu
          open={menuOpen}
          onClose={() => setMenuOpen(false)}
        />
      )}

      {/* ================= HEADER ================= */}
      <header
        className="
        flex items-center justify-between
        px-8 py-2
        m-4
        rounded-full
        backdrop-blur-md
        bg-white/10
        shadow-lg
        shadow-purple-500/10
        "
        >
        <h1 className="text-lg font-semibold capitalize flex items-center gap-2">
          {isAuthenticated ?
            <>
              {currentPage && typeof currentPage.name === 'object' && (
                cloneElement(currentPage.name, { size: 32 })
              )}
            </>
            :
            // Nexus logo
            <h2 className="text-2xl font-bold tracking-wider bg-linear-to-r from-blue-400 via-purple-500 to-violet-400 bg-clip-text text-transparent font-[Orbitron]">
              NEXUS
            </h2>
          }
        </h1>

        {isAuthenticated && (
          <button
            onClick={() => setMenuOpen(true)}
            className="
              text-xl
              hover:text-white
              transition-colors
            "
          >
            ☰
          </button>
        )}
      </header>

      {/* ================= MAIN ================= */}
      <main
        className={`
          flex-1
          px-4
          ${!isAuthenticated || title === "Home" ? "flex items-center justify-center" : ""}
        `}
      >
        <AnimatePresence mode="wait">
          <PageTransition key={location.pathname}>
            {useOutlet()}
          </PageTransition>
        </AnimatePresence>
      </main>

      {/* ================= LOWER THIRD ================= */}
      <footer className="h-10 overflow-hidden relative backdrop-blur-md bg-white/5 border-t border-white/10">
        <style>{`
          @keyframes scroll-left {
            from { transform: translateX(0); }
            to { transform: translateX(-50%); }
          }
        `}</style>
        <div
          className="w-max flex items-center h-full whitespace-nowrap text-xs text-gray-400 tracking-widest uppercase"
          style={{ animation: 'scroll-left 25s linear infinite' }}
        >
          {[0, 1].map(i => (
            <span key={i} className="shrink-0 min-w-screen flex items-center justify-around">
              <span>Nexus v1.0</span>
              <span className="text-purple-400/60">◆</span>
              <span>React + Spring Boot</span>
              <span className="text-purple-400/60">◆</span>
              <span>Personal Assistant</span>
              <span className="text-purple-400/60">◆</span>
            </span>
          ))}
        </div>
      </footer>
    </div>
  )
}
