import { useAuth } from '../context/AuthContext'
import Welcome from '../components/Welcome'
import Login from '../components/Login'

export default function Home() {
  const { isAuthenticated, isLoading } = useAuth()

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

  return isAuthenticated ? <Welcome /> : <Login />
}
