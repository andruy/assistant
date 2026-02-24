import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { Navigate } from 'react-router'
import { useToast } from '../context/ToastContext'

export default function Microsoft() {
  const { isAuthenticated, isLoading } = useAuth()
  const toast = useToast()
  const [directories, setDirectories] = useState<string[]>([])
  const [selectedDirectory, setSelectedDirectory] = useState<string>('')
  const [linksMap, setLinksMap] = useState<Record<string, string[]>>({})
  const [loadingDirectories, setLoadingDirectories] = useState(true)
  const [sending, setSending] = useState(false)

  const API_BASE_URL = '/api/shell'

  useEffect(() => {
    async function fetchDirectories() {
      try {
        const response = await fetch(`${API_BASE_URL}/directories`)
        if (response.ok) {
          const data: { name: string }[] = await response.json()
          data.sort((a, b) => a.name.localeCompare(b.name))
          setDirectories(data.map(item => item.name))
        }
      } catch (error) {
        console.error('Failed to fetch directories:', error)
        toast('Failed to load directories')
      } finally {
        setLoadingDirectories(false)
      }
    }

    if (isAuthenticated) {
      fetchDirectories()
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

  function addLink(link: string) {
    if (!selectedDirectory || !link.trim()) return

    setLinksMap(prev => ({
      ...prev,
      [selectedDirectory]: [...(prev[selectedDirectory] || []), link]
    }))
  }

  function removeLink(directory: string, index: number) {
    setLinksMap(prev => {
      const updatedLinks = prev[directory].filter((_, i) => i !== index)
      if (updatedLinks.length === 0) {
        const { [directory]: _, ...rest } = prev
        return rest
      }
      return { ...prev, [directory]: updatedLinks }
    })
  }

  function removeDirectory(directory: string) {
    setLinksMap(prev => {
      const { [directory]: _, ...rest } = prev
      return rest
    })
  }

  async function send() {
    if (Object.keys(linksMap).length === 0) return

    setSending(true)
    try {
      const response = await fetch(`${API_BASE_URL}/youtube`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(linksMap)
      })

      if (response.ok) {
        const result = await response.json()
        console.log(result.message)
        toast('Links submitted')
        setLinksMap({})
        setSelectedDirectory('')
        return result
      } else {
        console.error(response)
        toast('Something went wrong')
        return 'Something went wrong'
      }
    } finally {
      setSending(false)
    }
  }

  const totalLinks = Object.values(linksMap).reduce((sum, links) => sum + links.length, 0)

  return (
    <div className="p-6 max-w-2xl mx-auto">
      <div className="mb-6">
        {loadingDirectories ? (
          <div className="w-6 h-6 border-2 border-purple-400 border-t-transparent rounded-full animate-spin" />
        ) : (
          <select
            value={selectedDirectory}
            onChange={(e) => setSelectedDirectory(e.target.value)}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-400 bg-gray-800"
          >
            <option value="" hidden>-- Select a directory --</option>
            {directories.map((dir) => (
              <option key={dir} value={dir}>
                {dir} {linksMap[dir] ? `(${linksMap[dir].length})` : ''}
              </option>
            ))}
          </select>
        )}
      </div>

      <div className="flex gap-2 mb-6">
        <input
          type="text"
          placeholder="Enter a link"
          className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-400"
          onKeyDown={(e) => {
            if (e.key === 'Enter' && e.currentTarget.value.trim()) {
              addLink(e.currentTarget.value)
              e.currentTarget.value = ''
            }
          }}
          id="linkInput"
          disabled={!selectedDirectory}
        />
        <button
          onClick={() => {
            const input = document.getElementById('linkInput') as HTMLInputElement
            if (input.value.trim()) {
              addLink(input.value)
              input.value = ''
            }
          }}
          disabled={!selectedDirectory}
          className="px-6 py-2 bg-purple-400 text-white rounded-lg hover:bg-purple-500 disabled:bg-gray-400"
        >
          Add
        </button>
      </div>

      <div className="space-y-4">
        {Object.entries(linksMap).map(([directory, links]) => (
          <div key={directory} className="border border-gray-600 rounded-lg p-4">
            <div className="flex justify-between items-center mb-2">
              <h3 className="font-medium text-purple-400">{directory}</h3>
              <button
                onClick={() => removeDirectory(directory)}
                className="text-red-500 hover:text-red-700 text-sm"
              >
                Remove All
              </button>
            </div>
            <div className="space-y-2">
              {links.map((link, index) => (
                <div key={index} className="p-2 bg-cyan-800 rounded flex justify-between items-center">
                  <span className="truncate mr-2 text-sm">{link}</span>
                  <button
                    onClick={() => removeLink(directory, index)}
                    className="text-red-500 hover:text-red-700 shrink-0 text-sm"
                  >
                    Remove
                  </button>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>

      <button
        onClick={send}
        disabled={totalLinks === 0 || sending}
        className="mt-6 px-6 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 disabled:bg-gray-400"
      >
        {sending ? <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" /> : `Send${totalLinks > 0 ? ` (${totalLinks} link${totalLinks > 1 ? 's' : ''})` : ''}`}
      </button>
    </div>
  )
}
