import { useState, useCallback, useEffect } from 'react'

const AUTH_KEY = 'tobi2_auth'

export function useAuth() {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [conversations, setConversations] = useState([])

  useEffect(() => {
    const stored = localStorage.getItem(AUTH_KEY)
    if (stored) {
      try {
        const parsed = JSON.parse(stored)
        if (parsed?.token) {
          setUser(parsed)
          fetchConversations(parsed.token).then(setConversations)
        }
      } catch {}
    }
  }, [])

  const fetchConversations = async (token) => {
    try {
      const res = await fetch('/api/tobi2/conversations', {
        headers: { Authorization: `Bearer ${token}` }
      })
      if (!res.ok) return []
      const data = await res.json()
      return data.conversations || []
    } catch {
      return []
    }
  }

  const login = useCallback(async (username, password) => {
    setLoading(true)
    setError('')
    try {
      const res = await fetch('/api/tobi2/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
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
        role: data.role,
        email: data.email || ''
      }
      localStorage.setItem(AUTH_KEY, JSON.stringify(userData))
      setUser(userData)
      setConversations(data.conversations || [])
      return true
    } catch (err) {
      setError('Gabim në lidhje me serverin.')
      return false
    } finally {
      setLoading(false)
    }
  }, [])

  const register = useCallback(async (username, fullName, password) => {
    setLoading(true)
    setError('')
    try {
      const res = await fetch('/api/tobi2/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, fullName, password })
      })
      const data = await res.json()
      if (!data.authenticated) {
        setError(data.error || 'Regjistrimi dështoi!')
        return false
      }
      const userData = {
        token: data.token,
        userId: data.userId,
        fullName: data.fullName,
        role: data.role,
        email: ''
      }
      localStorage.setItem(AUTH_KEY, JSON.stringify(userData))
      setUser(userData)
      setConversations([])
      return true
    } catch (err) {
      setError('Gabim në lidhje me serverin.')
      return false
    } finally {
      setLoading(false)
    }
  }, [])

  const changePassword = useCallback(async (oldPassword, newPassword) => {
    const stored = localStorage.getItem(AUTH_KEY)
    if (!stored) return false
    const { token } = JSON.parse(stored)
    try {
      const res = await fetch('/api/tobi2/auth/change-password', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({ oldPassword, newPassword })
      })
      return res.ok
    } catch {
      return false
    }
  }, [])

  const refreshConversations = useCallback(async () => {
    const stored = localStorage.getItem(AUTH_KEY)
    if (!stored) return
    const { token } = JSON.parse(stored)
    const convs = await fetchConversations(token)
    setConversations(convs)
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem(AUTH_KEY)
    setUser(null)
    setConversations([])
  }, [])

  return { user, loading, error, conversations, login, register, changePassword, logout, refreshConversations }
}
