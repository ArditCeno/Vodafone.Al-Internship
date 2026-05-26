import React, { useState } from 'react'
import TobiIllustration from './TobiIllustration'

function LoginPage({ onLogin, onRegister, loading, error }) {
  const [mode, setMode] = useState('login')
  const [username, setUsername] = useState('')
  const [fullName, setFullName] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [focusedField, setFocusedField] = useState(null)

  const handleSubmit = (e) => {
    e.preventDefault()
    if (!username.trim() || !password.trim()) return
    if (mode === 'login') {
      onLogin(username.trim(), password)
    } else {
      if (!fullName.trim()) return
      onRegister(username.trim(), fullName.trim(), password)
    }
  }

  const switchMode = () => {
    setMode(mode === 'login' ? 'register' : 'login')
    setError('')
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
            <h2>{mode === 'login' ? 'Hyr në llogari' : 'Krijo llogari'}</h2>
            <p>{mode === 'login' ? 'Vendos kredencialet e tua për të vazhduar' : 'Plotëso të dhënat për t\'u regjistruar'}</p>
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
                  placeholder={mode === 'login' ? "Emër Mbiemër ose email" : "Zgjidh një emër përdoruesi"}
                  autoComplete="username"
                />
              </div>
            </div>

            {mode === 'register' && (
              <div className="login-field">
                <label htmlFor="fullName">Emri i plotë</label>
                <div className="input-wrapper">
                  <input
                    id="fullName"
                    type="text"
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    onFocus={() => setFocusedField('fullName')}
                    onBlur={() => setFocusedField(null)}
                    placeholder="Emri dhe Mbiemri"
                    autoComplete="name"
                  />
                </div>
              </div>
            )}

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
                  autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
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
              disabled={loading || !username.trim() || !password.trim() || (mode === 'register' && !fullName.trim())}
            >
              {loading
                ? (mode === 'login' ? 'Duke u autentikuar...' : 'Duke u regjistruar...')
                : (mode === 'login' ? 'Hyr në llogari' : 'Regjistrohu')}
            </button>
          </form>

          <div className="login-footer">
            <p>
              {mode === 'login' ? (
                <>Nuk ke llogari? <button className="link-btn" onClick={switchMode}>Regjistrohu</button></>
              ) : (
                <>Ke tashmë llogari? <button className="link-btn" onClick={switchMode}>Hyr</button></>
              )}
            </p>
            {mode === 'login' && (
              <p className="login-hint">Punonjësit: përdorni email-in tuaj @vodafone</p>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export default LoginPage