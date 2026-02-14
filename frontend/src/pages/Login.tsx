import { useState, type FormEvent } from 'react'
import { useAuth } from '../context/AuthContext'
import { Navigate } from 'react-router'

export default function Login() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const { login, isAuthenticated, isLoading } = useAuth()

  if (isLoading) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center">
        <div className="flex flex-col items-center gap-4">
          <div className="relative w-16 h-16">
            <div className="absolute inset-0 rounded-full border-2 border-gray-700" />
            <div className="absolute inset-0 rounded-full border-2 border-transparent border-t-blue-400 animate-spin" />
            <div className="absolute inset-2 rounded-full border-2 border-transparent border-t-purple-400 animate-spin animation-delay-150" style={{ animationDirection: 'reverse' }} />
            <div className="absolute inset-4 rounded-full border-2 border-transparent border-t-violet-400 animate-spin" />
          </div>
          <p className="text-sm text-gray-400 tracking-wider animate-pulse font-mono">
            INITIALIZING...
          </p>
        </div>
      </div>
    )
  }

  if (isAuthenticated) {
    return <Navigate to="/" replace />
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError('')
    setIsSubmitting(true)

    try {
      await login(username, password)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen bg-slate-950 text-gray-100 font-mono flex items-center justify-center p-4 relative overflow-hidden">
      {/* Background decoration */}
      <div className="absolute inset-0 pointer-events-none">
        <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-purple-500/5 rounded-full blur-3xl" />
        <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-blue-500/5 rounded-full blur-3xl" />
      </div>

      <div className="w-full max-w-md relative z-10">
        {/* Logo */}
        <div className="text-center mb-8">
          <h2 className="text-2xl font-bold tracking-wider bg-linear-to-r from-blue-400 via-purple-500 to-violet-400 bg-clip-text text-transparent font-[Orbitron]">
            NEXUS
          </h2>
        </div>

        {/* Card */}
        <div className="bg-gray-900/80 backdrop-blur-sm border border-gray-800 rounded-2xl p-8 shadow-2xl shadow-purple-500/5">
          {/* Header */}
          <div className="text-center mb-8">
            <h1 className="text-3xl font-bold tracking-wider bg-linear-to-r from-blue-400 via-purple-400 to-violet-400 bg-clip-text text-transparent">
              ACCESS PORTAL
            </h1>
            <p className="mt-2 text-gray-400 text-sm tracking-wide">
              Enter your credentials to continue
            </p>
          </div>

          {/* Error Message */}
          {error && (
            <div className="mb-6 px-4 py-3 rounded-lg bg-red-500/10 border border-red-500/30 text-red-400 text-sm">
              {error}
            </div>
          )}

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="space-y-2">
              <label htmlFor="username" className="block text-sm font-medium text-gray-300 tracking-wide">
                USERNAME
              </label>
              <input
                id="username"
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
                className="w-full px-4 py-3 rounded-lg bg-gray-800 border border-gray-700 text-white placeholder-gray-500 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500/50 transition-all duration-200"
                placeholder="Enter username"
              />
            </div>

            <div className="space-y-2">
              <label htmlFor="password" className="block text-sm font-medium text-gray-300 tracking-wide">
                PASSWORD
              </label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="w-full px-4 py-3 rounded-lg bg-gray-800 border border-gray-700 text-white placeholder-gray-500 focus:outline-none focus:border-purple-500 focus:ring-1 focus:ring-purple-500/50 transition-all duration-200"
                placeholder="Enter password"
              />
            </div>

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full py-3 px-4 rounded-lg font-medium tracking-wider text-white bg-linear-to-r from-blue-600 via-purple-600 to-blue-600 hover:from-blue-500 hover:via-purple-500 hover:to-blue-500 focus:outline-none focus:ring-2 focus:ring-purple-500/50 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-300 shadow-lg shadow-purple-500/25"
            >
              {isSubmitting ? (
                <span className="flex items-center justify-center gap-2">
                  <svg className="animate-spin h-5 w-5" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                  </svg>
                  AUTHENTICATING...
                </span>
              ) : (
                'INITIALIZE SESSION'
              )}
            </button>
          </form>

          {/* Decorative Elements */}
          <div className="mt-8 flex items-center justify-center gap-2">
            <div className="h-px flex-1 bg-linear-to-r from-transparent via-gray-700 to-transparent" />
            <span className="text-xs text-gray-500 tracking-widest">SECURE</span>
            <div className="h-px flex-1 bg-linear-to-r from-transparent via-gray-700 to-transparent" />
          </div>
        </div>

        {/* Bottom decorative line */}
        <div className="mt-6 flex items-center justify-center">
          <div className="h-px w-32 bg-linear-to-r from-transparent via-purple-500/30 to-transparent" />
        </div>
      </div>
    </div>
  )
}
