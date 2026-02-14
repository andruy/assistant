import { useAuth } from '../context/AuthContext'
import { Navigate } from 'react-router'

export default function Home() {
  const { isAuthenticated, isLoading, user } = useAuth()

  if (isLoading) {
    return (
      <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center">
        <div className="flex flex-col items-center gap-4">
          {/* Loading Spinner */}
          <div className="relative w-16 h-16">
            <div className="absolute inset-0 rounded-full border-2 border-gray-700" />
            <div className="absolute inset-0 rounded-full border-2 border-transparent border-t-blue-400 animate-spin" />
            <div className="absolute inset-2 rounded-full border-2 border-transparent border-t-purple-400 animate-spin animation-delay-150" style={{ animationDirection: 'reverse' }} />
            <div className="absolute inset-4 rounded-full border-2 border-transparent border-t-violet-400 animate-spin" />
          </div>
          <p className="text-sm text-gray-400 tracking-wider animate-pulse">
            INITIALIZING...
          </p>
        </div>
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  return (
    <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center p-4">
      <div className="max-w-2xl w-full text-center">
        {/* Welcome Card */}
        <div className="bg-gray-900/60 backdrop-blur-sm border border-gray-800 rounded-2xl p-8 md:p-12 shadow-2xl shadow-purple-500/5">
          {/* Status Indicator */}
          <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-purple-500/10 border border-purple-500/30 mb-6">
            <span className="w-2 h-2 rounded-full bg-purple-400 animate-pulse" />
            <span className="text-xs font-medium text-purple-400 tracking-wider">SYSTEM ONLINE</span>
          </div>

          {/* Greeting */}
          <h1 className="text-4xl md:text-5xl font-bold mb-4">
            <span className="text-gray-100">Welcome back,</span>
            <br />
            <span className="bg-linear-to-r from-blue-400 via-purple-400 to-violet-400 bg-clip-text text-transparent">
              {user?.username || 'User'}
            </span>
          </h1>

          <p className="text-gray-400 text-lg mb-8 max-w-md mx-auto leading-relaxed">
            Your session is active. Access all system features from the navigation menu.
          </p>
        </div>

        {/* Decorative Grid Lines */}
        <div className="mt-12 flex items-center justify-center gap-4 text-gray-700">
          <div className="h-px w-16 bg-linear-to-r from-transparent to-gray-700" />
          <svg className="w-4 h-4" viewBox="0 0 16 16" fill="currentColor">
            <path d="M8 0l2 6h6l-5 4 2 6-5-4-5 4 2-6-5-4h6z" />
          </svg>
          <div className="h-px w-16 bg-linear-to-l from-transparent to-gray-700" />
        </div>
      </div>
    </div>
  )
}
