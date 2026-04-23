import { useEffect, useState } from 'react'

export default function Toast({ message, type = 'error', duration = 5000, onClose }) {
  const [isVisible, setIsVisible] = useState(!!message)

  useEffect(() => {
    if (!message) {
      setIsVisible(false)
      return
    }
    setIsVisible(true)
    const timer = setTimeout(() => {
      setIsVisible(false)
      onClose?.()
    }, duration)
    return () => clearTimeout(timer)
  }, [message, duration, onClose])

  if (!isVisible || !message) return null

  const colors = {
    error: { bg: 'rgba(224,82,82,0.15)', border: 'rgba(224,82,82,0.3)', text: 'var(--red)' },
    success: { bg: 'rgba(82,183,136,0.15)', border: 'rgba(82,183,136,0.3)', text: 'var(--green)' },
    info: { bg: 'rgba(201,168,76,0.15)', border: 'rgba(201,168,76,0.3)', text: 'var(--gold)' },
  }
  const color = colors[type] || colors.info

  return (
    <div style={{
      position: 'fixed',
      top: 24,
      right: 24,
      zIndex: 9999,
      background: color.bg,
      border: `1px solid ${color.border}`,
      borderRadius: 'var(--radius)',
      padding: '12px 16px',
      color: color.text,
      fontSize: 13,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      gap: 12,
      animation: 'slideIn 0.3s ease forwards',
      maxWidth: 400,
    }}>
      <span>{message}</span>
      <button
        onClick={() => {
          setIsVisible(false)
          onClose?.()
        }}
        style={{
          background: 'none',
          border: 'none',
          cursor: 'pointer',
          color: color.text,
          fontSize: 16,
          opacity: 0.6,
          padding: 0,
          transition: 'opacity 0.2s',
        }}
        onMouseEnter={e => e.target.style.opacity = 1}
        onMouseLeave={e => e.target.style.opacity = 0.6}
      >
        ×
      </button>
    </div>
  )
}
