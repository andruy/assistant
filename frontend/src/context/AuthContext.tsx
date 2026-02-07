import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react'

interface User {
  username: string
  email?: string
}

interface AuthContextType {
  user: User | null
  isLoading: boolean
  isAuthenticated: boolean
  login: (username: string, password: string) => Promise<void>
  logout: () => Promise<void>
  checkAuth: () => Promise<void>
  authFetch: (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>
}

const AuthContext = createContext<AuthContextType | null>(null)

const API_BASE_URL = '/api'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const login = async (username: string, password: string) => {
    const response = await fetch('/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      credentials: 'include',
      body: new URLSearchParams({ username, password }),
    })

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Login failed' }))
      throw new Error(error.message || 'Login failed')
    }

    // Set user from response if your backend returns user data,
    // otherwise just set the username from the login form
    const data = await response.json().catch(() => null)
    setUser(data ?? { username })
  }

  const logout = useCallback(async () => {
    try {
      await fetch('/logout', {
        method: 'POST',
        credentials: 'include',
      })
    } finally {
      setUser(null)
    }
  }, [])

  const checkAuth = async () => {
    setIsLoading(true)
    try {
      const response = await fetch(`${API_BASE_URL}/auth/me`, {
        credentials: 'include',
      })
      if (response.ok) {
        const data = await response.json()
        setUser(data)
      } else {
        setUser(null)
      }
    } catch {
      setUser(null)
    } finally {
      setIsLoading(false)
    }
  }

  // Authenticated fetch wrapper that handles session expiry
  const authFetch = useCallback(async (input: RequestInfo | URL, init?: RequestInit): Promise<Response> => {
    const response = await fetch(input, {
      ...init,
      credentials: 'include',
    })

    // Check if session expired (401 Unauthorized or redirect to login)
    if (response.status === 401 || response.status === 403) {
      setUser(null)
    }

    // Check for redirect responses (Spring Security redirects to login page)
    if (response.redirected && response.url.includes('/login')) {
      setUser(null)
    }

    return response
  }, [])

  useEffect(() => {
    checkAuth()
  }, [])

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoading,
        isAuthenticated: !!user,
        login,
        logout,
        checkAuth,
        authFetch,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
