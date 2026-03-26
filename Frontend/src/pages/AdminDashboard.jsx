import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { eventsAPI, venuesAPI, fraudAPI } from '../services/api'
import { useAuth } from '../context/AuthContext'

const TABS = ['Events', 'Venues', 'Fraud Monitoring']

export default function AdminDashboard() {
  const { isAdmin } = useAuth()
  const navigate = useNavigate()
  const [tab, setTab] = useState(0)

  if (!isAdmin) {
    return (
      <div style={{ textAlign: 'center', padding: '80px 24px' }}>
        <p style={{ color: 'var(--red)', fontSize: 18 }}>Access denied — Admin only</p>
      </div>
    )
  }

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto', padding: '40px 24px' }} className="fade-in">
      <h1 style={{ fontSize: 42, marginBottom: 6 }}>Admin Dashboard</h1>
      <p style={{ color: 'var(--text-muted)', marginBottom: 36 }}>Manage your platform</p>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: 4, borderBottom: '1px solid var(--border)', marginBottom: 32 }}>
        {TABS.map((t, i) => (
          <button key={i} onClick={() => setTab(i)} style={{
            padding: '10px 20px', fontSize: 13, fontWeight: 500,
            color: tab === i ? 'var(--gold)' : 'var(--text-muted)',
            borderBottom: tab === i ? '2px solid var(--gold)' : '2px solid transparent',
            marginBottom: -1, background: 'none', cursor: 'pointer',
            transition: 'all 0.2s',
          }}>
            {t}
          </button>
        ))}
      </div>

      {tab === 0 && <AdminEvents />}
      {tab === 1 && <AdminVenues />}
      {tab === 2 && <AdminFraud />}
    </div>
  )
}

// ─── Events Tab ───────────────────────────────────────────────────────────────
function AdminEvents() {
  const [events, setEvents] = useState([])
  const [loading, setLoading] = useState(true)

  const load = async () => {
    try {
      const { data } = await eventsAPI.getAll()
      setEvents(data)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const handleDelete = async (id) => {
    if (!confirm('Delete this event?')) return
    await eventsAPI.delete(id)
    setEvents(e => e.filter(ev => ev.id !== id))
  }

  const handlePublish = async (id) => {
    await eventsAPI.publish(id)
    load()
  }

  const handleCancel = async (id) => {
    await eventsAPI.cancel(id)
    load()
  }

  if (loading) return <div className="loading"><div className="spinner" /></div>

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <span style={{ color: 'var(--text-muted)', fontSize: 14 }}>{events.length} total events</span>
      </div>
      <Table
        headers={['Title', 'Status', 'Capacity', 'Start Date', 'Actions']}
        rows={events.map(ev => [
          ev.title,
          <span className={`badge badge-${ev.status?.toLowerCase()}`}>{ev.status}</span>,
          `${ev.currentAttendees} / ${ev.maxCapacity}`,
          ev.startDate ? new Date(ev.startDate).toLocaleDateString('en-GB') : '—',
          <div style={{ display: 'flex', gap: 8 }}>
            {ev.status === 'DRAFT' && (
              <button className="btn btn-gold" style={{ padding: '4px 12px', fontSize: 11 }} onClick={() => handlePublish(ev.id)}>Publish</button>
            )}
            {ev.status !== 'CANCELLED' && ev.status !== 'COMPLETED' && (
              <button className="btn btn-danger" style={{ padding: '4px 12px', fontSize: 11 }} onClick={() => handleCancel(ev.id)}>Cancel</button>
            )}
            <button className="btn btn-danger" style={{ padding: '4px 12px', fontSize: 11 }} onClick={() => handleDelete(ev.id)}>Delete</button>
          </div>
        ])}
      />
    </div>
  )
}

// ─── Venues Tab ───────────────────────────────────────────────────────────────
function AdminVenues() {
  const [venues, setVenues] = useState([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ name: '', address: '', city: '', maxCapacity: '' })
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')

  const load = async () => {
    try {
      const { data } = await venuesAPI.getAll()
      setVenues(data)
    } finally {
      setLoading(false)
    }
  }
  useEffect(() => { load() }, [])

  const handleSave = async (e) => {
    e.preventDefault()
    setSaving(true)
    setFormError('')
    try {
      await venuesAPI.create({ name: form.name, address: form.address, city: form.city, maxCapacity: parseInt(form.maxCapacity) })
      setForm({ name: '', address: '', city: '', maxCapacity: '' })
      setShowForm(false)
      load()
    } catch (err) {
      setFormError(err.response?.data?.message || JSON.stringify(err.response?.data) || 'Failed to save venue.')
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (id) => {
    if (!confirm('Delete this venue?')) return
    await venuesAPI.delete(id)
    setVenues(v => v.filter(ve => ve.id !== id))
  }

  if (loading) return <div className="loading"><div className="spinner" /></div>

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 20 }}>
        <span style={{ color: 'var(--text-muted)', fontSize: 14 }}>{venues.length} venues</span>
        <button className="btn btn-gold" style={{ padding: '6px 16px' }} onClick={() => { setShowForm(!showForm); setFormError('') }}>
          {showForm ? 'Cancel' : '+ Add Venue'}
        </button>
      </div>

      {showForm && (
        <div className="card" style={{ marginBottom: 20, background: 'var(--bg-elevated)' }}>
          <form onSubmit={handleSave} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            {formError && <div className="error-msg">{formError}</div>}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr 1fr', gap: 16 }}>
              <div className="input-group">
                <label>Name</label>
                <input required placeholder="Venue name" value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} />
              </div>
              <div className="input-group">
                <label>Address</label>
                <input required placeholder="12 Rue de la Paix" value={form.address} onChange={e => setForm(f => ({ ...f, address: e.target.value }))} />
              </div>
              <div className="input-group">
                <label>City</label>
                <input required placeholder="Paris" value={form.city} onChange={e => setForm(f => ({ ...f, city: e.target.value }))} />
              </div>
              <div className="input-group">
                <label>Max Capacity</label>
                <input required type="number" placeholder="1000" value={form.maxCapacity} onChange={e => setForm(f => ({ ...f, maxCapacity: e.target.value }))} />
              </div>
            </div>
            <button type="submit" className="btn btn-gold" style={{ alignSelf: 'flex-end', padding: '8px 24px' }} disabled={saving}>
              {saving ? 'Saving...' : 'Save Venue'}
            </button>
          </form>
        </div>
      )}

      <Table
        headers={['Name', 'Address', 'City', 'Capacity', 'Actions']}
        rows={venues.map(v => [
          v.name, v.address, v.city,
          v.maxCapacity?.toLocaleString(),
          <button className="btn btn-danger" style={{ padding: '4px 12px', fontSize: 11 }} onClick={() => handleDelete(v.id)}>Delete</button>
        ])}
      />
    </div>
  )
}

