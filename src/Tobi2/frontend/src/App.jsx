import React, { useEffect, useRef, useState, useCallback } from 'react'
import ChatMessage from './components/ChatMessage'
import ChatInput from './components/ChatInput'
import TypingIndicator from './components/TypingIndicator'
import EvaluationBadge from './components/EvaluationBadge'
import TobiIllustration from './components/TobiIllustration'
import Toast from './components/Toast'
import LoginPage from './components/LoginPage'
import ChurnPage from './components/ChurnPage'

import { useChat } from './hooks/useChat'
import { useSpeech } from './hooks/useSpeech'
import { useAuth } from './hooks/useAuth'

const suggestions = [
  { icon: '📱', label: 'Balance', hint: 'Sa kredi kam?', text: 'Sa kredi kam?' },
  { icon: '📶', label: 'Planet', hint: 'Cfare planesh?', text: 'Cfare planesh keni?' },
  { icon: '⭐', label: 'Rekomando', hint: 'Planin më të mirë', text: 'Më rekomando një plan' },
  { icon: '📄', label: 'Fatura', hint: 'Shuma e faturës', text: 'Sa eshte fatura ime?' },
  { icon: '👤', label: 'Agjent', hint: 'Flisni me njeri', text: 'Dua te flas me agjent' }
]

