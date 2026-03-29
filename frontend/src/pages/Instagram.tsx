import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { Navigate } from 'react-router'
import { useToast } from '../context/ToastContext'

export default function Instagram() {
  const { isAuthenticated, isLoading } = useAuth()
  const toast = useToast()
  const [selectedDate, setSelectedDate] = useState('')
  const [loadingDateList, setLoadingDateList] = useState(true)
  const [dateList, setDateList] = useState<number[]>([])
  const [accounts, setAccounts] = useState<Record<string, string>>({})
  const [loadingAccounts, setLoadingAccounts] = useState(false)
  const [showCompare, setShowCompare] = useState(false)
  const [comparing, setComparing] = useState(false)
  const [fetchingFollowers, setFetchingFollowers] = useState(false)
  const [fetchingFollowing, setFetchingFollowing] = useState(false)
  const [followersDates, setFollowersDates] = useState<number[]>([])
  const [followingDates, setFollowingDates] = useState<number[]>([])
  const [selectedFollowersDate, setSelectedFollowersDate] = useState('')
  const [selectedFollowingDate, setSelectedFollowingDate] = useState('')
  const [comparingDates, setComparingDates] = useState(false)
  const [showDateCompare, setShowDateCompare] = useState(false)

  const API_BASE_URL = '/api/instagram'

  function formatDate(ms: number) {
    return new Date(ms).toLocaleString()
  }

  useEffect(() => {
    if (!showCompare || comparing || fetchingFollowers || fetchingFollowing) return
    function handleClick() { setShowCompare(false) }
    document.addEventListener('click', handleClick)
    return () => document.removeEventListener('click', handleClick)
  }, [showCompare, comparing, fetchingFollowers, fetchingFollowing])

  useEffect(() => {
    async function fetchList() {
      try {
        const response = await fetch(`${API_BASE_URL}/dates`)
        if (response.ok) {
          const data: number[] = await response.json()
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

  async function fetchDateOptions() {
    try {
      const [fRes, gRes] = await Promise.all([
        fetch(`${API_BASE_URL}/dates/followers`),
        fetch(`${API_BASE_URL}/dates/following`),
      ])
      if (fRes.ok) setFollowersDates(await fRes.json())
      if (gRes.ok) setFollowingDates(await gRes.json())
    } catch (error) {
      console.error('Failed to fetch date options:', error)
      toast('Failed to load date options')
    }
  }

  async function compareDates() {
    if (!selectedFollowersDate || !selectedFollowingDate) return
    setComparingDates(true)
    try {
      const params = new URLSearchParams({
        dateFollowers: selectedFollowersDate,
        dateFollowing: selectedFollowingDate,
      })
      const response = await fetch(`${API_BASE_URL}/compare-dates?${params}`)
      if (response.ok) {
        toast('Date comparison completed')
      } else {
        toast('Date comparison failed')
      }
    } catch {
      toast('Date comparison failed')
    } finally {
      setComparingDates(false)
    }
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
          <div className="flex gap-2" onClick={(e) => e.stopPropagation()}>
            <button
              disabled={fetchingFollowers}
              onClick={async () => {
                setFetchingFollowers(true)
                try {
                  const response = await fetch(`${API_BASE_URL}/followers`)
                  if (response.ok) { toast('Followers task started'); setShowCompare(false) }
                  else toast('Followers task failed')
                } catch {
                  toast('Followers task failed')
                } finally {
                  setFetchingFollowers(false)
                }
              }}
              className="px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-500 disabled:bg-gray-400 flex items-center justify-center min-w-24"
            >
              {fetchingFollowers ? <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" /> : 'Followers'}
            </button>
            <button
              disabled={comparing}
              onClick={async () => {
                setComparing(true)
                try {
                  const response = await fetch(`${API_BASE_URL}/compare`)
                  if (response.ok) {
                    toast('Comparison task started')
                    setShowCompare(false)
                  } else {
                    toast('Comparison task failed')
                  }
                } catch {
                  toast('Comparison task failed')
                } finally {
                  setComparing(false)
                }
              }}
              className="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 disabled:bg-gray-400 flex items-center justify-center min-w-24"
            >
              {comparing ? <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" /> : 'Compare'}
            </button>
            <button
              disabled={fetchingFollowing}
              onClick={async () => {
                setFetchingFollowing(true)
                try {
                  const response = await fetch(`${API_BASE_URL}/following`)
                  if (response.ok) { toast('Following task started'); setShowCompare(false) }
                  else toast('Following task failed')
                } catch {
                  toast('Following task failed')
                } finally {
                  setFetchingFollowing(false)
                }
              }}
              className="px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-500 disabled:bg-gray-400 flex items-center justify-center min-w-24"
            >
              {fetchingFollowing ? <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" /> : 'Following'}
            </button>
          </div>
        ) : (
          <button
            onClick={(e) => { e.stopPropagation(); setShowCompare(true) }}
            className="text-gray-500 hover:text-gray-300 transition-colors"
          >
            &#x2026;
          </button>
        )}
      </div>

      <div className="flex justify-center mb-6">
        <button
          onClick={() => {
            setShowDateCompare(!showDateCompare)
            if (!showDateCompare && followersDates.length === 0) fetchDateOptions()
          }}
          className="text-sm text-gray-500 hover:text-gray-300 transition-colors"
        >
          {showDateCompare ? 'Hide' : 'Compare by date'}
        </button>
      </div>

      {showDateCompare && (
        <div className="mb-6 p-4 border border-gray-700 rounded-lg space-y-3">
          <div className="flex gap-2">
            <select
              value={selectedFollowersDate}
              onChange={(e) => setSelectedFollowersDate(e.target.value)}
              className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-400 bg-gray-800 text-sm"
            >
              <option value="" hidden>Followers date</option>
              {followersDates.map((d) => <option key={d} value={d}>{formatDate(d)}</option>)}
            </select>
            <select
              value={selectedFollowingDate}
              onChange={(e) => setSelectedFollowingDate(e.target.value)}
              className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-400 bg-gray-800 text-sm"
            >
              <option value="" hidden>Following date</option>
              {followingDates.map((d) => <option key={d} value={d}>{formatDate(d)}</option>)}
            </select>
          </div>
          <button
            onClick={compareDates}
            disabled={!selectedFollowersDate || !selectedFollowingDate || comparingDates}
            className="w-full px-4 py-2 bg-purple-400 text-white rounded-lg hover:bg-purple-500 disabled:bg-gray-400 flex items-center justify-center"
          >
            {comparingDates ? <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" /> : 'Compare'}
          </button>
        </div>
      )}

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
            {dateList.map((d) => (
              <option key={d} value={d}>
                {formatDate(d)}
              </option>
            ))}
          </select>
        )}
        <button
          onClick={fetchAccounts}
          disabled={!selectedDate || loadingAccounts}
          className="px-6 py-2 bg-purple-400 text-white rounded-lg hover:bg-purple-500 disabled:bg-gray-400 flex items-center justify-center min-w-20"
        >
          {loadingAccounts ? <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" /> : 'Submit'}
        </button>
      </div>

      {loadingAccounts ? (
        <div className="flex items-center justify-center py-8">
          <div className="w-6 h-6 border-2 border-purple-400 border-t-transparent rounded-full animate-spin" />
        </div>
      ) : Object.keys(accounts).length > 0 ? (
        <div className="space-y-2 text-center">
          <p className="text-sm text-gray-500 mb-2">{Object.keys(accounts).length} accounts</p>
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
