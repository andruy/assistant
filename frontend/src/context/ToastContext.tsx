import { AnimatePresence, motion } from "framer-motion"
import {
  createContext,
  useContext,
  useState,
  type ReactNode,
} from "react"

type Toast = { id: number; message: string }

const ToastContext = createContext<(msg: string) => void>(() => {})

export const useToast = () => useContext(ToastContext)

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([])

  function push(message: string) {
    const id = Date.now()
    setToasts(t => [...t, { id, message }])

    setTimeout(() => {
      setToasts(t => t.filter(x => x.id !== id))
    }, 3000)
  }

  return (
    <ToastContext.Provider value={push}>
      {children}
      <div className="fixed bottom-4 left-0 right-0 flex justify-center pointer-events-none">
        <AnimatePresence>
          {toasts.map(t => (
            <motion.div
              key={t.id}
              initial={{ opacity: 0, y: 40 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: 40 }}
              className="bg-black/60 backdrop-blur-md text-gray-200
                         px-5 py-3 rounded-xl shadow-xl"
            >
              {t.message}
            </motion.div>
          ))}
        </AnimatePresence>
      </div>
    </ToastContext.Provider>
  )
}
