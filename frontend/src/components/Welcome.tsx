import { Link } from 'react-router'
import { useAuth } from '../context/AuthContext'

export default function Welcome() {
  const { user } = useAuth()

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

          {/* Quick Actions */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 max-w-md mx-auto">
            <Link
              to="/dashboard"
              className="group px-6 py-4 rounded-xl bg-linear-to-br from-blue-500/10 to-blue-500/5 border border-blue-500/30 hover:border-blue-500/60 transition-all duration-300"
            >
              <div className="text-blue-400 font-semibold tracking-wide group-hover:text-blue-300 transition-colors">
                Dashboard
              </div>
              <div className="text-xs text-gray-500 mt-1">View analytics</div>
            </Link>

            <Link
              to="/settings"
              className="group px-6 py-4 rounded-xl bg-linear-to-br from-purple-500/10 to-purple-500/5 border border-purple-500/30 hover:border-purple-500/60 transition-all duration-300"
            >
              <div className="text-purple-400 font-semibold tracking-wide group-hover:text-purple-300 transition-colors">
                Settings
              </div>
              <div className="text-xs text-gray-500 mt-1">Configure system</div>
            </Link>
          </div>
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
