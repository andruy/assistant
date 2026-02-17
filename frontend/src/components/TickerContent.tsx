import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { fetchCurrencyRates } from '../api/CurrencyRates'

const CURRENCIES = ['EUR', 'PYG', 'ARS', 'BRL', 'VES'] as const

export default function TickerContent() {
  const { isAuthenticated } = useAuth()
  const [rates, setRates] = useState<Record<string, number> | null>(null)

  useEffect(() => {
    if (!isAuthenticated) return

    const loadRates = async () => {
      try {
      const data = await fetchCurrencyRates()
      setRates(data.conversion_rates)
      } catch {
        setRates(null)
      }
    }
    loadRates()
  }, [isAuthenticated])

  if (!isAuthenticated || !rates) {
    return (
      <>
        <span>Nexus v1.0</span>
        <span className="text-purple-400/60">◆</span>
        <span>React + Spring Boot</span>
        <span className="text-purple-400/60">◆</span>
        <span>Personal Assistant</span>
        <span className="text-purple-400/60">◆</span>
      </>
    )
  }

  return (
    <>
      {CURRENCIES.map(code => (
        <span key={code} className="flex items-center gap-2">
          <span>{code}</span>
          <span className="text-white font-medium">{rates[code]}</span>
          <span className="text-purple-400/60 ml-2">◆</span>
        </span>
      ))}
    </>
  )
}
