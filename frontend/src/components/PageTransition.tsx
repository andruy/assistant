import { motion } from "framer-motion"
import { type ReactNode, useState } from "react"

export default function PageTransition({ children, center }: { children: ReactNode; center?: boolean }) {
  const [frozen] = useState(children)

  return (
    <motion.div
      initial={{ opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -12 }}
      transition={{ duration: 0.35 }}
      className={`h-full overflow-y-auto px-4 dark-scroll ${center ? 'flex items-center justify-center' : ''}`}
    >
      {frozen}
    </motion.div>
  )
}
