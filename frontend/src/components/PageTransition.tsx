import { motion } from "framer-motion"
import { type ReactNode, useState } from "react"

export default function PageTransition({ children }: { children: ReactNode }) {
  const [frozen] = useState(children)

  return (
    <motion.div
      initial={{ opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -12 }}
      transition={{ duration: 0.35 }}
      className="h-full"
    >
      {frozen}
    </motion.div>
  )
}
