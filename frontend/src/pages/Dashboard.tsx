import { useAuth } from '../context/AuthContext'
import { Navigate } from 'react-router'

export default function Dashboard() {
  const { isAuthenticated, isLoading } = useAuth()

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
      <div className="max-w-6xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold tracking-wider bg-gradient-to-r from-blue-400 to-purple-400 bg-clip-text text-transparent">
            DASHBOARD
          </h1>
          <p className="text-gray-400 mt-2">System overview and analytics</p>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
          {[
            { label: 'Active Sessions', value: '24', color: 'blue' },
            { label: 'Total Users', value: '1,234', color: 'purple' },
            { label: 'Uptime', value: '99.9%', color: 'violet' },
            { label: 'API Calls', value: '50K', color: 'indigo' },
          ].map((stat) => (
            <div
              key={stat.label}
              className={`p-6 rounded-xl bg-gray-900/60 border border-gray-800 hover:border-${stat.color}-500/50 transition-colors`}
            >
              <div className={`text-3xl font-bold text-${stat.color}-400 mb-1`}>
                {stat.value}
              </div>
              <div className="text-xs text-gray-500 tracking-wider uppercase">
                {stat.label}
              </div>
            </div>
          ))}
        </div>

        {/* Content Area */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Activity Panel */}
          <div className="bg-gray-900/60 border border-gray-800 rounded-xl p-6">
            <h2 className="text-lg font-semibold text-gray-200 mb-4 tracking-wide">
              Recent Activity
            </h2>
            <div className="space-y-3">
              {['System initialized', 'User authenticated', 'Data synced', 'Cache cleared'].map(
                (activity, i) => (
                  <div
                    key={i}
                    className="flex items-center gap-3 p-3 rounded-lg bg-gray-800/50 border border-gray-700"
                  >
                    <div className="w-2 h-2 rounded-full bg-purple-400" />
                    <span className="text-sm text-gray-300">{activity}</span>
                    <span className="ml-auto text-xs text-gray-500">
                      {i + 1}m ago
                    </span>
                  </div>
                )
              )}
            </div>
          </div>

          {/* Quick Actions */}
          <div className="bg-gray-900/60 border border-gray-800 rounded-xl p-6">
            <h2 className="text-lg font-semibold text-gray-200 mb-4 tracking-wide">
              Quick Actions
            </h2>
            <div className="grid grid-cols-2 gap-3">
              {[
                { label: 'New Project', icon: '+' },
                { label: 'Run Tests', icon: '>' },
                { label: 'View Logs', icon: '#' },
                { label: 'Settings', icon: '*' },
              ].map((action) => (
                <button
                  key={action.label}
                  className="p-4 rounded-lg bg-gray-800/50 border border-gray-700 hover:border-purple-500/50 hover:bg-gray-800 transition-all text-left group"
                >
                  <span className="text-2xl text-blue-400 group-hover:text-blue-300">
                    {action.icon}
                  </span>
                  <div className="text-sm text-gray-300 mt-2">{action.label}</div>
                </button>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
