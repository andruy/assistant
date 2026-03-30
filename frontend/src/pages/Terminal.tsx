import { useState, useEffect, useRef } from 'react'
import { useAuth } from '../context/AuthContext'
import { Navigate } from 'react-router'

type ConnectionStatus = 'connecting' | 'connected' | 'disconnected'

export default function Terminal() {
  const { isAuthenticated, isLoading } = useAuth()
  const [output, setOutput] = useState('')
  const [loading, setLoading] = useState(true)
  const [status, setStatus] = useState<ConnectionStatus>('connecting')
  const [commit, setCommit] = useState('logs')
  const scrollRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!isAuthenticated) return

    async function fetchCommit() {
      try {
        const res = await fetch('/api/health')
        const data = await res.json()
        if (data.commit) setCommit(data.commit)
      } catch { /* ignore */ }
    }
    fetchCommit()

    const eventSource = new EventSource('/api/logs/stream')

    eventSource.addEventListener('init', (event) => {
      setOutput(event.data || 'No logs available')
      setLoading(false)
      setStatus('connected')
    })

    eventSource.addEventListener('log', (event) => {
      setOutput(prev => prev + event.data)
      setStatus('connected')
    })

    eventSource.onerror = () => {
      setStatus('disconnected')
    }

    return () => eventSource.close()
  }, [isAuthenticated])

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight
    }
  }, [output])

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

  const statusColor = {
    connecting: 'bg-yellow-500',
    connected: 'bg-green-500',
    disconnected: 'bg-red-500',
  }[status]

  const statusLabel = {
    connecting: 'Connecting...',
    connected: 'Live',
    disconnected: 'Disconnected',
  }[status]

  return (
    <div className="p-5 max-w-7xl mx-auto">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-full bg-red-500" />
          <div className="w-3 h-3 rounded-full bg-yellow-500" />
          <div className="w-3 h-3 rounded-full bg-green-500" />
          <span className="ml-2 text-sm text-gray-400">{commit}</span>
        </div>
        <div className="flex items-center gap-2">
          <div className={`w-2 h-2 rounded-full ${statusColor}`} />
          <span className="text-xs text-gray-500">{statusLabel}</span>
        </div>
      </div>

      <div className="bg-gray-900 border border-gray-700 rounded-lg overflow-hidden">
        <div ref={scrollRef} className="p-4 h-[70vh] overflow-auto font-mono text-sm terminal-scroll">
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
