import { createContext, useContext, useState, useEffect, type ReactNode } from 'react'
import { supabase } from '../lib/supabase'
import type { User } from '@supabase/supabase-js'

interface StorageAuthContextType {
  user: User | null
  isLoading: boolean
  isAuthenticated: boolean
  signInWithGoogle: () => Promise<void>
  signInWithGithub: () => Promise<void>
  signOut: () => Promise<void>
}

const StorageAuthContext = createContext<StorageAuthContextType | null>(null)

function getRedirectUrl() {
  return window.location.origin + window.location.pathname
}

export function StorageAuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const { data: { subscription } } = supabase.auth.onAuthStateChange((_event, session) => {
      setUser(session?.user ?? null)
      setIsLoading(false)
    })

    // Fallback: if onAuthStateChange doesn't fire quickly (no stored session, no URL tokens)
    supabase.auth.getSession().then(({ data: { session } }) => {
      setUser(session?.user ?? null)
      setIsLoading(false)
    })

    return () => subscription.unsubscribe()
  }, [])

  const signInWithGoogle = async () => {
    await supabase.auth.signInWithOAuth({
      provider: 'google',
      options: { redirectTo: getRedirectUrl() },
    })
  }

  const signInWithGithub = async () => {
    await supabase.auth.signInWithOAuth({
      provider: 'github',
      options: { redirectTo: getRedirectUrl() },
    })
  }

  const signOut = async () => {
    await supabase.auth.signOut()
  }

  return (
    <StorageAuthContext.Provider
      value={{
        user,
        isLoading,
        isAuthenticated: !!user,
        signInWithGoogle,
        signInWithGithub,
        signOut,
      }}
    >
      {children}
    </StorageAuthContext.Provider>
  )
}

export function useStorageAuth() {
  const context = useContext(StorageAuthContext)
  if (!context) {
    throw new Error('useStorageAuth must be used within a StorageAuthProvider')
  }
  return context
}
