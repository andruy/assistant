import { useAuth } from '../context/AuthContext'
import { Navigate } from 'react-router'

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
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold tracking-wider bg-gradient-to-r from-purple-400 to-blue-400 bg-clip-text text-transparent">
            SETTINGS
          </h1>
          <p className="text-gray-400 mt-2">Configure your preferences</p>
        </div>

        {/* Settings Sections */}
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
                <button className="px-4 py-2 text-sm rounded-lg border border-gray-600 text-gray-300 hover:border-blue-500/50 hover:text-blue-400 transition-colors">
                  Edit
                </button>
              </div>
              <div className="flex items-center justify-between p-4 rounded-lg bg-gray-800/50 border border-gray-700">
                <div>
                  <div className="text-sm text-gray-400">Email</div>
                  <div className="text-gray-200 font-medium">{user?.email || 'Not set'}</div>
                </div>
                <button className="px-4 py-2 text-sm rounded-lg border border-gray-600 text-gray-300 hover:border-blue-500/50 hover:text-blue-400 transition-colors">
                  Edit
                </button>
              </div>
            </div>
          </div>

          {/* Appearance Section */}
          <div className="bg-gray-900/60 border border-gray-800 rounded-xl p-6">
            <h2 className="text-lg font-semibold text-gray-200 mb-4 tracking-wide flex items-center gap-2">
              <span className="text-purple-400">//</span> Appearance
            </h2>
            <div className="space-y-4">
              <div className="flex items-center justify-between p-4 rounded-lg bg-gray-800/50 border border-gray-700">
                <div>
                  <div className="text-gray-200 font-medium">Dark Mode</div>
                  <div className="text-sm text-gray-400">Use dark theme</div>
                </div>
                <div className="w-12 h-6 rounded-full bg-purple-500/20 border border-purple-500/50 relative cursor-pointer">
                  <div className="absolute right-0.5 top-0.5 w-5 h-5 rounded-full bg-purple-400" />
                </div>
              </div>
              <div className="flex items-center justify-between p-4 rounded-lg bg-gray-800/50 border border-gray-700">
                <div>
                  <div className="text-gray-200 font-medium">Animations</div>
                  <div className="text-sm text-gray-400">Enable UI animations</div>
                </div>
                <div className="w-12 h-6 rounded-full bg-purple-500/20 border border-purple-500/50 relative cursor-pointer">
                  <div className="absolute right-0.5 top-0.5 w-5 h-5 rounded-full bg-purple-400" />
                </div>
              </div>
            </div>
          </div>

          {/* Security Section */}
          <div className="bg-gray-900/60 border border-gray-800 rounded-xl p-6">
            <h2 className="text-lg font-semibold text-gray-200 mb-4 tracking-wide flex items-center gap-2">
              <span className="text-violet-400">//</span> Security
            </h2>
            <div className="space-y-4">
              <button className="w-full p-4 rounded-lg bg-gray-800/50 border border-gray-700 hover:border-purple-500/50 transition-colors text-left">
                <div className="text-gray-200 font-medium">Change Password</div>
                <div className="text-sm text-gray-400">Update your password</div>
              </button>
              <button className="w-full p-4 rounded-lg bg-gray-800/50 border border-gray-700 hover:border-purple-500/50 transition-colors text-left">
                <div className="text-gray-200 font-medium">Two-Factor Authentication</div>
                <div className="text-sm text-gray-400">Add an extra layer of security</div>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
