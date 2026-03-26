import { useState, useEffect } from 'react'
import { registrationsAPI } from '../services/api'

function formatDate(d) {
  if (!d) return '—'
  return new Date(d).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })
}

const STATUS_CLASS = {
  CONFIRMED: 'badge-confirmed',
  CANCELLED: 'badge-cancelled',
  PENDING: 'badge-pending',
}

export default function MyTickets() {
  const [registrations, setRegistrations] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [expandedQr, setExpandedQr] = useState(null)

  useEffect(() => {
    registrationsAPI.getMy()
      .then(({ data }) => setRegistrations(data))
      .catch(() => setError('Failed to load tickets.'))
      .finally(() => setLoading(false))
  }, [])

  const handleCancel = async (id) => {
    try {
      await registrationsAPI.cancel(id)
      setRegistrations(r => r.map(reg => reg.id === id ? { ...reg, status: 'CANCELLED' } : reg))
    } catch (e) {
      alert(e.response?.data?.message || 'Could not cancel ticket.')
    }
  }

  if (loading) return <div className="loading"><div className="spinner" /> Loading tickets...</div>

  return (
    <div style={{ maxWidth: 900, margin: '0 auto', padding: '40px 24px' }} className="fade-in">
      <h1 style={{ fontSize: 40, marginBottom: 8 }}>My Tickets</h1>
      <p style={{ color: 'var(--text-muted)', marginBottom: 36 }}>{registrations.length} ticket{registrations.length !== 1 ? 's' : ''} found</p>

      {error && <div className="error-msg" style={{ marginBottom: 24 }}>{error}</div>}

      {registrations.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '80px 0', color: 'var(--text-muted)' }}>
          <p style={{ fontSize: 18, marginBottom: 8 }}>No tickets yet</p>
          <p style={{ fontSize: 14 }}>Browse events and buy your first ticket.</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          {registrations.map(reg => (
            <div key={reg.id} className="card" style={{
              display: 'grid', gridTemplateColumns: '1fr auto',
              gap: 20, alignItems: 'center',
            }}>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 10 }}>
                  <span className={`badge ${STATUS_CLASS[reg.status] || ''}`}>{reg.status}</span>
                  {reg.ticketType && (
                    <span style={{ fontSize: 12, color: 'var(--gold)', background: 'var(--gold-dim)', padding: '2px 10px', borderRadius: 20 }}>
                      {reg.ticketType.name}
                    </span>
                  )}
                </div>
                <div style={{ fontSize: 11, color: 'var(--text-dim)', letterSpacing: '0.06em', textTransform: 'uppercase', fontFamily: 'monospace', marginBottom: 6 }}>
                  {reg.id}
                </div>
                <div style={{ display: 'flex', gap: 24, fontSize: 13, color: 'var(--text-muted)' }}>
                  <span>Registered: {formatDate(reg.registeredAt)}</span>
                  {reg.ticketType?.price && <span>€{reg.ticketType.price.toFixed(2)}</span>}
                </div>

                {reg.status === 'CONFIRMED' && reg.qrCode && (
                  <button className="btn btn-ghost" style={{ marginTop: 12, padding: '4px 0', fontSize: 12 }}
                    onClick={() => setExpandedQr(expandedQr === reg.id ? null : reg.id)}>
                    {expandedQr === reg.id ? '↑ Hide QR Code' : '↓ Show QR Code'}
                  </button>
                )}
              </div>

              {reg.status === 'CONFIRMED' && (
                <button className="btn btn-danger" style={{ padding: '6px 14px', fontSize: 12 }}
                  onClick={() => handleCancel(reg.id)}>
                  Cancel
                </button>
              )}

              {expandedQr === reg.id && reg.qrCode && (
                <div style={{ gridColumn: '1 / -1', display: 'flex', justifyContent: 'center', paddingTop: 16, borderTop: '1px solid var(--border)' }}>
                  <img
                    src={`data:image/png;base64,${reg.qrCode}`}
                    alt="QR Code"
                    style={{ width: 180, height: 180, borderRadius: 8, background: '#fff', padding: 8 }}
                  />
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}