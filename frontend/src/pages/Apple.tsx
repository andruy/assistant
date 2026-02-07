import { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { Navigate } from 'react-router'

export default function Apple() {
  const { isAuthenticated, isLoading, authFetch } = useAuth()
  const [linksArray, setLinksArray] = useState<string[]>([])
  const [inputValue, setInputValue] = useState('')

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

  const API_BASE_URL = '/api/shell'

  async function send() {
    const data = {
        links: linksArray
    }

    const response = await authFetch(`${API_BASE_URL}/youtube/auto`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    })

    if (response.ok) {
        const result = await response.json()
        console.log(result.report)
        setLinksArray([])
        return result
    } else {
        console.error(response)
        console.log(data)
        return "Something went wrong"
    }
  }

  return (
    <div className="p-6 max-w-2xl mx-auto">
      <div className="flex gap-2 mb-6">
        <input
          type="text"
          placeholder="Enter a link"
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-400"
          onKeyDown={(e) => {
            if (e.key === 'Enter' && inputValue.trim()) {
              setLinksArray([...linksArray, inputValue])
              setInputValue('')
            }
          }}
        />
        <button
          onClick={() => {
            if (inputValue.trim()) {
              setLinksArray([...linksArray, inputValue])
              setInputValue('')
            }
          }}
          disabled={!inputValue.trim()}
          className="px-6 py-2 bg-purple-400 text-white rounded-lg hover:bg-purple-500 disabled:bg-gray-400"
        >
          Add
        </button>
      </div>
      <div className="space-y-2">
        {linksArray.map((link, index) => (
          <div key={index} className="p-3 bg-cyan-800 rounded-lg flex justify-between items-center">
            <span>{link}</span>
            <button
              onClick={() => setLinksArray(linksArray.filter((_, i) => i !== index))}
              className="text-red-500 hover:text-red-700"
            >
              Remove
            </button>
          </div>
        ))}
      </div>
      <button
        onClick={send}
        disabled={linksArray.length === 0}
        className="mt-6 px-6 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 disabled:bg-gray-400"
      >
        Send
      </button>
    </div>
  )
}
