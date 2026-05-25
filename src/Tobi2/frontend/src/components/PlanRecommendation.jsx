import React from 'react'

function parsePlans(text) {
  const lines = text.split('\n').map(l => l.trim()).filter(Boolean)
  const plans = []
  let current = null
  for (const line of lines) {
    if (line.startsWith('⭐') || line.startsWith('•')) {
      if (current) plans.push(current)
      const nameMatch = line.match(/\*\*(.+?)\*\*/)
      current = {
        recommended: line.startsWith('⭐'),
        name: nameMatch ? nameMatch[1] : '',
        price: '',
        fit: '',
        reason: ''
      }
      const savingsMatch = line.match(/kurseni (\d+)/)
      if (savingsMatch) current.savings = parseInt(savingsMatch[1])
    } else if (current) {
      if (!current.price) {
        const priceMatch = line.match(/(\d+)\s*Lek/)
        if (priceMatch) current.price = priceMatch[1]
        const fitMatch = line.match(/(\d+)%\s*përputhje/)
        if (fitMatch) current.fit = fitMatch[1]
        if (line.includes('kursim')) current.reason = 'save_money'
        else if (line.includes('të dhëna')) current.reason = 'more_data'
        else if (line.includes('5G')) current.reason = 'unlock_5g'
        else if (line.includes('roaming')) current.reason = 'roaming'
        else if (line.includes('përshtatje')) current.reason = 'better_fit'
      }
    }
  }
  if (current) plans.push(current)
  return plans
}

const REASON_LABELS = {
  save_money: { sq: 'Kursim i madh', en: 'Great savings', icon: '💰' },
  more_data: { sq: 'Më shumë të dhëna', en: 'More data', icon: '📶' },
  unlock_5g: { sq: 'Përfshin 5G', en: 'Includes 5G', icon: '⚡' },
  roaming: { sq: 'Përfshin roaming', en: 'Includes roaming', icon: '🌍' },
  better_fit: { sq: 'Përshtatje më e mirë', en: 'Better fit', icon: '🎯' }
}

function PlanCard({ plan, lang }) {
  const reason = REASON_LABELS[plan.reason] || REASON_LABELS.better_fit
  const isSq = lang === 'sq'
  return (
    <div className={`plan-card ${plan.recommended ? 'plan-card-recommended' : ''}`}>
      {plan.recommended && <div className="plan-card-badge">{isSq ? '⭐ Më i miri' : '⭐ Best Pick'}</div>}
      <div className="plan-card-header">
        <h4 className="plan-card-name">{plan.name}</h4>
        {plan.savings > 0 && (
          <span className="plan-card-savings">
            {isSq ? 'Kurseni ' : 'Save '}{plan.savings} Lek/{isSq ? 'muaj' : 'mo'}
          </span>
        )}
      </div>
      <div className="plan-card-details">
        {plan.price && (
          <div className="plan-card-price">
            <span className="plan-card-price-value">{plan.price}</span>
            <span className="plan-card-price-label">Lek/{isSq ? 'muaj' : 'mo'}</span>
          </div>
        )}
        {plan.fit && (
          <div className="plan-card-fit">
            <div className="plan-card-fit-bar">
              <div className="plan-card-fit-fill" style={{ width: `${plan.fit}%` }} />
            </div>
            <span className="plan-card-fit-text">{plan.fit}% {isSq ? 'përputhje' : 'match'}</span>
          </div>
        )}
      </div>
      <div className="plan-card-reason">
        <span className="plan-card-reason-icon">{reason.icon}</span>
        <span>{isSq ? reason.sq : reason.en}</span>
      </div>
      <button className="plan-card-btn">
        {isSq ? 'Kalo te ky plan' : 'Switch to this plan'}
      </button>
    </div>
  )
}
function PlanRecommendation({ text, lang }) {
  const plans = parsePlans(text)
  if (plans.length === 0) return null
  const isSq = lang === 'sq'
  return (
    <div className="plan-recommendation">
      <p className="plan-recommendation-intro">
        {isSq ? 'Bazuar në përdorimin tuaj, këto janë planet më të mira:' : 'Based on your usage, here are the best plans:'}
      </p>
      <div className="plan-cards">
        {plans.map((plan, i) => (
          <PlanCard key={i} plan={plan} lang={lang} />
        ))}
      </div>
    </div>
  )
}

export { PlanRecommendation, parsePlans }
export default PlanRecommendation
