import { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { Navigate } from 'react-router'

export default function Folder() {
  const { isAuthenticated, isLoading } = useAuth()
  const [folderName, setFolderName] = useState('')
  const [message, setMessage] = useState('')

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

  const API_BASE_URL = '/api/directory'

  async function send() {
    if (!folderName.trim()) return

    const params = new URLSearchParams({ name: folderName })
    const response = await fetch(`${API_BASE_URL}?${params}`, {
      method: 'POST'
    })

    if (response.ok) {
      const result = await response.json()
      setMessage(result.report || 'Folder created successfully')
      setFolderName('')
    } else {
      console.error(response)
      setMessage('Something went wrong')
    }
  }

  return (
    <div className="p-6 max-w-2xl mx-auto">
      <div className="flex gap-2 mb-4">
        <input
          type="text"
          placeholder="Enter folder name"
          value={folderName}
          onChange={(e) => setFolderName(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') {
              send()
            }
          }}
          className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-400"
        />
        <button
          onClick={send}
          disabled={!folderName.trim()}
          className="px-6 py-2 bg-purple-400 text-white rounded-lg hover:bg-purple-500 disabled:bg-gray-400"
        >
          Create
        </button>
      </div>
      {message && (
        <div className="p-3 bg-cyan-800 rounded-lg">
          {message}
        </div>
      )}
    </div>
  )
}
