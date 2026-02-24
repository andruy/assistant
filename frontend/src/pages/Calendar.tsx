import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { Navigate } from 'react-router'
import { useToast } from '../context/ToastContext'

interface Task {
  timeframe: number
  email: {
    to: string
    subject: string
    body: string
  }
}

export default function Calendar() {
  const { isAuthenticated, isLoading } = useAuth()
  const toast = useToast()
  const [actions, setActions] = useState<string[]>([])
  const [loadingActions, setLoadingActions] = useState(true)
  const [task, setTask] = useState<Task | null>(null)
  const [selectValue, setSelectValue] = useState('')
  const [dateInput, setDateInput] = useState('')
  const [sending, setSending] = useState(false)

  const API_BASE_URL = '/api/email'

  useEffect(() => {
    async function fetchActions() {
      try {
        const response = await fetch(`${API_BASE_URL}/tasks`)
        if (response.ok) {
          const data: string[] = await response.json()
          setActions(data)
        }
      } catch (error) {
        console.error('Failed to fetch actions:', error)
        toast('Failed to load actions')
      } finally {
        setLoadingActions(false)
      }
    }

    if (isAuthenticated) {
      fetchActions()
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

  useEffect(() => {
    if (dateInput && selectValue) {
      setTask({
        timeframe: new Date(dateInput).getTime(),
        email: {
          to: "andruycira@icloud.com",
          subject: selectValue,
          body: "Lorem ipsum"
        }
      })
    }
  }, [dateInput, selectValue])

  async function send() {
    if (!task || !selectValue || !dateInput) return

    setSending(true)
    try {
      const response = await fetch(`${API_BASE_URL}/task`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(task)
      })

      if (response.ok) {
        const result = await response.json()
        toast(result.report || 'Task scheduled')
        setSelectValue('')
        setDateInput('')
        setTask(null)
      } else {
        console.error(response)
        toast('Something went wrong')
      }
    } finally {
      setSending(false)
    }
  }

  return (
    <div className="p-6 max-w-2xl mx-auto">
      <div className="space-y-4 mb-4">
        <div>
          {loadingActions ? (
            <div className="w-6 h-6 border-2 border-purple-400 border-t-transparent rounded-full animate-spin" />
          ) : (
            <select
              value={selectValue}
              onChange={(e) => setSelectValue(e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-400 bg-gray-800"
            >
              <option value="" hidden>-- Select an action --</option>
              {actions.map((action) => (
                <option key={action} value={action}>
                  {action}
                </option>
              ))}
            </select>
          )}
        </div>

        <div>
          <input
            type="datetime-local"
            value={dateInput}
            onChange={(e) => setDateInput(e.target.value)}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-400 bg-gray-800"
          />
        </div>
      </div>

      <button
        onClick={send}
        disabled={!selectValue || !dateInput || sending}
        className="px-6 py-2 bg-purple-400 text-white rounded-lg hover:bg-purple-500 disabled:bg-gray-400"
      >
        {sending ? 'Scheduling...' : 'Schedule'}
      </button>
    </div>
  )
}
