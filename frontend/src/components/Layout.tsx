import { useState } from 'react'
import { Outlet, Link, useLocation } from 'react-router'
import { useAuth } from '../context/AuthContext'

const navLinks = [
  { to: '/', label: 'Home' },
  { to: '/about', label: 'About' },
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/settings', label: 'Settings' },
]

export default function Layout() {
  const [menuOpen, setMenuOpen] = useState(false)
  const { isAuthenticated, logout, user } = useAuth()
  const location = useLocation()

  const handleLogout = async () => {
    await logout()
    setMenuOpen(false)
  }

  return (
    <div className="min-h-screen bg-gray-950 text-gray-100 font-mono">
      {/* Header */}
      <header className="fixed top-0 left-0 right-0 z-50 bg-gray-900/95 backdrop-blur-sm border-b border-gray-800">
        <div className="flex items-center justify-between px-4 py-3">
          {/* Logo */}
          <Link
            to="/"
            className="text-xl font-bold tracking-wider bg-gradient-to-r from-blue-400 via-purple-500 to-violet-400 bg-clip-text text-transparent font-[Orbitron]"
          >
            NEXUS
          </Link>

          {/* Menu Button */}
          <button
            onClick={() => setMenuOpen(!menuOpen)}
            className="relative w-10 h-10 flex flex-col items-center justify-center gap-1.5 rounded-lg bg-gray-800 hover:bg-gray-700 border border-gray-700 hover:border-purple-500/50 transition-all duration-300 group"
            aria-label="Toggle menu"
          >
            <span
              className={`w-5 h-0.5 bg-blue-400 transition-all duration-300 ${
                menuOpen ? 'rotate-45 translate-y-2' : ''
              }`}
            />
            <span
              className={`w-5 h-0.5 bg-purple-400 transition-all duration-300 ${
                menuOpen ? 'opacity-0 scale-0' : ''
              }`}
            />
            <span
              className={`w-5 h-0.5 bg-violet-400 transition-all duration-300 ${
                menuOpen ? '-rotate-45 -translate-y-2' : ''
              }`}
            />
          </button>
        </div>

        {/* Collapsible Menu */}
        <nav
          className={`overflow-hidden transition-all duration-300 ease-in-out ${
            menuOpen ? 'max-h-96 opacity-100' : 'max-h-0 opacity-0'
          }`}
        >
          <div className="px-4 pb-4 space-y-1">
            {navLinks.map((link) => (
              <Link
                key={link.to}
                to={link.to}
                onClick={() => setMenuOpen(false)}
                className={`block px-4 py-2.5 rounded-lg transition-all duration-200 border ${
                  location.pathname === link.to
                    ? 'bg-gradient-to-r from-blue-500/20 to-purple-500/20 border-purple-500/50 text-purple-300'
                    : 'bg-gray-800/50 border-gray-700 hover:border-purple-500/50 hover:bg-gray-800 text-gray-300 hover:text-white'
                }`}
              >
                <span className="font-medium tracking-wide">{link.label}</span>
              </Link>
            ))}

            {/* Auth Section */}
            <div className="pt-2 mt-2 border-t border-gray-700">
              {isAuthenticated ? (
                <div className="space-y-2">
                  <div className="px-4 py-2 text-sm text-gray-400">
                    Logged in as{' '}
                    <span className="text-purple-400 font-medium">{user?.username}</span>
                  </div>
                  <button
                    onClick={handleLogout}
                    className="w-full px-4 py-2.5 rounded-lg bg-red-500/10 border border-red-500/30 text-red-400 hover:bg-red-500/20 hover:border-red-500/50 transition-all duration-200 font-medium tracking-wide"
                  >
                    Logout
                  </button>
                </div>
              ) : (
                <Link
                  to="/"
                  onClick={() => setMenuOpen(false)}
                  className="block px-4 py-2.5 rounded-lg bg-gradient-to-r from-blue-500/20 to-purple-500/20 border border-purple-500/30 text-purple-300 hover:border-purple-500/50 transition-all duration-200 font-medium tracking-wide text-center"
                >
                  Login
                </Link>
              )}
            </div>
          </div>
        </nav>
      </header>

      {/* Main Content */}
      <main className="pt-16 min-h-screen">
        <Outlet />
      </main>

      {/* Footer */}
      <footer className="border-t border-gray-800 bg-gray-900/50 py-6">
        <div className="container mx-auto px-4 text-center text-sm text-gray-500">
          <p className="tracking-wide">
            <span className="text-blue-500">&lt;</span>
            NEXUS
            <span className="text-purple-500">/&gt;</span> {new Date().getFullYear()}
          </p>
        </div>
      </footer>
    </div>
  )
}
