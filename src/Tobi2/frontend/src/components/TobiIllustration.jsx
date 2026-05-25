import React from 'react'
import './TobiIllustration.css'

function TobiIllustration({ size = 80, state = 'idle' }) {
  return (
    <div className={`tobi-character ${state}`} style={{ width: size, height: size }}>
      <svg width="100%" height="100%" viewBox="0 0 120 130" fill="none" xmlns="http://www.w3.org/2000/svg">
        <defs>
          <radialGradient id="tobiGlow" cx="50%" cy="50%" r="50%">
            <stop offset="0%" stopColor="#E60000" stopOpacity="0.15" />
            <stop offset="100%" stopColor="#E60000" stopOpacity="0" />
          </radialGradient>
          <radialGradient id="tobiGlowOuter" cx="50%" cy="50%" r="50%">
            <stop offset="0%" stopColor="#E60000" stopOpacity="0.08" />
            <stop offset="100%" stopColor="#E60000" stopOpacity="0" />
          </radialGradient>
        </defs>

        {/* Outer glow */}
        <circle cx="60" cy="55" r="55" fill="url(#tobiGlowOuter)" className="tobi-glow-outer" />

        {/* Particle ring */}
        <g className="tobi-particle">
          <g className="tobi-particle-ring">
            <circle cx="60" cy="5" r="1.5" fill="#E60000" opacity="0.6" className="tobi-particle-dot" />
            <circle cx="107" cy="40" r="1.5" fill="#E60000" opacity="0.6" className="tobi-particle-dot" />
            <circle cx="107" cy="70" r="1.5" fill="#E60000" opacity="0.6" className="tobi-particle-dot" />
            <circle cx="60" cy="108" r="1.5" fill="#E60000" opacity="0.6" className="tobi-particle-dot" />
            <circle cx="13" cy="40" r="1.5" fill="#E60000" opacity="0.6" className="tobi-particle-dot" />
            <circle cx="13" cy="70" r="1.5" fill="#E60000" opacity="0.6" className="tobi-particle-dot" />
          </g>
        </g>

        {/* Main glow */}
        <circle cx="60" cy="55" r="45" fill="url(#tobiGlow)" className="tobi-glow" />

        {/* Shadow */}
        <ellipse cx="60" cy="118" rx="30" ry="5" fill="rgba(0,0,0,0.06)" className="tobi-shadow" />

        {/* Antenna */}
        <g className="tobi-antenna">
          <line x1="60" y1="12" x2="60" y2="26" stroke="#E60000" strokeWidth="2.5" strokeLinecap="round" />
          <circle cx="60" cy="12" r="5" fill="#E60000" className="tobi-antenna-ball" />
        </g>

        {/* Ears */}
        <ellipse cx="26" cy="48" rx="6" ry="8" fill="#E60000" opacity="0.2" className="tobi-ear-left" />
        <ellipse cx="94" cy="48" rx="6" ry="8" fill="#E60000" opacity="0.2" className="tobi-ear-right" />

        {/* Head */}
        <circle cx="60" cy="52" r="30" fill="#FFF5F5" stroke="#E60000" strokeWidth="2.5" />

        {/* Eyes */}
        <g className="tobi-eye-group">
          <circle cx="47" cy="47" r="7" fill="#E60000" className="tobi-eye" />
          <circle cx="45" cy="44.5" r="2.5" fill="white" className="tobi-eye-shine" />
        </g>
        <g className="tobi-eye-group">
          <circle cx="73" cy="47" r="7" fill="#E60000" className="tobi-eye" />
          <circle cx="71" cy="44.5" r="2.5" fill="white" className="tobi-eye-shine" />
        </g>

        {/* Blush */}
        <ellipse cx="37" cy="56" rx="6" ry="3" fill="#E60000" opacity="0.08" className="tobi-blush" />
        <ellipse cx="83" cy="56" rx="6" ry="3" fill="#E60000" opacity="0.08" className="tobi-blush" />

        {/* Mouth */}
        <path d="M54 58 Q60 64 66 58" stroke="#E60000" strokeWidth="2.5" fill="none" strokeLinecap="round" className="tobi-mouth" />

        {/* Body */}
        <rect x="42" y="76" width="36" height="26" rx="12" fill="#E60000" />

        {/* Chest LEDs */}
        <rect x="54" y="83" width="12" height="2.5" rx="1.25" fill="white" opacity="0.7" className="tobi-chest-led" />
        <rect x="56" y="89" width="8" height="2.5" rx="1.25" fill="white" opacity="0.4" className="tobi-chest-led" />

        {/* Arms */}
        <rect x="28" y="81" width="13" height="7" rx="3.5" fill="#E60000" opacity="0.85" className="tobi-arm tobi-arm-left" />
        <rect x="79" y="81" width="13" height="7" rx="3.5" fill="#E60000" opacity="0.85" className="tobi-arm tobi-arm-right" />

        {/* Feet */}
        <ellipse cx="50" cy="105" rx="9" ry="4" fill="#E60000" opacity="0.15" />
        <ellipse cx="70" cy="105" rx="9" ry="4" fill="#E60000" opacity="0.15" />
      </svg>
    </div>
  )
}

export default TobiIllustration
