import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { Navigate } from 'react-router'

export default function Instagram() {
  const { isAuthenticated, isLoading } = useAuth()
  const [selectedDate, setSelectedDate] = useState('')
  const [loadingDateList, setLoadingDateList] = useState(true)
  const [dateList, setDateList] = useState<string[]>([])
  const [accounts, setAccounts] = useState<Record<string, string>>({})
  const [loadingAccounts, setLoadingAccounts] = useState(false)

  const API_BASE_URL = '/api/instagram'

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
    } finally {
      setLoadingAccounts(false)
    }
  }

  return (
    <div className="p-6 max-w-2xl mx-auto">
      <div className="flex gap-2 mb-6">
        {loadingDateList ? (
          <div className="w-6 h-6 border-2 border-purple-400 border-t-transparent rounded-full animate-spin" />
        ) : (
          <select
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
            className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-400 bg-gray-800"
          >
            <option value="">-- Select a date --</option>
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
        <div className="space-y-2">
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
      ) : selectedDate && !loadingAccounts ? (
        <div className="text-center py-8 text-gray-400">
          No accounts found for this date
        </div>
      ) : null}
    </div>
  )
}