function App() {
  const { user, loading: authLoading, error: authError, login, logout } = useAuth()
  const [activeTab, setActiveTab] = useState('chat')

  const {
    messages,
    isLoading,
    streamingText,
    isHumanHandoff,
    evaluation,
    startSession,
    sendMessage,
    sendPhoto,
    triggerHumanHandoff
  } = useChat()

  const {
    isListening,
    isSpeaking,
    transcript,
    interimText,
    error: sttError,
    browserSupported,
    isUsingFallback,
    clearError,
    startListening,
    stopListening,
    speak,
    stopSpeaking
  } = useSpeech()

  const messagesEndRef = useRef(null)
  const messagesContainerRef = useRef(null)
  const [darkMode, setDarkMode] = useState(false)
  const [showScrollBtn, setShowScrollBtn] = useState(false)
  const [toasts, setToasts] = useState([])
  const [hasStarted, setHasStarted] = useState(false)

  const addToast = useCallback((message, type = 'error') => {
    setToasts(prev => [...prev, { id: Date.now(), message, type }])
  }, [])

  const dismissToast = useCallback((id) => {
    setToasts(prev => prev.filter(t => t.id !== id))
  }, [])

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', darkMode ? 'dark' : 'light')
  }, [darkMode])

  useEffect(() => {
    if (user && !hasStarted) { startSession(); setHasStarted(true) }
  }, [user, hasStarted, startSession])

  const scrollToBottom = (smooth = true) => {
    messagesEndRef.current?.scrollIntoView({ behavior: smooth ? 'smooth' : 'auto' })
  }

  useEffect(() => {
    if (!isLoading) scrollToBottom(true)
  }, [messages, isLoading])

  useEffect(() => {
    if (streamingText) scrollToBottom(true)
  }, [streamingText])

  useEffect(() => {
    if (transcript) sendMessage(transcript)
  }, [transcript, sendMessage])

  const handleScroll = useCallback(() => {
    const el = messagesContainerRef.current
    if (!el) return
    const isNearBottom = el.scrollHeight - el.scrollTop - el.clientHeight < 100
    setShowScrollBtn(!isNearBottom)
  }, [])

  const handleMicClick = () => {
    if (isListening) { stopListening(); return }
    clearError()
    startListening()
  }

  const handleSpeakLastBot = async (text) => {
    if (isSpeaking) { stopSpeaking(); return }
    try {
      await speak(text)
    } catch (e) {
      addToast('Gabim: ' + e.message, 'error')
    }
  }

  const handleLogin = useCallback(async (username, password, loginType) => {
    const success = await login(username, password, loginType)
    return success
  }, [login])

  const wrappedSendMessage = useCallback(async (text) => {
    try { await sendMessage(text) }
    catch { addToast('Gabim në lidhje.', 'error') }
  }, [sendMessage, addToast])

  const lastBotMessage = [...messages].reverse().find(m => m.sender === 'bot')
  const isEmpty = messages.length === 0 && !isLoading && !streamingText
  const isBotTyping = streamingText || isLoading

  const isVodafoneEmployee = user?.role === 'VODAFONE_EMPLOYEE'

  const tobiState = isListening ? 'listening'
    : isLoading || streamingText ? 'thinking'
    : isSpeaking ? 'speaking'
    : 'idle'

  if (!user) {
    return (
      <div className="app-container">
        <LoginPage onLogin={handleLogin} loading={authLoading} error={authError} />
      </div>
    )
  }

  return (
    <div className="app-container">
      <div className="phone-frame">
        <div className="phone-notch">
          <div className="notch-dynamic-island" />
        </div>
        <div className="phone-screen">
          <div className="chat-container">
        {toasts.length > 0 && (
          <div className="toast-container">
            {toasts.map(t => (
              <Toast key={t.id} {...t} onDismiss={() => dismissToast(t.id)} />
            ))}
          </div>
        )}

        <div className="app-header-bar">
          <div className="user-info">
            <div className="user-avatar">
              {user.fullName?.charAt(0) || 'U'}
            </div>
            <div className="user-details">
              <div className="user-name">
                {user.fullName}
                {isVodafoneEmployee && <span className="user-badge">Vodafone</span>}
              </div>
              <div className="user-id">{user.userId}</div>
            </div>
          </div>
          <button className="logout-btn" onClick={logout}>
            Dil
          </button>
        </div>

        {isVodafoneEmployee && (
          <div className="app-tabs">
            <button
              className={`app-tab ${activeTab === 'chat' ? 'active' : ''}`}
              onClick={() => setActiveTab('chat')}
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
              </svg>
              Chat
            </button>
            <button
              className={`app-tab ${activeTab === 'churn' ? 'active' : ''}`}
              onClick={() => setActiveTab('churn')}
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z" />
                <polyline points="13 2 13 9 20 9" />
              </svg>
              Churn
            </button>
          </div>
        )}

        {activeTab === 'churn' && isVodafoneEmployee ? (
          <ChurnPage />
        ) : (
          <>
            <div className="chat-header">
              <div className={`status-indicator ${isBotTyping ? 'typing' : ''}`} />
              <div className="header-info">
                <h1>TOBi2</h1>
                <p className="subtitle">
                  {isBotTyping ? 'duke shkruar...' : 'Vodafone Albania'}
                </p>
              </div>
              {evaluation && <EvaluationBadge evaluation={evaluation} />}
              <button
                className="theme-toggle"
                onClick={() => setDarkMode(prev => !prev)}
              >
                {darkMode ? '☀' : '☾'}
              </button>
            </div>

            <div
              className="chat-messages"
              ref={messagesContainerRef}
              onScroll={handleScroll}
            >
              {isEmpty && (
                <div className="empty-state">
                  <TobiIllustration size={100} state={tobiState} />
                  <h2>Mirë se vini, {user.fullName?.split(' ')[0] || ''}</h2>
                  <p>Pyetni për balancën, planet, faturat ose internetin.</p>
                  <div className="suggestion-cards">
                    {suggestions.map((s, i) => (
                      <button
                        key={i}
                        className="suggestion-card"
                        onClick={() => wrappedSendMessage(s.text)}
                      >
                        <span className="card-icon">{s.icon}</span>
                        <span className="card-label">{s.label}</span>
                        <span className="card-hint">{s.hint}</span>
                      </button>
                    ))}
                  </div>
                </div>
              )}

              {messages.map((msg) => (
                <ChatMessage key={msg.id} message={msg} />
              ))}

              {isLoading && streamingText && (
                <div className="message-row bot-row">
                  <TobiIllustration size={28} state="thinking" />
                  <div className="message bot streaming-cursor">{streamingText}</div>
                </div>
              )}
              {isLoading && !streamingText && (
                <div className="message-row bot-row">
                  <TobiIllustration size={28} state="thinking" />
                  <TypingIndicator />
                </div>
              )}

              <div ref={messagesEndRef} />
            </div>

            <button
              className={`scroll-bottom-btn ${showScrollBtn ? 'visible' : ''}`}
              onClick={() => scrollToBottom(true)}
            >
              ↓
            </button>

            {lastBotMessage && !isLoading && !streamingText && (
              <div className="speak-row">
                <button
                  className={`speak-btn ${isSpeaking ? 'speaking' : ''}`}
                  onClick={() => handleSpeakLastBot(lastBotMessage.text)}
                >
                  {isSpeaking ? (
                    <>
                      <span className="speak-bar speak-bar-1" />
                      <span className="speak-bar speak-bar-2" />
                      <span className="speak-bar speak-bar-3" />
                      Duke folur...
                    </>
                  ) : (
                    <>
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5" />
                        <path d="M19.07 4.93a10 10 0 0 1 0 14.14" />
                        <path d="M15.54 8.46a5 5 0 0 1 0 7.07" />
                      </svg>
                      Dëgjo
                    </>
                  )}
                </button>
              </div>
            )}

            <ChatInput
              onSend={wrappedSendMessage}
              onPhoto={sendPhoto}
              isLoading={isLoading}
              isListening={isListening}
              interimText={interimText}
              onMicClick={handleMicClick}
              isHumanHandoff={isHumanHandoff}
              onHandoffClick={triggerHumanHandoff}
              sttError={sttError}
              browserSupported={browserSupported}
              isUsingFallback={isUsingFallback}
            />
          </>
        )}
          </div>
        </div>
        <div className="phone-home-bar" />
        <div className="phone-silent-switch" />
        <div className="phone-volume-up" />
        <div className="phone-volume-down" />
        <div className="phone-power" />
      </div>
    </div>
  )
}

export default App
