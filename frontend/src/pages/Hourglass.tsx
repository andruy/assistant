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

export default function Hourglass() {
  const { isAuthenticated, isLoading } = useAuth()
  const toast = useToast()
  const [inputValue, setInputValue] = useState('')
  const [isAcOff, setIsAcOff] = useState(true)
  const [task, setTask] = useState<Task | null>(null)
  const [sending, setSending] = useState(false)

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

  const API_BASE_URL = '/api/email'

  useEffect(() => {
    setTask({
      timeframe: (60000 * Number(inputValue)) + Date.now(),
      email: {
        to: "andruycira@icloud.com",
        subject: isAcOff ? "Turn AC off" : "Turn AC on",
        body: "Lorem ipsum"
      }
    })
  }, [inputValue, isAcOff])

  async function send() {
    if (!inputValue.trim() || isNaN(Number(inputValue))) return

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
        toast(result.report || 'Task sent')
        setInputValue('')
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
      <div className="flex gap-4 mb-4 items-center">
        <input
          type="number"
          placeholder="Minutes"
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') {
              send()
            }
          }}
          className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-400"
        />
        <div className="flex items-center gap-2">
          <span className={`text-sm ${!isAcOff ? 'text-green-400' : 'text-gray-400'}`}>On</span>
          <button
            type="button"
            onClick={() => setIsAcOff(!isAcOff)}
            className={`relative w-12 h-6 rounded-full transition-colors ${isAcOff ? 'bg-red-500' : 'bg-green-500'}`}
          >
            <span
              className={`absolute top-1 w-4 h-4 bg-white rounded-full transition-transform ${isAcOff ? 'left-7' : 'left-1'}`}
            />
          </button>
          <span className={`text-sm ${isAcOff ? 'text-red-400' : 'text-gray-400'}`}>Off</span>
        </div>
      </div>
      <div className="flex gap-4 mb-4 items-center">
        <span
          className="flex-1 px-4 py-2">
          {isAcOff ? 'AC will stop' : 'AC will start'} {inputValue.trim() ? ` ${new Date(task?.timeframe || 0).toLocaleString()}` : '...'}
        </span>
        <button
          onClick={send}
          disabled={!inputValue.trim() || isNaN(Number(inputValue)) || sending}
          className="px-6 py-2 bg-purple-400 text-white rounded-lg hover:bg-purple-500 disabled:bg-gray-400 flex items-center justify-center min-w-[5rem]"
        >
          {sending ? <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" /> : 'Submit'}
        </button>
      </div>
    </div>
  )
}
