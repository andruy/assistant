import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { Navigate } from 'react-router'

export default function Terminal() {
  const { isAuthenticated, isLoading, authFetch } = useAuth()
  const [output, setOutput] = useState('')
  const [loading, setLoading] = useState(true)
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null)

  const API_BASE_URL = '/api/logs'

  const fetchLogs = async () => {
    setLoading(true)
    try {
      const response = await authFetch(API_BASE_URL)
      const data: { report: string } = await response.json()
      setOutput(data.report || 'No logs available')
      setLastUpdated(new Date())
    } catch (error) {
      setOutput('Error fetching logs')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (isAuthenticated) {
      fetchLogs()
    }
  }, [isAuthenticated, authFetch])

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

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-full bg-red-500" />
          <div className="w-3 h-3 rounded-full bg-yellow-500" />
          <div className="w-3 h-3 rounded-full bg-green-500" />
          <span className="ml-2 text-sm text-gray-400">logs</span>
        </div>
        <div className="flex items-center gap-4">
          {lastUpdated && (
            <span className="text-xs text-gray-500">
              Updated: {lastUpdated.toLocaleTimeString()}
            </span>
          )}
          <button
            onClick={fetchLogs}
            disabled={loading}
            className="px-4 py-1.5 bg-gray-700 text-white rounded-lg hover:bg-gray-600 disabled:bg-gray-800 disabled:text-gray-500 transition-colors text-sm flex items-center gap-2"
          >
            <span className={loading ? 'animate-spin' : ''}>↻</span>
            Refresh
          </button>
        </div>
      </div>

      <div className="bg-gray-900 border border-gray-700 rounded-lg overflow-hidden">
        <div className="p-4 h-[60vh] overflow-auto font-mono text-sm">
          {loading && !output ? (
            <div className="flex items-center justify-center h-full">
              <div className="w-6 h-6 border-2 border-purple-400 border-t-transparent rounded-full animate-spin" />
            </div>
          ) : (
            <pre className="text-green-400 whitespace-pre-wrap wrap-break-word">
              {output}
            </pre>
          )}
        </div>
      </div>
    </div>
  )
}
