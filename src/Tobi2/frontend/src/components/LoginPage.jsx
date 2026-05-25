import React, { useState } from 'react'
import TobiIllustration from './TobiIllustration'

function LoginPage({ onLogin, loading, error }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [focusedField, setFocusedField] = useState(null)

  const handleSubmit = (e) => {
    e.preventDefault()
    if (!username.trim() || !password.trim()) return
    const loginType = username.includes('@vodafone') ? 'vodafone' : 'regular'
    onLogin(username.trim(), password, loginType)
  }

  return (
    <div className="login-page">
      <div className="login-bg-shapes">
        <div className="bg-circle bg-circle-1" />
        <div className="bg-circle bg-circle-2" />
        <div className="bg-circle bg-circle-3" />
        <div className="bg-circle bg-circle-4" />
      </div>

      <div className="login-brand">
        <TobiIllustration size={56} state={focusedField === 'password' ? 'listening' : 'idle'} />
        <div className="login-brand-text">
          <h1>TOBi2.0</h1>
          <span>Vodafone Albania</span>
        </div>
      </div>

      <div className="login-card">
        <div className="login-card-inner">
          <div className="login-header">
            <h2>Hyr në llogari</h2>
            <p>Vendos kredencialet e tua për të vazhduar</p>
          </div>

          {error && (
            <div className="login-error">
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="login-field">
              <label htmlFor="username">Përdoruesi</label>
              <div className="input-wrapper">
                <input
                  id="username"
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  onFocus={() => setFocusedField('username')}
                  onBlur={() => setFocusedField(null)}
                  placeholder="Emër Mbiemër, Numër ose email"
                  autoComplete="username"
                />
              </div>
            </div>

            <div className="login-field">
              <label htmlFor="password">Fjalëkalimi</label>
              <div className="input-wrapper">
                <input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  onFocus={() => setFocusedField('password')}
                  onBlur={() => setFocusedField(null)}
                  placeholder="Fjalëkalimi"
                  autoComplete="current-password"
                />
                <button
                  type="button"
                  className="password-toggle"
                  onClick={() => setShowPassword(!showPassword)}
                  tabIndex={-1}
                >
                  {showPassword ? '🙈' : '👁️'}
                </button>
              </div>
            </div>

            <button
              type="submit"
              className="login-btn"
              disabled={loading || !username.trim() || !password.trim()}
            >
              {loading ? 'Duke u autentikuar...' : 'Hyr në llogari'}
            </button>
          </form>

          <div className="login-footer">
            <p>Keni problem me hyrjen? Kontaktoni <strong>Administratorin</strong></p>
          </div>
        </div>
      </div>
    </div>
  )
}

export default LoginPage
