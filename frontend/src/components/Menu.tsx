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

const screenVariants = {
  enter: (dir: number) => ({ y: dir === 0 ? 0 : dir > 0 ? "100%" : "-100%", opacity: 0 }),
  center: { y: 0, opacity: 1 },
  exit: (dir: number) => ({ y: dir > 0 ? "-100%" : "100%", opacity: 0 }),
}

export default function Menu({ open, onClose }: Props) {
  const location = useLocation()
  const overlayRef = useRef<HTMLDivElement>(null)
  const screenRef = useRef<HTMLDivElement>(null)
  const scrollbarRef = useRef<HTMLDivElement>(null)
  const watchBtnRef = useRef<HTMLDivElement>(null)
  const indexRef = useRef(0)
  const cooldown = useRef(false)
  const [currentIndex, setCurrentIndex] = useState(0)
  const [direction, setDirection] = useState(0)


  const navigate = useCallback((newIndex: number) => {
    const clamped = Math.max(0, Math.min(newIndex, pages.length - 1))
    if (clamped === indexRef.current || cooldown.current) return
    setDirection(clamped > indexRef.current ? 1 : -1)
    indexRef.current = clamped
    setCurrentIndex(clamped)
    cooldown.current = true
    setTimeout(() => { cooldown.current = false }, 350)
  }, [])

  // On open: jump to the current page & lock body scroll
  useEffect(() => {
    if (!open) return
    const idx = pages.findIndex(p => p.path === location.pathname)
    const startIndex = idx >= 0 ? idx : 0
    indexRef.current = startIndex
    setCurrentIndex(startIndex)
    setDirection(0)
    document.body.style.overflow = "hidden"
    return () => { document.body.style.overflow = "" }
  }, [open, location.pathname])

  // Wheel — navigate every ~3 scroll lines (3 × 100px deltaY)
  useEffect(() => {
    if (!open) return
    const el = screenRef.current
    if (!el) return
    let accumulated = 0
    function onWheel(e: WheelEvent) {
      e.preventDefault()
      accumulated += e.deltaY
      if (Math.abs(accumulated) < 10) return
      if (accumulated > 0) navigate(indexRef.current + 1)
      else navigate(indexRef.current - 1)
      accumulated = 0
    }
    el.addEventListener("wheel", onWheel, { passive: false })
    return () => el.removeEventListener("wheel", onWheel)
  }, [open, navigate])

  // Touch — prevent background scroll & navigate on swipe
  useEffect(() => {
    if (!open) return
    const el = screenRef.current
    if (!el) return
    let startY = 0
    function onTouchStart(e: TouchEvent) {
      startY = e.touches[0].clientY
    }
    function onTouchMove(e: TouchEvent) {
      e.preventDefault()
    }
    function onTouchEnd(e: TouchEvent) {
      const delta = startY - e.changedTouches[0].clientY
      if (Math.abs(delta) > 50) {
        if (delta > 0) navigate(indexRef.current + 1)
        else navigate(indexRef.current - 1)
      }
    }
    el.addEventListener("touchstart", onTouchStart, { passive: false })
    el.addEventListener("touchmove", onTouchMove, { passive: false })
    el.addEventListener("touchend", onTouchEnd, { passive: true })
    return () => {
      el.removeEventListener("touchstart", onTouchStart)
      el.removeEventListener("touchmove", onTouchMove)
      el.removeEventListener("touchend", onTouchEnd)
    }
  }, [open, navigate])

  // Scrollbar drag
  useEffect(() => {
    if (!open) return
    const thumb = scrollbarRef.current
    const container = watchBtnRef.current
    if (!thumb || !container) return

    const fontSize = 7
    const trackTop = 9.3 * fontSize
    const trackHeight = 16.7 * fontSize

    function calcIndex(clientY: number) {
      const rect = container!.getBoundingClientRect()
      const ratio = Math.max(0, Math.min(1, (clientY - rect.top - trackTop) / trackHeight))
      return Math.round(ratio * (pages.length - 1))
    }

    function onPointerDown(e: PointerEvent) {
      e.preventDefault()
      e.stopPropagation()
      function onPointerMove(e: PointerEvent) {
        const newIndex = calcIndex(e.clientY)
        if (newIndex === indexRef.current) return
        setDirection(newIndex > indexRef.current ? 1 : -1)
        indexRef.current = newIndex
        setCurrentIndex(newIndex)
      }
      function onPointerUp() {
        window.removeEventListener("pointermove", onPointerMove)
        window.removeEventListener("pointerup", onPointerUp)
      }
      window.addEventListener("pointermove", onPointerMove)
      window.addEventListener("pointerup", onPointerUp)
    }

    thumb.addEventListener("pointerdown", onPointerDown)
    return () => thumb.removeEventListener("pointerdown", onPointerDown)
  }, [open])

  // Keyboard
  useEffect(() => {
    if (!open) return
    function onKeyDown(e: KeyboardEvent) {
      if (e.key === "ArrowDown") {
        e.preventDefault()
        navigate(indexRef.current + 1)
      } else if (e.key === "ArrowUp") {
        e.preventDefault()
        navigate(indexRef.current - 1)
      } else if (e.key === "Escape") {
        onClose()
      }
    }
    window.addEventListener("keydown", onKeyDown)
    return () => window.removeEventListener("keydown", onKeyDown)
  }, [open, navigate, onClose])

  const page = pages[currentIndex]

  return (
    <AnimatePresence>
      {open && (
        <motion.div
          ref={overlayRef}
          className="fixed inset-0 bg-black/70 backdrop-blur-lg z-50 flex items-center justify-center"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
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

          {/* Static watch */}
          <div ref={watchBtnRef} className="watch-btn">
            <div className="watch">
              <div className="strap strap-top">
                <div className="mesh" /><div className="mesh" /><div className="mesh" />
                <div className="mesh" /><div className="mesh" />
              </div>
              <div className="case">
                <div className="crown" />
                <div className="power" />
                <div ref={screenRef} className="screen">
                  <AnimatePresence custom={direction} mode="popLayout">
                    <motion.div
                      key={page.path}
                      className="screen-slide"
                      custom={direction}
                      variants={screenVariants}
                      initial="enter"
                      animate="center"
                      exit="exit"
                      transition={{ duration: 0.3 }}
                    >
                      <Link to={page.path} onClick={onClose}>
                        {page.name}
                      </Link>
                    </motion.div>
                  </AnimatePresence>
                </div>
              </div>
              <div className="strap strap-bottom">
                <div className="mesh" /><div className="mesh" /><div className="mesh" />
                <div className="mesh" /><div className="mesh" />
              </div>
            </div>
            {/* iOS-style scrollbar */}
            <div
              ref={scrollbarRef}
              className="watch-scrollbar"
              style={{
                top: `${7.3 + 2 + (currentIndex / (pages.length - 1)) * (24.7 - 4 - 4)}em`,
              }}
            />
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  )
}
