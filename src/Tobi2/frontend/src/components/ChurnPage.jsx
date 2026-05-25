import React, { useEffect, useState } from 'react'

function RiskBadge({ risk }) {
  const colors = {
    HIGH: { bg: '#FFF0F0', text: '#CC0000', dot: '#E60000' },
    MEDIUM: { bg: '#FFF8E1', text: '#E67700', dot: '#FFB300' },
    LOW: { bg: '#E8F5E9', text: '#2E7D32', dot: '#4CAF50' }
  }
  const c = colors[risk] || colors.LOW
  return (
    <span className="risk-badge" style={{ background: c.bg, color: c.text }}>
      <span className="risk-dot" style={{ background: c.dot }} />
      {risk}
    </span>
  )
}

function reasonLabel(reason) {
  const labels = {
    data_drop: 'Rënie e trafikut',
    unresolved_complaints: 'Ankesa të pazgjidhura',
    competitor_inquiry: 'Pyetje për konkurentin',
    inactive_14d: 'Inaktiv 14 ditë',
    inactive_7d: 'Inaktiv 7 ditë',
    contract_ending_soon: 'Kontrata mbyllet',
    unpaid_bill: 'Faturë e papaguar'
  }
  return labels[reason] || reason
}

function UserDetail({ userId, onClose }) {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    fetch(`/api/tobi2/admin/churn/${userId}`)
      .then(res => res.json())
      .then(d => { setData(d); setLoading(false) })
      .catch(() => { setError('Nuk u ngarkuan detajet.'); setLoading(false) })
  }, [userId])

  if (loading) return <div className="user-detail-loading">Duke ngarkuar...</div>
  if (error) return <div className="user-detail-error">{error}</div>
  if (!data) return null

  const signal = data.signal

  return (
    <div className="user-detail">
      <div className="user-detail-header">
        <div>
          <strong>{data.userId}</strong>
          <RiskBadge risk={data.risk} />
        </div>
        <button className="user-detail-close" onClick={onClose}>&times;</button>
      </div>

      <div className="user-detail-grid">
        <div className="detail-section">
          <h4>Sinjalet e përdoruesit</h4>
          <div className="detail-rows">
            <div className="detail-row">
              <span className="detail-label">Data mesatare ditore</span>
              <span className="detail-value">{signal.avgDailyDataMB} MB</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Data mesatare ditore (më parë)</span>
              <span className="detail-value">{signal.prevAvgDailyDataMB} MB</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Ankesa të pazgjidhura</span>
              <span className="detail-value">{signal.unresolvedComplaints}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Pyeti për konkurentin</span>
              <span className="detail-value">{signal.askedAboutCompetitor ? 'Po' : 'Jo'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Ditë pa aktivitet</span>
              <span className="detail-value">{signal.daysSinceLastChat}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Ditë deri në mbarim të kontratës</span>
              <span className="detail-value">{signal.daysUntilContractEnd}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Ka faturë të papaguar</span>
              <span className="detail-value">{signal.hasUnpaidBill ? 'Po' : 'Jo'}</span>
            </div>
          </div>
        </div>

        <div className="detail-section">
          <h4>Rezultati i Churn</h4>
          <div className="churn-result-score">
            <div className="big-score" style={{ color: data.risk === 'HIGH' ? '#CC0000' : data.risk === 'MEDIUM' ? '#E67700' : '#2E7D32' }}>
              {data.score}<span className="big-score-unit">/100</span>
            </div>
            <RiskBadge risk={data.risk} />
          </div>
          <div className="score-bar-chart">
            <div className="score-fill" style={{ width: `${data.score}%`, background: data.risk === 'HIGH' ? '#E60000' : data.risk === 'MEDIUM' ? '#FFB300' : '#4CAF50' }} />
          </div>

          <h4 style={{ marginTop: 16 }}>Arsyet</h4>
          <div className="detail-reasons">
            {data.reasons.map((r, i) => (
              <div key={i} className="detail-reason-item">{reasonLabel(r)}</div>
            ))}
            {data.reasons.length === 0 && <span className="detail-no-data">Nuk ka arsye</span>}
          </div>
        </div>

        <div className="detail-section detail-section-full">
          <h4>Oferta e Rekomanduar</h4>
          <div className="offer-box">
            <div className="offer-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#E60000" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <polyline points="20 12 20 22 4 22 4 12" />
                <rect x="2" y="7" width="20" height="5" />
                <line x1="12" y1="22" x2="12" y2="7" />
                <path d="M12 7H7.5a2.5 2.5 0 0 1 0-5C11 2 12 7 12 7z" />
                <path d="M12 7h4.5a2.5 2.5 0 0 0 0-5C13 2 12 7 12 7z" />
              </svg>
            </div>
            <div className="offer-text">
              {data.personalOffer || 'Nuk ka ofertë'}
            </div>
          </div>

          {data.personalOffer && (
            <div className="impact-box">
              <h4>Impakti në Biznes</h4>
              <div className="impact-grid">
                <div className="impact-item">
                  <span className="impact-value positive">+92%</span>
                  <span className="impact-label">Mbajtje me ndërhyrje</span>
                </div>
                <div className="impact-item">
                  <span className="impact-value negative">45%</span>
                  <span className="impact-label">Pa ndërhyrje</span>
                </div>
                <div className="impact-item">
                  <span className="impact-value">{data.score}/100</span>
                  <span className="impact-label">Risk score</span>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

function ChurnPage() {
  const [summary, setSummary] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [expandedUser, setExpandedUser] = useState(null)

  useEffect(() => {
    fetch('/api/tobi2/admin/churn/summary')
      .then(res => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        return res.json()
      })
      .then(data => {
        setSummary(data)
        setLoading(false)
      })
      .catch(e => {
        setError('Gabim në ngarkimin e të dhënave të churn. Sigurohu që backend-i është ndezur.')
        setLoading(false)
      })
  }, [])

  if (loading) {
    return (
      <div className="churn-loading">
        <span className="spinner" />
        Duke ngarkuar...
      </div>
    )
  }

  if (error) {
    return (
      <div className="churn-page">
        <div className="churn-error-box">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#CC0000" strokeWidth="2">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
          <h3>Churn nuk disponohet</h3>
          <p>{error}</p>
        </div>
      </div>
    )
  }

  if (!summary) return null

  const { totalUsers, highRiskCount, mediumRiskCount, lowRiskCount, retentionRate, retentionWithIntervention, retentionWithoutIntervention, users } = summary

  return (
    <div className="churn-page">
      <div className="churn-header">
        <h2>Churn Dashboard</h2>
        <p>Monitorimi i rrezikut të humbjes së klientëve dhe ofertat e rekomanduara</p>
      </div>

      <div className="churn-cards">
        <div className="churn-card churn-card-total">
          <div className="churn-card-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" /><circle cx="9" cy="7" r="4" /><path d="M23 21v-2a4 4 0 0 0-3-3.87" /><path d="M16 3.13a4 4 0 0 1 0 7.75" /></svg>
          </div>
          <div className="churn-card-value">{totalUsers}</div>
          <div className="churn-card-label">Gjithsej Përdorues</div>
        </div>
        <div className="churn-card churn-card-high">
          <div className="churn-card-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12" /></svg>
          </div>
          <div className="churn-card-value">{highRiskCount}</div>
          <div className="churn-card-label">Risk i Lartë</div>
        </div>
        <div className="churn-card churn-card-medium">
          <div className="churn-card-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><line x1="12" y1="20" x2="12" y2="10" /><line x1="18" y1="20" x2="18" y2="4" /><line x1="6" y1="20" x2="6" y2="16" /></svg>
          </div>
          <div className="churn-card-value">{mediumRiskCount}</div>
          <div className="churn-card-label">Risk i Mesëm</div>
        </div>
        <div className="churn-card churn-card-low">
          <div className="churn-card-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12" /></svg>
          </div>
          <div className="churn-card-value">{lowRiskCount}</div>
          <div className="churn-card-label">Risk i Ulët</div>
        </div>
      </div>

      <div className="churn-insight-cards">
        <div className="insight-card">
          <h4>Norma e Mbajtjes</h4>
          <div className="insight-main">{retentionRate}%</div>
          <div className="insight-sub">Aktualisht</div>
        </div>
        <div className="insight-card insight-green">
          <h4>Me Ndërhyrje</h4>
          <div className="insight-main">{retentionWithIntervention}%</div>
          <div className="insight-sub">+{retentionWithIntervention - retentionRate}% përmirësim</div>
        </div>
        <div className="insight-card insight-red">
          <h4>Pa Ndërhyrje</h4>
          <div className="insight-main">{retentionWithoutIntervention}%</div>
          <div className="insight-sub">{retentionRate - retentionWithoutIntervention}% më keq</div>
        </div>
      </div>

      <div className="churn-table-wrapper">
        <table className="churn-table">
          <thead>
            <tr>
              <th>Përdoruesi</th>
              <th>Score</th>
              <th>Risk</th>
              <th>Arsyet</th>
              <th>Oferta</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {users.map((u, i) => (
              <React.Fragment key={i}>
                <tr
                  className={`churn-row ${expandedUser === i ? 'expanded' : ''}`}
                  onClick={() => setExpandedUser(expandedUser === i ? null : i)}
                >
                  <td className="churn-user-id">{u.userId}</td>
                  <td>
                    <div className="score-bar-wrapper">
                      <div className="score-bar">
                        <div className={`score-fill score-${u.risk.toLowerCase()}`} style={{ width: `${u.score}%` }} />
                      </div>
                      <span className="score-text">{u.score}/100</span>
                    </div>
                  </td>
                  <td><RiskBadge risk={u.risk} /></td>
                  <td>
                    <div className="churn-reasons">
                      {u.reasons.slice(0, 2).map((r, j) => (
                        <span key={j} className="churn-reason-tag">{reasonLabel(r)}</span>
                      ))}
                      {u.reasons.length > 2 && <span className="churn-reason-more">+{u.reasons.length - 2}</span>}
                      {u.reasons.length === 0 && <span className="churn-no-reasons">—</span>}
                    </div>
                  </td>
                  <td className="churn-offer">{u.personalOffer || '—'}</td>
                  <td className="churn-expand-col">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"
                      className={`expand-icon ${expandedUser === i ? 'open' : ''}`}
                    >
                      <polyline points="6 9 12 15 18 9" />
                    </svg>
                  </td>
                </tr>
                {expandedUser === i && (
                  <tr className="churn-detail-row">
                    <td colSpan={6}>
                      <UserDetail userId={u.userId} onClose={() => setExpandedUser(null)} />
                    </td>
                  </tr>
                )}
              </React.Fragment>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

export default ChurnPage
