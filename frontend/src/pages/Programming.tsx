import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { Navigate } from 'react-router'
import { useToast } from '../context/ToastContext'

interface MediaFile {
  name: string
  size: number
  type: string
  lastModified: number
}

interface DirectoryListing {
  folders: string[]
  files: MediaFile[]
}

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
  return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
}

const API_BASE_URL = '/api/programming'

export default function Programming() {
  const { isAuthenticated, isLoading } = useAuth()
  const toast = useToast()
  const [currentPath, setCurrentPath] = useState('')
  const [listing, setListing] = useState<DirectoryListing>({ folders: [], files: [] })
  const [loadingListing, setLoadingListing] = useState(true)
  const [selectedFile, setSelectedFile] = useState<MediaFile | null>(null)

  async function fetchListing(path: string) {
    setLoadingListing(true)
    try {
      const params = new URLSearchParams({ path })
      const response = await fetch(`${API_BASE_URL}/list?${params}`)
      if (response.ok) {
        const data: DirectoryListing = await response.json()
        setListing(data)
      } else {
        toast('Failed to load directory')
      }
    } catch (error) {
      console.error('Failed to fetch listing:', error)
      toast('Failed to load directory')
    } finally {
      setLoadingListing(false)
    }
  }

  useEffect(() => {
    if (isAuthenticated) {
      fetchListing(currentPath)
    }
  }, [isAuthenticated, currentPath])

  function enterFolder(name: string) {
    setSelectedFile(null)
    setCurrentPath(currentPath ? `${currentPath}/${name}` : name)
  }

  function navigateTo(path: string) {
    setSelectedFile(null)
    setCurrentPath(path)
  }

  function buildFileUrl(file: MediaFile, endpoint: 'stream' | 'download') {
    const fullPath = currentPath ? `${currentPath}/${file.name}` : file.name
    const params = new URLSearchParams({ path: fullPath })
    return `${API_BASE_URL}/${endpoint}?${params}`
  }

  const breadcrumbs = currentPath ? currentPath.split('/') : []

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
            key={buildFileUrl(selectedFile, 'stream')}
            controls
            className="w-full rounded-lg bg-black"
            src={buildFileUrl(selectedFile, 'stream')}
          />
        </div>
      )}

      <div className="flex items-center flex-wrap gap-1 mb-4 text-sm">
        <button
          onClick={() => navigateTo('')}
          className="text-purple-400 hover:text-purple-300"
        >
          root
        </button>
        {breadcrumbs.map((segment, i) => {
          const target = breadcrumbs.slice(0, i + 1).join('/')
          const isLast = i === breadcrumbs.length - 1
          return (
            <span key={target} className="flex items-center gap-1">
              <span className="text-gray-600">/</span>
              {isLast ? (
                <span className="text-gray-300">{segment}</span>
              ) : (
                <button
                  onClick={() => navigateTo(target)}
                  className="text-purple-400 hover:text-purple-300"
                >
                  {segment}
                </button>
              )}
            </span>
          )
        })}
        <button
          onClick={() => fetchListing(currentPath)}
          disabled={loadingListing}
          className="ml-auto px-4 py-2 text-sm bg-gray-800 border border-gray-700 rounded-lg hover:border-purple-500/50 text-gray-300 hover:text-white transition-all disabled:opacity-50 flex items-center justify-center min-w-20"
        >
          {loadingListing ? <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" /> : 'Refresh'}
        </button>
      </div>

      {loadingListing ? (
        <div className="flex items-center justify-center py-8">
          <div className="w-6 h-6 border-2 border-purple-400 border-t-transparent rounded-full animate-spin" />
        </div>
      ) : listing.folders.length === 0 && listing.files.length === 0 ? (
        <div className="text-center py-8 text-gray-400">
          Empty directory
        </div>
      ) : (
        <div className="space-y-2">
          {listing.folders.map((folder) => (
            <button
              key={`folder:${folder}`}
              onClick={() => enterFolder(folder)}
              className="w-full flex items-center p-3 rounded-lg border bg-gray-800/50 border-gray-700 hover:border-purple-500/30 hover:bg-gray-800 transition-all duration-200 text-left"
            >
              <span className="mr-3 text-purple-400">▸</span>
              <span className="text-purple-400 hover:text-purple-300 truncate">
                {folder}
              </span>
            </button>
          ))}
          {listing.files.map((file) => (
            <div
              key={`file:${file.name}`}
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
                href={buildFileUrl(file, 'download')}
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
