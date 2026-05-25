import { useState, useCallback, useEffect } from 'react'

const AUTH_KEY = 'tobi2_auth'

export function useAuth() {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    const stored = localStorage.getItem(AUTH_KEY)
    if (stored) {
      try {
        const parsed = JSON.parse(stored)
        if (parsed?.token) setUser(parsed)
      } catch {}
    }
  }, [])

  const login = useCallback(async (username, password, loginType = 'regular') => {
    setLoading(true)
    setError('')
    try {
      const res = await fetch('/api/tobi2/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password, loginType })
      })
      const data = await res.json()
      if (!data.authenticated) {
        setError(data.error || 'Kredencialet e gabuara!')
        return false
      }
      const userData = {
        token: data.token,
        userId: data.userId,
        fullName: data.fullName,
        role: data.role
      }
      localStorage.setItem(AUTH_KEY, JSON.stringify(userData))
      setUser(userData)
      return true
    } catch (err) {
      setError('Gabim në lidhje me serverin.')
      return false
    } finally {
      setLoading(false)
    }
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem(AUTH_KEY)
    setUser(null)
  }, [])

  return { user, loading, error, login, logout }
}