// ─── Fraud Tab ────────────────────────────────────────────────────────────────
function AdminFraud() {
  const [frauds, setFrauds] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fraudAPI.getAll()
      .then(({ data }) => setFrauds(data))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="loading"><div className="spinner" /></div>

  return (
    <div>
      <div style={{ marginBottom: 20, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span style={{ color: 'var(--text-muted)', fontSize: 14 }}>{frauds.length} alerts</span>
        {frauds.filter(f => f.scoreAnomalie > 0.8).length > 0 && (
          <span className="badge" style={{ background: 'rgba(224,82,82,0.15)', color: 'var(--red)' }}>
            {frauds.filter(f => f.scoreAnomalie > 0.8).length} high risk
          </span>
        )}
      </div>
      <Table
        headers={['Order ID', 'Fraud Type', 'Score', 'Detected At']}
        rows={frauds.map(f => [
          <span style={{ fontFamily: 'monospace', fontSize: 12 }}>{f.orderId?.slice(0, 16)}...</span>,
          <span style={{ color: 'var(--red)', fontSize: 12 }}>{f.typeFraude}</span>,
          <ScoreBar score={f.scoreAnomalie} />,
          f.detectedAt ? new Date(f.detectedAt).toLocaleDateString('en-GB') : '—',
        ])}
      />
    </div>
  )
}

function ScoreBar({ score }) {
  const pct = Math.round((score || 0) * 100)
  const color = pct > 80 ? 'var(--red)' : pct > 50 ? 'var(--gold)' : 'var(--green)'
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
      <div style={{ width: 80, height: 6, background: 'var(--border)', borderRadius: 3 }}>
        <div style={{ width: `${pct}%`, height: '100%', background: color, borderRadius: 3, transition: 'width 0.5s' }} />
      </div>
      <span style={{ fontSize: 12, color, fontWeight: 500 }}>{pct}%</span>
    </div>
  )
}

// ─── Shared Table ─────────────────────────────────────────────────────────────
function Table({ headers, rows }) {
  if (rows.length === 0) {
    return <div style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>No data found.</div>
  }
  return (
    <div style={{ overflowX: 'auto' }}>
      <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
        <thead>
          <tr>
            {headers.map((h, i) => (
              <th key={i} style={{
                textAlign: 'left', padding: '10px 16px',
                borderBottom: '1px solid var(--border)',
                color: 'var(--text-muted)', fontSize: 11,
                fontWeight: 500, letterSpacing: '0.08em', textTransform: 'uppercase',
              }}>{h}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row, i) => (
            <tr key={i} style={{ borderBottom: '1px solid var(--border)' }}
              onMouseEnter={e => e.currentTarget.style.background = 'var(--bg-elevated)'}
              onMouseLeave={e => e.currentTarget.style.background = 'transparent'}>
              {row.map((cell, j) => (
                <td key={j} style={{ padding: '12px 16px', verticalAlign: 'middle' }}>{cell}</td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}