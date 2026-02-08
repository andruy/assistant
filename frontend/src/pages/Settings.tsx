import { useAuth } from '../context/AuthContext'
import { Navigate } from 'react-router'
import PasskeyManager from '../components/PasskeyManager'

export default function Settings() {
  const { isAuthenticated, isLoading, user } = useAuth()

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
    <div className="min-h-[calc(100vh-4rem)] p-4 md:p-8">
      <div className="max-w-3xl mx-auto">
        <div className="mb-8">
          <h1 className="text-3xl font-bold tracking-wider bg-linear-to-r from-purple-400 to-blue-400 bg-clip-text text-transparent">
            SETTINGS
          </h1>
          <p className="text-gray-400 mt-2">Configure your preferences</p>
        </div>

        <div className="space-y-6">
          {/* Profile Section */}
          <div className="bg-gray-900/60 border border-gray-800 rounded-xl p-6">
            <h2 className="text-lg font-semibold text-gray-200 mb-4 tracking-wide flex items-center gap-2">
              <span className="text-blue-400">//</span> Profile
            </h2>
            <div className="space-y-4">
              <div className="flex items-center justify-between p-4 rounded-lg bg-gray-800/50 border border-gray-700">
                <div>
                  <div className="text-sm text-gray-400">Username</div>
                  <div className="text-gray-200 font-medium">{user?.username}</div>
                </div>
              </div>
            </div>
          </div>

          {/* Security Section */}
          <div className="bg-gray-900/60 border border-gray-800 rounded-xl p-6">
            <h2 className="text-lg font-semibold text-gray-200 mb-4 tracking-wide flex items-center gap-2">
              <span className="text-violet-400">//</span> Passkeys
            </h2>
            <PasskeyManager />
          </div>
        </div>
      </div>
    </div>
  )
}
