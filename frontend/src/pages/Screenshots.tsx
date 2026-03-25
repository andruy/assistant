import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { Navigate } from 'react-router'

const API_BASE_URL = '/api/instagram'

export default function Screenshots() {
  const { isAuthenticated, isLoading } = useAuth()
  const [runs, setRuns] = useState<string[]>([])
  const [loadingRuns, setLoadingRuns] = useState(true)
  const [expandedRun, setExpandedRun] = useState<string | null>(null)
  const [images, setImages] = useState<string[]>([])
  const [loadingImages, setLoadingImages] = useState(false)

  useEffect(() => {
    if (!isAuthenticated) return
    fetch(`${API_BASE_URL}/screenshots`)
      .then(res => res.json())
      .then(setRuns)
      .catch(console.error)
      .finally(() => setLoadingRuns(false))
  }, [isAuthenticated])

  async function toggleRun(run: string) {
    if (expandedRun === run) {
      setExpandedRun(null)
      setImages([])
      return
    }
    setExpandedRun(run)
    setLoadingImages(true)
    try {
      const res = await fetch(`${API_BASE_URL}/screenshots/${run}`)
      const data: string[] = await res.json()
      setImages(data)
    } catch (e) {
      console.error(e)
    } finally {
      setLoadingImages(false)
    }
  }

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
      {loadingRuns ? (
        <div className="flex items-center justify-center py-8">
          <div className="w-6 h-6 border-2 border-purple-400 border-t-transparent rounded-full animate-spin" />
        </div>
      ) : runs.length === 0 ? (
        <p className="text-center text-gray-500">No screenshot runs yet</p>
      ) : (
        <div className="space-y-3">
          {runs.map(run => (
            <div key={run}>
              <button
                onClick={() => toggleRun(run)}
                className={`
                  w-full text-left px-5 py-3
                  bg-gray-800/50 border rounded-lg
                  hover:border-purple-500/50 hover:bg-gray-800 transition-all duration-200
                  ${expandedRun === run ? 'border-purple-500/50 bg-gray-800' : 'border-gray-700'}
                `}
              >
                <span className="text-purple-400 font-mono">{run.replace(/_/g, ' ').replace(/-/g, ':').replace(' ', '  ')}</span>
                <span className="text-gray-500 ml-3">{expandedRun === run ? '▾' : '▸'}</span>
              </button>

              {expandedRun === run && (
                <div className="mt-2 ml-4 space-y-4">
                  {loadingImages ? (
                    <div className="flex items-center justify-center py-4">
                      <div className="w-5 h-5 border-2 border-purple-400 border-t-transparent rounded-full animate-spin" />
                    </div>
                  ) : images.map(img => (
                    <div key={img} className="space-y-1">
                      <p className="text-sm text-gray-400 font-mono">{img.replace('.png', '')}</p>
                      <img
                        src={`${API_BASE_URL}/screenshots/${run}/${img}`}
                        alt={img}
                        className="rounded-lg border border-gray-700 w-full"
                      />
                    </div>
                  ))}
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
