import React, { useEffect } from 'react'

function Toast({ message, type = 'error', onDismiss }) {
  useEffect(() => {
    const timer = setTimeout(onDismiss, 3500)
    return () => clearTimeout(timer)
  }, [onDismiss])

  return (
    <div className={`toast ${type}`} onClick={onDismiss}>
      {message}
    </div>
  )
}

export default Toast
