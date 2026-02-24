import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { Navigate } from 'react-router'
import { useToast } from '../context/ToastContext'

export default function Instagram() {
  const { isAuthenticated, isLoading } = useAuth()
  const toast = useToast()
  const [selectedDate, setSelectedDate] = useState('')
  const [loadingDateList, setLoadingDateList] = useState(true)
  const [dateList, setDateList] = useState<string[]>([])
  const [accounts, setAccounts] = useState<Record<string, string>>({})
  const [loadingAccounts, setLoadingAccounts] = useState(false)
  const [showCompare, setShowCompare] = useState(false)
  const [comparing, setComparing] = useState(false)

  const API_BASE_URL = '/api/instagram'

  useEffect(() => {
    if (!showCompare || comparing) return
    function handleClick() { setShowCompare(false) }
    document.addEventListener('click', handleClick)
    return () => document.removeEventListener('click', handleClick)
  }, [showCompare, comparing])

  useEffect(() => {
    async function fetchList() {
      try {
        const response = await fetch(`${API_BASE_URL}/dates`)
        if (response.ok) {
          const data: string[] = await response.json()
          setDateList(data)
        }
      } catch (error) {
        console.error('Failed to fetch list of dates:', error)
        toast('Failed to load dates')
      } finally {
        setLoadingDateList(false)
      }
    }

    if (isAuthenticated) {
      fetchList()
    }
  }, [isAuthenticated])

  if (isLoading) {
    return (
      <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center">
        <div className="w-8 h-8 border-2 border-purple-400 border-t-transparent rounded-full animate-spin" />
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/" replace />
  }

  async function fetchAccounts() {
    if (!selectedDate) return

    setLoadingAccounts(true)
    setAccounts({})

    try {
      const params = new URLSearchParams({ date: selectedDate })
      const response = await fetch(`${API_BASE_URL}/accounts?${params}`)

      if (response.ok) {
        const data: Record<string, string> = await response.json()
        setAccounts(data)
      }
    } catch (error) {
      console.error('Failed to fetch accounts:', error)
      toast('Failed to load accounts')
    } finally {
      setLoadingAccounts(false)
    }
  }

  return (
    <div className="p-6 max-w-2xl mx-auto">
      <div className="flex justify-center mb-6">
        {showCompare ? (
          <button
            disabled={comparing}
            onClick={async (e) => {
              e.stopPropagation()
              setComparing(true)
              try {
                const response = await fetch(`${API_BASE_URL}/compare`)
                if (response.ok) {
                  toast('Compare started')
                  setShowCompare(false)
                } else {
                  toast('Compare failed')
                }
              } catch {
                toast('Compare failed')
              } finally {
                setComparing(false)
              }
            }}
            className="px-6 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 disabled:bg-gray-400"
          >
            {comparing ? 'Comparing...' : 'Compare'}
          </button>
        ) : (
          <button
            onClick={(e) => { e.stopPropagation(); setShowCompare(true) }}
            className="text-gray-500 hover:text-gray-300 transition-colors"
          >
            &#x2026;
          </button>
        )}
      </div>

      <div className="flex gap-2 mb-6">
        {loadingDateList ? (
          <div className="w-6 h-6 border-2 border-purple-400 border-t-transparent rounded-full animate-spin" />
        ) : (
          <select
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
            className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-400 bg-gray-800"
          >
            <option value="" hidden>-- Select a date --</option>
            {dateList.map((date) => (
              <option key={date} value={date}>
                {date}
              </option>
            ))}
          </select>
        )}
        <button
          onClick={fetchAccounts}
          disabled={!selectedDate || loadingAccounts}
          className="px-6 py-2 bg-purple-400 text-white rounded-lg hover:bg-purple-500 disabled:bg-gray-400"
        >
          {loadingAccounts ? 'Loading...' : 'Submit'}
        </button>
      </div>

      {loadingAccounts ? (
        <div className="flex items-center justify-center py-8">
          <div className="w-6 h-6 border-2 border-purple-400 border-t-transparent rounded-full animate-spin" />
        </div>
      ) : Object.keys(accounts).length > 0 ? (
        <div className="space-y-2 text-center">
          {Object.entries(accounts).map(([name, url]) => (
            <a
              key={name}
              href={url}
              target="_blank"
              rel="noopener noreferrer"
              className="block p-3 bg-gray-800/50 border border-gray-700 rounded-lg hover:border-purple-500/50 hover:bg-gray-800 transition-all duration-200"
            >
              <span className="text-purple-400 hover:text-purple-300">{name}</span>
              <span className="text-gray-500 text-sm ml-2">↗</span>
            </a>
          ))}
        </div>
      ) : null}
    </div>
  )
}
