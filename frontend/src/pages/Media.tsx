import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { Navigate } from 'react-router'

interface MediaFile {
  name: string
  size: number
  type: string
  lastModified: number
}

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
  return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
}

export default function Media() {
  const { isAuthenticated, isLoading } = useAuth()
  const [files, setFiles] = useState<MediaFile[]>([])
  const [loadingFiles, setLoadingFiles] = useState(true)
  const [selectedFile, setSelectedFile] = useState<MediaFile | null>(null)

  const API_BASE_URL = '/api/media'

  async function fetchFiles() {
    setLoadingFiles(true)
    try {
      const response = await fetch(API_BASE_URL)
      if (response.ok) {
        const data: MediaFile[] = await response.json()
        setFiles(data)
      }
    } catch (error) {
      console.error('Failed to fetch media files:', error)
    } finally {
      setLoadingFiles(false)
    }
  }

  useEffect(() => {
    if (isAuthenticated) {
      fetchFiles()
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

  return (
    <div className="p-6 max-w-4xl mx-auto">
      {selectedFile && (
        <div className="mb-6">
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-lg font-medium text-purple-300 truncate mr-4">
              {selectedFile.name}
            </h2>
            <button
              onClick={() => setSelectedFile(null)}
              className="px-3 py-1 text-sm bg-gray-800 border border-gray-700 rounded-lg hover:border-purple-500/50 text-gray-300 hover:text-white transition-all"
            >
              Close
            </button>
          </div>
          <video
            key={selectedFile.name}
            controls
            className="w-full rounded-lg bg-black"
            src={`${API_BASE_URL}/stream/${encodeURIComponent(selectedFile.name)}`}
          />
        </div>
      )}

      <div className="flex justify-end mb-4">
        <button
          onClick={fetchFiles}
          disabled={loadingFiles}
          className="px-4 py-2 text-sm bg-gray-800 border border-gray-700 rounded-lg hover:border-purple-500/50 text-gray-300 hover:text-white transition-all disabled:opacity-50"
        >
          {loadingFiles ? 'Refreshing...' : 'Refresh'}
        </button>
      </div>

      {loadingFiles ? (
        <div className="flex items-center justify-center py-8">
          <div className="w-6 h-6 border-2 border-purple-400 border-t-transparent rounded-full animate-spin" />
        </div>
      ) : files.length === 0 ? (
        <div className="text-center py-8 text-gray-400">
          No media files found
        </div>
      ) : (
        <div className="space-y-2">
          {files.map((file) => (
            <div
              key={file.name}
              className={`flex items-center justify-between p-3 rounded-lg border transition-all duration-200 ${
                selectedFile?.name === file.name
                  ? 'bg-purple-500/20 border-purple-500/50'
                  : 'bg-gray-800/50 border-gray-700 hover:border-purple-500/30 hover:bg-gray-800'
              }`}
            >
              <button
                onClick={() => setSelectedFile(file)}
                className="flex-1 text-left min-w-0"
              >
                <div className="text-purple-400 hover:text-purple-300 truncate">
                  {file.name}
                </div>
                <div className="text-xs text-gray-500 mt-1">
                  {formatFileSize(file.size)} &middot; {file.type}
                </div>
              </button>
              <a
                href={`${API_BASE_URL}/download/${encodeURIComponent(file.name)}`}
                className="ml-3 px-3 py-1 text-sm bg-gray-700 border border-gray-600 rounded-lg hover:border-purple-500/50 text-gray-300 hover:text-white transition-all shrink-0"
              >
                Download
              </a>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
