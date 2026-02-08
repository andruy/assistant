import {
  startRegistration,
  startAuthentication,
  browserSupportsWebAuthn,
} from '@simplewebauthn/browser'
import type {
  PublicKeyCredentialCreationOptionsJSON,
  PublicKeyCredentialRequestOptionsJSON,
} from '@simplewebauthn/browser'

export { browserSupportsWebAuthn }

export async function registerPasskey(label: string): Promise<{ success: boolean; error?: string }> {
  const optionsRes = await fetch('/api/passkey/register/options', {
    method: 'POST',
    credentials: 'include',
  })
  if (!optionsRes.ok) {
    const err = await optionsRes.json().catch(() => ({}))
    return { success: false, error: err.message || 'Failed to get registration options' }
  }
  const options: PublicKeyCredentialCreationOptionsJSON = await optionsRes.json()

  let registration
  try {
    registration = await startRegistration({ optionsJSON: options })
  } catch (err) {
    if (err instanceof Error && err.name === 'NotAllowedError') {
      return { success: false, error: 'Registration was cancelled' }
    }
    return { success: false, error: 'Authenticator error: ' + (err instanceof Error ? err.message : 'Unknown') }
  }

  const verifyRes = await fetch('/api/passkey/register/verify', {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ credential: registration, label }),
  })
  if (!verifyRes.ok) {
    const err = await verifyRes.json().catch(() => ({}))
    return { success: false, error: err.message || 'Registration verification failed' }
  }

  return { success: true }
}

export async function authenticateWithPasskey(): Promise<{
  success: boolean
  username?: string
  error?: string
}> {
  const optionsRes = await fetch('/api/passkey/authenticate/options', {
    method: 'POST',
    credentials: 'include',
  })
  if (!optionsRes.ok) {
    const err = await optionsRes.json().catch(() => ({}))
    return { success: false, error: err.message || 'Failed to get authentication options' }
  }
  const options: PublicKeyCredentialRequestOptionsJSON = await optionsRes.json()

  let authentication
  try {
    authentication = await startAuthentication({ optionsJSON: options })
  } catch (err) {
    if (err instanceof Error && err.name === 'NotAllowedError') {
      return { success: false, error: 'Authentication was cancelled' }
    }
    return { success: false, error: 'Authenticator error: ' + (err instanceof Error ? err.message : 'Unknown') }
  }

  const verifyRes = await fetch('/api/passkey/authenticate/verify', {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ credential: authentication }),
  })
  if (!verifyRes.ok) {
    const err = await verifyRes.json().catch(() => ({}))
    return { success: false, error: err.message || 'Authentication failed' }
  }

  const data = await verifyRes.json()
  return { success: true, username: data.username }
}

export interface PasskeyInfo {
  id: number
  label: string
  createdAt: string
  lastUsedAt: string | null
}

export async function listPasskeys(): Promise<PasskeyInfo[]> {
  const res = await fetch('/api/passkey/list', { credentials: 'include' })
  if (!res.ok) throw new Error('Failed to fetch passkeys')
  return res.json()
}

export async function deletePasskey(id: number): Promise<void> {
  const res = await fetch(`/api/passkey/${id}`, {
    method: 'DELETE',
    credentials: 'include',
  })
  if (!res.ok) throw new Error('Failed to delete passkey')
}
