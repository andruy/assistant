import { useState, useEffect, useCallback } from 'react'
import {
  browserSupportsWebAuthn,
  registerPasskey,
  listPasskeys,
  deletePasskey,
  type PasskeyInfo,
} from '../utils/passkey'

export default function PasskeyManager() {
  const [passkeys, setPasskeys] = useState<PasskeyInfo[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [isRegistering, setIsRegistering] = useState(false)
  const [label, setLabel] = useState('')
  const [showRegister, setShowRegister] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const supportsWebAuthn = browserSupportsWebAuthn()

  const loadPasskeys = useCallback(async () => {
    try {
      const data = await listPasskeys()
      setPasskeys(data)
    } catch {
      setError('Failed to load passkeys')
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    loadPasskeys()
  }, [loadPasskeys])

  const handleRegister = async () => {
    setIsRegistering(true)
    setError('')
    setSuccess('')
    const result = await registerPasskey(label || 'My Passkey')
    if (result.success) {
      setSuccess('Passkey registered successfully!')
      setShowRegister(false)
      setLabel('')
      await loadPasskeys()
    } else {
      setError(result.error || 'Registration failed')
    }
    setIsRegistering(false)
  }

  const handleDelete = async (id: number) => {
    setError('')
    setSuccess('')
    try {
      await deletePasskey(id)
      setPasskeys(prev => prev.filter(p => p.id !== id))
      setSuccess('Passkey removed')
    } catch {
      setError('Failed to delete passkey')
    }
  }

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    })
  }

  if (!supportsWebAuthn) {
    return (
      <div className="p-4 rounded-lg bg-yellow-500/10 border border-yellow-500/30 text-yellow-400 text-sm">
        Passkeys require a secure context (HTTPS). Connect via HTTPS to enable passkey support.
      </div>
    )
  }

  return (
    <div className="space-y-4">
      {error && (
        <div className="px-4 py-3 rounded-lg bg-red-500/10 border border-red-500/30 text-red-400 text-sm">
          {error}
        </div>
      )}
      {success && (
        <div className="px-4 py-3 rounded-lg bg-green-500/10 border border-green-500/30 text-green-400 text-sm">
          {success}
        </div>
      )}

      {isLoading ? (
        <div className="flex justify-center py-4">
          <div className="w-6 h-6 border-2 border-purple-400 border-t-transparent rounded-full animate-spin" />
        </div>
      ) : (
        <>
          {passkeys.length > 0 && (
            <div className="space-y-2">
              {passkeys.map(pk => (
                <div
                  key={pk.id}
                  className="flex items-center justify-between p-4 rounded-lg bg-gray-800/50 border border-gray-700"
                >
                  <div className="min-w-0 flex-1">
                    <div className="text-gray-200 font-medium">{pk.label}</div>
                    <div className="text-xs text-gray-500 mt-1">
                      Added {formatDate(pk.createdAt)}
                      {pk.lastUsedAt && <> &middot; Last used {formatDate(pk.lastUsedAt)}</>}
                    </div>
                  </div>
                  <button
                    onClick={() => handleDelete(pk.id)}
                    className="ml-4 px-3 py-1.5 text-xs rounded-lg border border-red-500/30 text-red-400 hover:bg-red-500/10 hover:border-red-500/50 transition-colors"
                  >
                    Remove
                  </button>
                </div>
              ))}
            </div>
          )}

          {showRegister ? (
            <div className="p-4 rounded-lg bg-gray-800/50 border border-gray-700 space-y-3">
              <input
                type="text"
                value={label}
                onChange={e => setLabel(e.target.value)}
                placeholder="Passkey name (e.g. MacBook Pro)"
                className="w-full px-3 py-2 rounded-lg bg-gray-800 border border-gray-600 text-white placeholder-gray-500 text-sm focus:outline-none focus:border-purple-500 focus:ring-1 focus:ring-purple-500/50 transition-all"
              />
              <div className="flex gap-2">
                <button
                  onClick={handleRegister}
                  disabled={isRegistering}
                  className="flex-1 py-2 px-4 rounded-lg text-sm font-medium text-white bg-purple-600 hover:bg-purple-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                >
                  {isRegistering ? 'Registering...' : 'Register'}
                </button>
                <button
                  onClick={() => { setShowRegister(false); setLabel('') }}
                  className="py-2 px-4 rounded-lg text-sm font-medium text-gray-400 border border-gray-600 hover:border-gray-500 hover:text-gray-300 transition-colors"
                >
                  Cancel
                </button>
              </div>
            </div>
          ) : (
            <button
              onClick={() => setShowRegister(true)}
              className="w-full p-4 rounded-lg bg-gray-800/50 border border-gray-700 hover:border-purple-500/50 transition-colors text-left"
            >
              <div className="text-gray-200 font-medium">Add Passkey</div>
              <div className="text-sm text-gray-400">Use biometrics to sign in without a password</div>
            </button>
          )}
        </>
      )}
    </div>
  )
}
