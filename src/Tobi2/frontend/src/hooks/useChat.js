import { useState, useCallback, useRef } from 'react'

const API_BASE = '/api/tobi2'

function getAuthHeaders() {
  const stored = localStorage.getItem('tobi2_auth')
  if (!stored) return {}
  try {
    const { token } = JSON.parse(stored)
    return token ? { Authorization: `Bearer ${token}` } : {}
  } catch {
    return {}
  }
}

function getUserId() {
  const stored = localStorage.getItem('tobi2_auth')
  if (!stored) return 'anonymous'
  try {
    const { userId } = JSON.parse(stored)
    return userId || 'anonymous'
  } catch {
    return 'anonymous'
  }
}

export function useChat() {
  const [messages, setMessages] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [sessionId, setSessionId] = useState(null)
  const [isHumanHandoff, setIsHumanHandoff] = useState(false)
  const [evaluation, setEvaluation] = useState(null)
  const [streamingText, setStreamingText] = useState('')
  const abortRef = useRef(null)

  const startSession = useCallback(async () => {
    try {
      const headers = { ...getAuthHeaders() }
      const userId = getUserId()
      const stored = localStorage.getItem('tobi2_auth')
      const token = stored ? JSON.parse(stored).token : ''
      const res = await fetch(`${API_BASE}/session/start?userId=${userId}&token=${token}`, { headers })
      const data = await res.json()
      setSessionId(data.sessionId)
    } catch (err) {
      console.error('Failed to create session', err)
    }
  }, [])

  const sendMessage = useCallback(async (text) => {
    if (!text.trim() || isLoading) return

    const userMsg = { id: Date.now().toString(), text, sender: 'user' }
    setMessages(prev => [...prev, userMsg])
    setIsLoading(true)
    setStreamingText('')
    setEvaluation(null)

    let sid = sessionId
    if (!sid) {
      try {
        const headers = { ...getAuthHeaders() }
        const res = await fetch(`${API_BASE}/session/start?userId=user-${Date.now()}`, { headers })
        const data = await res.json()
        sid = data.sessionId
        setSessionId(sid)
      } catch (err) {
        console.error('Failed to create session', err)
      }
    }

    try {
      const stored = localStorage.getItem('tobi2_auth')
      const token = stored ? JSON.parse(stored).token : ''
      const eventSource = new EventSource(
        `${API_BASE}/chat/stream?message=${encodeURIComponent(text)}&sessionId=${sid}&language=sq&token=${token}`
      )

      let fullText = ''

      eventSource.onmessage = (event) => {
        fullText += event.data
        setStreamingText(fullText)
      }

      eventSource.onerror = () => {
        eventSource.close()
        const botMsg = {
          id: (Date.now() + 1).toString(),
          text: fullText || 'Më falni, ndodhi një gabim. Ju lutem provoni përsëri.',
          sender: 'bot'
        }
        setMessages(prev => [...prev, botMsg])
        setStreamingText('')
        setIsLoading(false)
      }

      eventSource.addEventListener('complete', () => {
        eventSource.close()
        const botMsg = {
          id: (Date.now() + 1).toString(),
          text: fullText,
          sender: 'bot'
        }
        setMessages(prev => [...prev, botMsg])
        setStreamingText('')
        setIsLoading(false)

        if (fullText.toLowerCase().includes('talk to an agent') ||
            fullText.toLowerCase().includes('fol me nje agjent') ||
            fullText.toLowerCase().includes('lidh me agjent')) {
          setIsHumanHandoff(true)
        }
      })

      abortRef.current = () => {
        eventSource.close()
        setIsLoading(false)
        setStreamingText('')
      }
    } catch (err) {
      console.error('Chat error', err)
      setMessages(prev => [...prev, {
        id: (Date.now() + 1).toString(),
        text: 'Më falni, ndodhi një gabim. Ju lutem provoni përsëri.',
        sender: 'bot'
      }])
      setIsLoading(false)
    }
  }, [isLoading, sessionId])

  const sendNonStreamingMessage = useCallback(async (text) => {
    if (!text.trim() || isLoading) return

    const userMsg = { id: Date.now().toString(), text, sender: 'user' }
    setMessages(prev => [...prev, userMsg])
    setIsLoading(true)
    setEvaluation(null)

    let sid = sessionId
    if (!sid) {
      try {
        const headers = { ...getAuthHeaders() }
        const res = await fetch(`${API_BASE}/session/start?userId=user-${Date.now()}`, { headers })
        const data = await res.json()
        sid = data.sessionId
        setSessionId(sid)
      } catch (err) {
        console.error('Failed to create session', err)
      }
    }

    try {
      const headers = {
        'Content-Type': 'application/json',
        ...getAuthHeaders()
      }
      const res = await fetch(`${API_BASE}/chat`, {
        method: 'POST',
        headers,
        body: JSON.stringify({
          message: text,
          sessionId: sid,
          userId: `user-${Date.now()}`,
          language: 'sq'
        })
      })
      const data = await res.json()
      const botMsg = {
        id: (Date.now() + 1).toString(),
        text: data.message,
        sender: 'bot'
      }
      setMessages(prev => [...prev, botMsg])

      if (data.evaluation) {
        setEvaluation(data.evaluation)
      }

      if (data.message.toLowerCase().includes('talk to an agent') ||
          data.message.toLowerCase().includes('fol me nje agjent')) {
        setIsHumanHandoff(true)
      }
    } catch (err) {
      console.error('Chat error', err)
      setMessages(prev => [...prev, {
        id: (Date.now() + 1).toString(),
        text: 'Më falni, ndodhi një gabim. Ju lutem provoni përsëri.',
        sender: 'bot'
      }])
    }
    setIsLoading(false)
  }, [isLoading, sessionId])

  const triggerHumanHandoff = useCallback(() => {
    setIsHumanHandoff(true)
    setMessages(prev => [...prev, {
      id: Date.now().toString(),
      text: '**Po ju lidhim me nje agjent...**\n\nNje agjent do t\'ju kontaktoje se shpejti. Faleminderit per durimin!',
      sender: 'bot'
    }])
  }, [])

  const sendPhoto = useCallback(async (file) => {
    if (!file || isLoading) return

    const previewUrl = URL.createObjectURL(file)
    const userMsg = { id: Date.now().toString(), text: `📷 ${file.name}`, sender: 'user', image: previewUrl }
    setMessages(prev => [...prev, userMsg])
    setIsLoading(true)

    let sid = sessionId
    if (!sid) {
      try {
        const res = await fetch(`${API_BASE}/session/start?userId=user-${Date.now()}`, { headers: { ...getAuthHeaders() } })
        const data = await res.json()
        sid = data.sessionId
        setSessionId(sid)
      } catch {
        setIsLoading(false)
        return
      }
    }

    try {
      const formData = new FormData()
      formData.append('photo', file)
      formData.append('sessionId', sid)
      formData.append('language', 'sq')
      const auth = getAuthHeaders()
      const res = await fetch(`${API_BASE}/chat/photo`, {
        method: 'POST',
        headers: auth,
        body: formData
      })
      const data = await res.json()
      if (!res.ok) {
        throw new Error(data.error || `Status ${res.status}`)
      }
      const botMsg = {
        id: (Date.now() + 1).toString(),
        text: data.message,
        sender: 'bot',
        attachedFile: data.attachedFile
      }
      setMessages(prev => [...prev, botMsg])
    } catch (err) {
      console.error('Photo upload error', err)
      setMessages(prev => [...prev, {
        id: (Date.now() + 1).toString(),
        text: err.message || 'Gabim në ngarkimin e fotos.',
        sender: 'bot'
      }])
    }
    setIsLoading(false)
  }, [isLoading, sessionId])

  const cancelStream = useCallback(() => {
    if (abortRef.current) {
      abortRef.current()
    }
  }, [])

  return {
    messages,
    isLoading,
    streamingText,
    sessionId,
    isHumanHandoff,
    evaluation,
    startSession,
    sendMessage,
    sendNonStreamingMessage,
    sendPhoto,
    triggerHumanHandoff,
    cancelStream
  }
}
