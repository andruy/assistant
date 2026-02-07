import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { Navigate } from 'react-router'

interface TaskId {
  id: string
  name: string
  time: string
}

export default function Notepad() {
  const { isAuthenticated, isLoading, checkAuth } = useAuth()
  const [loadingTasks, setLoadingTasks] = useState(true)
  const [tasks, setTasks] = useState<TaskId[]>([])
  const [selectedTask, setSelectedTask] = useState<TaskId | null>(null)
  const [message, setMessage] = useState('')

  const API_BASE_URL = '/api/email'

  useEffect(() => {
    async function fetchRunningTasks() {
      try {
        const response = await fetch(`${API_BASE_URL}/running`)
        if (!response.ok) {
          await checkAuth()
          return
        }
        const data: TaskId[] = await response.json()
        setTasks(data)
      } catch (error) {
        console.error('Failed to fetch threads:', error)
      } finally {
        setLoadingTasks(false)
      }
    }

    if (isAuthenticated) {
      fetchRunningTasks()
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

  async function send() {
    if (!selectedTask) return

    const response = await fetch(`${API_BASE_URL}/task`, {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(selectedTask)
    })

    if (!response.ok) {
      await checkAuth()
      return
    }
    const result = await response.json()
    setMessage(result.report || 'Task cancelled successfully')
    setTasks(tasks.filter(t => t.id !== selectedTask.id))
    setSelectedTask(null)
  }

  return (
    <div className="p-6 max-w-2xl mx-auto">
      <div className="mb-4">
        {loadingTasks ? (
          <div className="flex items-center justify-center py-8">
            <div className="w-6 h-6 border-2 border-purple-400 border-t-transparent rounded-full animate-spin" />
          </div>
        ) : tasks.length === 0 ? (
          <div className="text-center py-8 text-gray-400">
            No running tasks
          </div>
        ) : (
          <div className="space-y-2">
            {tasks.map((task) => (
              <div
                key={task.id}
                onClick={() => setSelectedTask(task)}
                className={`p-4 rounded-lg cursor-pointer transition-all duration-200 border ${
                  selectedTask?.id === task.id
                    ? 'bg-purple-500/20 border-purple-500/50'
                    : 'bg-gray-800/50 border-gray-700 hover:border-purple-500/30'
                }`}
              >
                <div className="flex justify-between items-start">
                  <div>
                    <div className="font-medium text-white">{task.name}</div>
                    <div className="text-sm text-gray-400">ID: {task.id}</div>
                  </div>
                  <div className="text-xs text-gray-500">
                    {task.time}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      <button
        onClick={send}
        disabled={!selectedTask}
        className="px-6 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 disabled:bg-gray-400 transition-colors"
      >
        Cancel Task
      </button>

      {message && (
        <div className="mt-4 p-3 bg-cyan-800 rounded-lg">
          {message}
        </div>
      )}
    </div>
  )
}
