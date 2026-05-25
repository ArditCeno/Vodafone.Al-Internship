import React from 'react'

function EvaluationBadge({ evaluation }) {
  if (!evaluation) return null

  const avgScore = (evaluation.brandTone + evaluation.accuracy) / 2
  const isGood = avgScore >= 7
  const isAverage = avgScore >= 5

  return (
    <div className={`evaluation-badge ${isGood ? 'good' : isAverage ? 'average' : ''}`}
         title={`Brand Tone: ${evaluation.brandTone}/10, Accuracy: ${evaluation.accuracy}/10\n${evaluation.feedback}`}>
      Eval {avgScore.toFixed(1)}
    </div>
  )
}

export default EvaluationBadge
