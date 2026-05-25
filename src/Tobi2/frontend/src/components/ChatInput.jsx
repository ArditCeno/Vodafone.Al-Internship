import React, { useState, useRef, useEffect } from 'react'

const quickActions = [
  { label: 'Balance', text: 'Sa kredi kam?' },
  { label: 'Top-up', text: 'Dua te bej top-up' },
  { label: 'Fatura', text: 'Sa eshte fatura ime?' },
  { label: 'Planet', text: 'Cfare planesh keni?' },
  { label: 'Rekomando', text: 'Më rekomando një plan' },
  { label: 'Agjent', text: 'Dua te flas me agjent' }
]

function ChatInput({ onSend, isLoading, isListening, interimText, onMicClick, onPhoto, isHumanHandoff, onHandoffClick, sttError, browserSupported, isUsingFallback }) {
  const [input, setInput] = useState('')
  const inputRef = useRef(null)
  const fileRef = useRef(null)

  useEffect(() => {
    if (!isLoading && inputRef.current) {
      inputRef.current.focus()
    }
  }, [isLoading])

  const handleSubmit = (e) => {
    e.preventDefault()
    if (input.trim() && !isLoading) {
      onSend(input.trim())
      setInput('')
    }
  }

  const handleFileChange = (e) => {
    const file = e.target.files?.[0]
    if (file && onPhoto) {
      onPhoto(file)
    }
    e.target.value = ''
  }

  if (isHumanHandoff) {
    return (
      <div className="chat-input-area">
        <button className="btn btn-handoff" onClick={onHandoffClick}>
          Fol me nje Agjent
        </button>
      </div>
    )
  }

  return (
    <div className="chat-input-area">
      <div className="quick-actions">
        {quickActions.map((action, i) => (
          <button
            key={i}
            className="quick-card"
            onClick={() => onSend(action.text)}
            disabled={isLoading || isListening}
          >
            {action.label}
          </button>
        ))}
      </div>
      <input
        ref={fileRef}
        type="file"
        accept="image/jpeg,image/png,image/webp"
        capture="environment"
        onChange={handleFileChange}
        className="photo-input-hidden"
      />
      <form onSubmit={handleSubmit} className="input-row">
        <input
          ref={inputRef}
          type="text"
          value={isListening && interimText ? interimText : input}
          onChange={(e) => setInput(e.target.value)}
          placeholder={isListening ? 'Duke dëgjuar...' : 'Shkruani mesazhin...'}
          disabled={isLoading}
        />
        <button
          type="button"
          className="btn btn-icon"
          onClick={() => fileRef.current?.click()}
          disabled={isLoading || isListening}
          title="Ngarko foto"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
            <circle cx="8.5" cy="8.5" r="1.5" />
            <polyline points="21 15 16 10 5 21" />
          </svg>
        </button>
        {sttError && !isListening && (
          <div className="stt-error">{sttError}</div>
        )}
        {isUsingFallback && (
          <div className="stt-error stt-info">Regjistrim audio po transkriptohet në server...</div>
        )}
        <button
          type="button"
          className={`btn btn-icon ${isListening ? 'recording' : ''}`}
          onClick={onMicClick}
          disabled={isLoading}
          title={isListening ? 'Stop recording' : browserSupported ? 'Start voice input' : 'Regjistro audio'}
        >
          {isListening ? (
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <rect x="6" y="4" width="4" height="16" />
              <rect x="14" y="4" width="4" height="16" />
            </svg>
          ) : (
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z" />
              <path d="M19 10v2a7 7 0 0 1-14 0v-2" />
              <line x1="12" y1="19" x2="12" y2="23" />
              <line x1="8" y1="23" x2="16" y2="23" />
            </svg>
          )}
        </button>
        <button
          type="submit"
          className="btn btn-primary"
          disabled={!input.trim() || isLoading || isListening}
        >
          {isLoading ? (
            <span className="btn-loading">
              <span className="spinner-sm" />
            </span>
          ) : (
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <line x1="22" y1="2" x2="11" y2="13" />
              <polygon points="22 2 15 22 11 13 2 9 22 2" />
            </svg>
          )}
        </button>
      </form>
    </div>
  )
}

export default ChatInput
