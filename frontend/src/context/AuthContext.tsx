import { createContext, useContext, useState, useEffect, useCallback, useRef, type ReactNode } from 'react'
import { useToast } from './ToastContext'

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
  checkAuth: (showLoading?: boolean) => Promise<void>
}

const AuthContext = createContext<AuthContextType | null>(null)

const API_BASE_URL = '/api'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const toast = useToast()
  const wasAuthenticated = useRef(false)

  const login = async (username: string, password: string) => {
    const response = await fetch('/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      credentials: 'include',
      body: new URLSearchParams({ username, password }),
    })

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Login failed' }))
      const message = error.message || 'Login failed'
      toast(message)
      throw new Error(message)
    }

    // Set user from response if your backend returns user data,
    // otherwise just set the username from the login form
    const data = await response.json().catch(() => null)
    setUser(data ?? { username })
  }

  const logout = async () => {
    try {
      await fetch('/logout', {
        method: 'POST',
        credentials: 'include',
      })
    } finally {
      setUser(null)
    }
  }

  const checkAuth = useCallback(async (showLoading = false) => {
    if (showLoading) {
      setIsLoading(true)
    }
    try {
      const response = await fetch(`${API_BASE_URL}/auth/me`, {
        credentials: 'include',
      })
      if (response.ok) {
        const data = await response.json()
        setUser(data)
        wasAuthenticated.current = true
      } else {
        if (wasAuthenticated.current) {
          toast('Session expired')
          wasAuthenticated.current = false
        }
        setUser(null)
      }
    } catch {
      setUser(null)
    } finally {
      if (showLoading) {
        setIsLoading(false)
      }
    }
  }, [])

  useEffect(() => {
    checkAuth(true)
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
