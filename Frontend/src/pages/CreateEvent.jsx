import { useState, useEffect } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { eventsAPI, venuesAPI } from '../services/api'

export default function CreateEvent() {
  const navigate = useNavigate()
  const [venues, setVenues] = useState([])
  const [form, setForm] = useState({
    title: '',
    description: '',
    startDate: '',
    endDate: '',
    maxCapacity: '',
    venueId: '',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    venuesAPI.getAll().then(({ data }) => setVenues(data)).catch(() => {})
  }, [])

  const handleChange = (e) => setForm(f => ({ ...f, [e.target.name]: e.target.value }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const payload = {
        title: form.title,
        description: form.description,
        startDate: form.startDate ? form.startDate + ':00' : null,
        endDate: form.endDate ? form.endDate + ':00' : null,
        maxCapacity: parseInt(form.maxCapacity),
        venueId: form.venueId !== '' ? form.venueId : null,
      }
      const { data } = await eventsAPI.create(payload)
      navigate(`/events/${data.id}`)
    } catch (err) {
      const msg = err.response?.data?.message || JSON.stringify(err.response?.data) || 'Failed to create event.'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ maxWidth: 700, margin: '0 auto', padding: '40px 24px' }} className="fade-in">
      <Link to="/events" style={{ color: 'var(--text-muted)', fontSize: 13, display: 'inline-flex', alignItems: 'center', gap: 6, marginBottom: 32 }}>
        ← Back to Events
      </Link>

      <h1 style={{ fontSize: 40, marginBottom: 8 }}>Create New Event</h1>
      <p style={{ color: 'var(--text-muted)', marginBottom: 36 }}>Event will be saved as DRAFT — publish it when ready.</p>

      <div className="card">
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
          {error && <div className="error-msg">{error}</div>}

          <div className="input-group">
            <label>Event Title *</label>
            <input name="title" required placeholder="e.g. Tech Conference 2026"
              value={form.title} onChange={handleChange} />
          </div>

          <div className="input-group">
            <label>Description</label>
            <textarea name="description" rows={4}
              placeholder="Describe your event..."
              value={form.description} onChange={handleChange}
              style={{ resize: 'vertical' }} />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
            <div className="input-group">
              <label>Start Date & Time *</label>
              <input type="datetime-local" name="startDate" required
                value={form.startDate} onChange={handleChange} />
            </div>
            <div className="input-group">
              <label>End Date & Time *</label>
              <input type="datetime-local" name="endDate" required
                value={form.endDate} onChange={handleChange} />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
            <div className="input-group">
              <label>Max Capacity *</label>
              <input type="number" name="maxCapacity" required min="1"
                placeholder="e.g. 500"
                value={form.maxCapacity} onChange={handleChange} />
            </div>
            <div className="input-group">
              <label>Venue (optional)</label>
              <select name="venueId" value={form.venueId} onChange={handleChange}>
                <option value="">— No venue selected —</option>
                {venues.map(v => (
                  <option key={v.id} value={v.id}>{v.name} ({v.capacity} cap.)</option>
                ))}
              </select>
            </div>
          </div>

          {venues.length === 0 && (
            <div style={{ fontSize: 13, color: 'var(--gold)', padding: '10px 14px', background: 'var(--gold-dim)', borderRadius: 4 }}>
              💡 No venues yet — go to Admin → Venues to create one first, or leave it empty for now.
            </div>
          )}

          <div className="divider" />

          <div style={{ display: 'flex', gap: 12 }}>
            <Link to="/events" className="btn btn-outline" style={{ flex: 1, justifyContent: 'center' }}>
              Cancel
            </Link>
            <button type="submit" className="btn btn-gold"
              style={{ flex: 2, justifyContent: 'center', padding: '12px 0' }}
              disabled={loading}>
              {loading
                ? 'Creating...'
                : '✦ Create Event (Draft)'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}