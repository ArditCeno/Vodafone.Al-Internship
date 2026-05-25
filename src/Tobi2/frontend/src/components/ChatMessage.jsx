import React from 'react'
import ReactMarkdown from 'react-markdown'
import PlanRecommendation from './PlanRecommendation'
import TobiIllustration from './TobiIllustration'
import { parsePlans } from './PlanRecommendation'

const VodafoneMark = () => (
  <img className="vf-message-mark" src="/vodafone-mark.png" alt="Vodafone" width="12" height="12" />
)

function formatTime(date) {
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

function ChatMessage({ message, isStreaming }) {
  const isBot = message.sender === 'bot'
  const time = message.timestamp || formatTime(new Date(parseInt(message.id)))
  const text = message.text || ''
  const planMatch = text.includes('Bazuar në përdorimin tuaj') || text.includes('Based on your usage')
  const plans = planMatch ? parsePlans(text) : []

  if (plans.length > 0 && isBot) {
    return (
      <div className="message-row bot-row">
        <TobiIllustration size={28} state="idle" />
        <div className={`message bot ${isStreaming ? 'streaming-cursor' : ''}`}>
          <PlanRecommendation text={text} lang="sq" />
          {!isStreaming && <span className="timestamp">{time}</span>}
          <VodafoneMark />
        </div>
      </div>
    )
  }

  return (
    <div className={`message-row ${isBot ? 'bot-row' : 'user-row'}`}>
      {isBot && <TobiIllustration size={28} state="idle" />}
      <div className={`message ${isBot ? 'bot' : 'user'} ${isStreaming ? 'streaming-cursor' : ''}`}>
        {message.image && (
          <img
            src={message.image}
            alt="uploaded photo"
            className="message-image"
            onClick={() => window.open(message.image, '_blank')}
          />
        )}
        <ReactMarkdown
          children={text}
          components={{
            a: ({ href, children }) => (
              <a href={href} target="_blank" rel="noopener noreferrer">{children}</a>
            )
          }}
        />
        {!isStreaming && <span className="timestamp">{time}</span>}
        {isBot && <VodafoneMark />}
      </div>
    </div>
  )
}

export default ChatMessage
