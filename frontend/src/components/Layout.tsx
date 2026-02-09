import { useState, useEffect } from 'react'
import { Outlet, Link, useLocation } from 'react-router'
import { useAuth } from '../context/AuthContext'

const publicLinks = [
  { to: '/', label: 'Home', icon: '⌂' },
  { to: '/about', label: 'About', icon: 'ℹ' },
]

const privateCategories = [
  {
    name: 'General',
    links: [
      { to: '/', label: 'Home', icon: '⌂' },
      { to: '/about', label: 'About', icon: 'ℹ' },
    ]
  },
  {
    name: 'Tools',
    links: [
      { to: '/apple', label: 'Apple', icon: '' },
      { to: '/microsoft', label: 'Microsoft', icon: '⊞' },
      { to: '/folder', label: 'Folder', icon: '📁' },
      { to: '/instagram', label: 'Instagram', icon: '📷' },
      { to: '/media', label: 'Media', icon: '🎬' },
    ]
  },
  {
    name: 'Scheduler',
    links: [
      { to: '/hourglass', label: 'Hourglass', icon: '⧗' },
      { to: '/calendar', label: 'Calendar', icon: '📅' },
      { to: '/notepad', label: 'Notepad', icon: '📝' },
      { to: '/terminal', label: 'Terminal', icon: '💻' },
    ]
  },
]

export default function Layout() {
  const [menuOpen, setMenuOpen] = useState(false)
  const { isAuthenticated, logout, user, checkAuth } = useAuth()
  const location = useLocation()

  // Check auth status and scroll to top on every route change
  useEffect(() => {
    window.scrollTo(0, 0)
    checkAuth()
  }, [location.pathname, checkAuth])

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
            className="text-xl font-bold tracking-wider bg-linear-to-r from-blue-400 via-purple-500 to-violet-400 bg-clip-text text-transparent font-[Orbitron]"
          >
            NEXUS
          </Link>

          {/* Right side - User info + Menu */}
          <div className="flex items-center gap-3">
            {isAuthenticated && (
              <span className="text-xs text-gray-400 hidden sm:block">
                <span className="text-purple-400">{user?.username}</span>
              </span>
            )}

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
        </div>
      </header>

      {/* Overlay */}
      {menuOpen && (
        <div
          className="fixed inset-0 bg-black/50 z-40 backdrop-blur-sm"
          onClick={() => setMenuOpen(false)}
        />
      )}

      {/* Slide-out Menu Panel */}
      <nav
        className={`fixed top-0 right-0 h-full w-72 bg-gray-900 border-l border-gray-800 z-50 transform transition-transform duration-300 ease-in-out ${
          menuOpen ? 'translate-x-0' : 'translate-x-full'
        }`}
      >
        <div className="flex flex-col h-full">
          {/* Menu Header */}
          <div className="flex items-center justify-between p-4 border-b border-gray-800">
            <span className="text-sm font-medium text-gray-400">Navigation</span>
            <button
              onClick={() => setMenuOpen(false)}
              className="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-800 text-gray-400 hover:text-white transition-colors"
            >
              ✕
            </button>
          </div>

          {/* Scrollable Menu Content */}
          <div className="flex-1 overflow-y-auto p-4 space-y-6">
            {isAuthenticated ? (
              // Private menu - categorized
              privateCategories.map((category) => (
                <div key={category.name}>
                  <h3 className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2 px-1">
                    {category.name}
                  </h3>
                  <div className="grid grid-cols-2 gap-2">
                    {category.links.map((link) => (
                      <Link
                        key={link.to}
                        to={link.to}
                        onClick={() => setMenuOpen(false)}
                        className={`flex flex-col items-center justify-center p-3 rounded-lg transition-all duration-200 border ${
                          location.pathname === link.to
                            ? 'bg-purple-500/20 border-purple-500/50 text-purple-300'
                            : 'bg-gray-800/50 border-gray-700 hover:border-purple-500/30 hover:bg-gray-800 text-gray-300 hover:text-white'
                        }`}
                      >
                        <span className="text-xl mb-1">{link.icon}</span>
                        <span className="text-xs font-medium">{link.label}</span>
                      </Link>
                    ))}
                  </div>
                </div>
              ))
            ) : (
              // Public menu - simple list
              <div>
                <h3 className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2 px-1">
                  Menu
                </h3>
                <div className="grid grid-cols-2 gap-2">
                  {publicLinks.map((link) => (
                    <Link
                      key={link.to}
                      to={link.to}
                      onClick={() => setMenuOpen(false)}
                      className={`flex flex-col items-center justify-center p-3 rounded-lg transition-all duration-200 border ${
                        location.pathname === link.to
                          ? 'bg-purple-500/20 border-purple-500/50 text-purple-300'
                          : 'bg-gray-800/50 border-gray-700 hover:border-purple-500/30 hover:bg-gray-800 text-gray-300 hover:text-white'
                      }`}
                    >
                      <span className="text-xl mb-1">{link.icon}</span>
                      <span className="text-xs font-medium">{link.label}</span>
                    </Link>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Auth Section at Bottom */}
          <div className="p-4 border-t border-gray-800">
            {isAuthenticated ? (
              <div className="space-y-3">
                <div className="flex items-center gap-2 px-2">
                  <div className="w-8 h-8 rounded-full bg-purple-500/20 border border-purple-500/30 flex items-center justify-center text-purple-400 text-sm">
                    {user?.username?.charAt(0).toUpperCase()}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="text-sm font-medium text-white truncate">{user?.username}</div>
                    <div className="text-xs text-gray-500">Logged in</div>
                  </div>
                </div>
                <button
                  onClick={handleLogout}
                  className="w-full px-4 py-2 rounded-lg bg-red-500/10 border border-red-500/30 text-red-400 hover:bg-red-500/20 hover:border-red-500/50 transition-all duration-200 text-sm font-medium"
                >
                  Logout
                </button>
              </div>
            ) : (
              <Link
                to="/"
                onClick={() => setMenuOpen(false)}
                className="block w-full px-4 py-2 rounded-lg bg-purple-500/20 border border-purple-500/30 text-purple-300 hover:border-purple-500/50 transition-all duration-200 text-sm font-medium text-center"
              >
                Login
              </Link>
            )}
          </div>
        </div>
      </nav>

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
