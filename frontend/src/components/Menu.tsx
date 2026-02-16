import { Link, useLocation } from "react-router"
import { motion, AnimatePresence } from "framer-motion"
import { useRef, useEffect, useCallback, useState } from "react"
import { FaApple, FaRegClipboard, FaFolder, FaHourglassHalf, FaHouse, FaInstagram, FaMicrosoft, FaPlay, FaRegCalendarDays, FaTerminal } from "react-icons/fa6"

type Props = {
  open: boolean
  onClose: () => void
}

export const pages = [
  { name: <FaHouse size={64} />, path: "/" },
  { name: <FaApple size={64} />, path: "/apple" },
  { name: <FaMicrosoft size={64} />, path: "/microsoft" },
  { name: <FaFolder size={64} />, path: "/folder" },
  { name: <FaInstagram size={64} />, path: "/instagram" },
  { name: <FaPlay size={64} />, path: "/media" },
  { name: <FaHourglassHalf size={64} />, path: "/hourglass" },
  { name: <FaRegCalendarDays size={64} />, path: "/calendar" },
  { name: <FaRegClipboard size={64} />, path: "/notepad" },
  { name: <FaTerminal size={64} />, path: "/terminal" },
]

const btnClass = `
  w-[100px] h-[100px]
  rounded-2xl
  bg-white/10 backdrop-blur-md
  border border-white/20
  flex items-center justify-center
  text-lg text-gray-200
`

const arrowClass = `
  w-10 h-10 rounded-full
  bg-white/10 backdrop-blur-md border border-white/20
  flex items-center justify-center
  text-gray-200 text-lg
  transition-opacity
`

export default function Menu({ open, onClose }: Props) {
  const location = useLocation()
  const scrollRef = useRef<HTMLDivElement>(null)
  const indexRef = useRef(0)
  const isScrolling = useRef(false)
  const [currentIndex, setCurrentIndex] = useState(0)

  const scrollToIndex = useCallback((index: number, smooth = true) => {
    const container = scrollRef.current
    if (!container) return
    const clamped = Math.max(0, Math.min(index, pages.length - 1))
    if (smooth && (isScrolling.current || clamped === indexRef.current)) return
    indexRef.current = clamped
    setCurrentIndex(clamped)
    if (smooth) {
      isScrolling.current = true
      container.scrollTo({ top: clamped * container.clientHeight, behavior: "smooth" })
      setTimeout(() => { isScrolling.current = false }, 450)
    } else {
      container.scrollTo({ top: clamped * container.clientHeight, behavior: "instant" })
    }
  }, [])

  // On open: jump to the current page's button
  useEffect(() => {
    if (!open) return
    const idx = pages.findIndex(p => p.path === location.pathname)
    const startIndex = idx >= 0 ? idx : 0
    // Use rAF to ensure the scroll container is rendered and sized
    requestAnimationFrame(() => scrollToIndex(startIndex, false))
  }, [open, location.pathname, scrollToIndex])

  // Wheel
  useEffect(() => {
    if (!open) return
    const container = scrollRef.current
    if (!container) return
    function onWheel(e: WheelEvent) {
      e.preventDefault()
      if (e.deltaY > 0) scrollToIndex(indexRef.current + 1)
      else if (e.deltaY < 0) scrollToIndex(indexRef.current - 1)
    }
    container.addEventListener("wheel", onWheel, { passive: false })
    return () => container.removeEventListener("wheel", onWheel)
  }, [open, scrollToIndex])

  // Touch
  useEffect(() => {
    if (!open) return
    const container = scrollRef.current
    if (!container) return
    let startY = 0
    function onTouchStart(e: TouchEvent) { startY = e.touches[0].clientY }
    function onTouchEnd(e: TouchEvent) {
      const delta = startY - e.changedTouches[0].clientY
      if (Math.abs(delta) > 50) {
        if (delta > 0) scrollToIndex(indexRef.current + 1)
        else scrollToIndex(indexRef.current - 1)
      }
    }
    container.addEventListener("touchstart", onTouchStart, { passive: true })
    container.addEventListener("touchend", onTouchEnd, { passive: true })
    return () => {
      container.removeEventListener("touchstart", onTouchStart)
      container.removeEventListener("touchend", onTouchEnd)
    }
  }, [open, scrollToIndex])

  // Keyboard
  useEffect(() => {
    if (!open) return
    function onKeyDown(e: KeyboardEvent) {
      if (e.key === "ArrowDown") {
        e.preventDefault()
        scrollToIndex(indexRef.current + 1)
      } else if (e.key === "ArrowUp") {
        e.preventDefault()
        scrollToIndex(indexRef.current - 1)
      } else if (e.key === "Escape") {
        onClose()
      }
    }
    window.addEventListener("keydown", onKeyDown)
    return () => window.removeEventListener("keydown", onKeyDown)
  }, [open, scrollToIndex, onClose])

  const atFirst = currentIndex === 0
  const atLast = currentIndex === pages.length - 1

  return (
    <AnimatePresence>
      {open && (
        <motion.div
          className="fixed inset-0 bg-black/70 backdrop-blur-lg z-50"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          onClick={onClose}
        >
          {/* Close button */}
          <button
            onClick={onClose}
            className="
              fixed top-4 right-4 z-10
              w-10 h-10 rounded-full
              bg-white/10 backdrop-blur-md border border-white/20
              flex items-center justify-center
              text-gray-200 text-xl
            "
          >
            &times;
          </button>

          {/* Up arrow */}
          <button
            onClick={(e) => { e.stopPropagation(); scrollToIndex(indexRef.current - 1) }}
            className={`fixed top-[calc(50%-106px)] left-1/2 -translate-x-1/2 z-10 ${arrowClass} ${atFirst ? "opacity-0 pointer-events-none" : "opacity-100"}`}
          >
            &#x25B2;
          </button>

          {/* Down arrow */}
          <button
            onClick={(e) => { e.stopPropagation(); scrollToIndex(indexRef.current + 1) }}
            className={`fixed top-[calc(50%+66px)] left-1/2 -translate-x-1/2 z-10 ${arrowClass} ${atLast ? "opacity-0 pointer-events-none" : "opacity-100"}`}
          >
            &#x25BC;
          </button>

          <div
            ref={scrollRef}
            onClick={e => e.stopPropagation()}
            className="
              h-full overflow-y-auto
              [scrollbar-width:none] [&::-webkit-scrollbar]:hidden
            "
            style={{ touchAction: "none" }}
          >
            {pages.map((p, i) => (
              <div
                key={p.path}
                className="h-screen flex items-center justify-center"
              >
                <motion.div
                  initial={{ scale: 0.8, opacity: 0 }}
                  whileInView={{ scale: 1, opacity: 1 }}
                  viewport={{ amount: 0.5 }}
                  transition={{ duration: 0.3, delay: i === currentIndex && !isScrolling.current ? 0.15 : 0 }}
                >
                  <Link to={p.path} onClick={onClose} className={btnClass}>
                    {p.name}
                  </Link>
                </motion.div>
              </div>
            ))}
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  )
}
