import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { eventsAPI } from '../services/api'
import { useAuth } from '../context/AuthContext'

const STATUS_COLORS = {
  PUBLISHED: 'badge-published',
  DRAFT: 'badge-draft',
  CANCELLED: 'badge-cancelled',
  COMPLETED: 'badge-completed',
}

function formatDate(dateStr) {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleDateString('en-GB', {
    day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
  })
}

export default function Events() {
  const { isOrganizer } = useAuth()
  const [events, setEvents] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [search, setSearch] = useState('')

  useEffect(() => {
    let cancelled = false

    const wait = (ms) => new Promise(resolve => setTimeout(resolve, ms))

    const loadEventsWithRetry = async () => {
      // Backend may need extra warmup time on first Docker boot.
      const maxAttempts = 25

      for (let attempt = 1; attempt <= maxAttempts; attempt += 1) {
        try {
          const { data } = isOrganizer ? await eventsAPI.getAll() : await eventsAPI.getPublished()
          if (!cancelled) {
            setEvents(Array.isArray(data) ? data : [])
            setError('')
            setLoading(false)
          }
          return
        } catch {
          if (attempt === maxAttempts) {
            if (!cancelled) {
              setError('Failed to load events.')
              setLoading(false)
            }
            return
          }
          await wait(2000)
        }
      }
    }

    const fetchEvents = async () => {
      await loadEventsWithRetry()
    }

    fetchEvents()

    return () => {
      cancelled = true
    }
  }, [isOrganizer])

  const filtered = events.filter(e =>
    e.title?.toLowerCase().includes(search.toLowerCase()) ||
    e.description?.toLowerCase().includes(search.toLowerCase())
  )

  if (loading) return <div className="loading"><div className="spinner" /> Loading events...</div>

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto', padding: '40px 24px' }} className="fade-in">
      {/* Hero */}
      <div style={{ marginBottom: 48, textAlign: 'center' }}>
        <h1 style={{ fontSize: 52, lineHeight: 1.1, marginBottom: 12 }}>
          Discover Events
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: 16 }}>
          {isOrganizer ? 'All events across all statuses' : 'Browse upcoming published events'}
        </p>
      </div>

      {/* Search + Actions */}
      <div style={{ display: 'flex', gap: 12, marginBottom: 32, alignItems: 'center' }}>
        <input
          placeholder="Search events..."
          value={search}
          onChange={e => setSearch(e.target.value)}
          style={{
            flex: 1, background: 'var(--bg-elevated)',
            border: '1px solid var(--border)', borderRadius: 4,
            color: 'var(--text)', padding: '10px 16px', outline: 'none',
            fontSize: 14,
          }}
        />
        {isOrganizer && (
          <Link to="/events/create" className="btn btn-gold">+ New Event</Link>
        )}
      </div>

      {error && <div className="error-msg" style={{ marginBottom: 24 }}>{error}</div>}

      {filtered.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '80px 0', color: 'var(--text-muted)' }}>
          <p style={{ fontSize: 18, marginBottom: 8 }}>No events found</p>
          <p style={{ fontSize: 14 }}>Try a different search or check back later.</p>
        </div>
      ) : (
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fill, minmax(340px, 1fr))',
          gap: 20,
        }}>
          {filtered.map(event => (
            <EventCard key={event.id} event={event} />
          ))}
        </div>
      )}
    </div>
  )
}

function EventCard({ event }) {
  const spotsLeft = event.maxCapacity - event.currentAttendees
  const soldPercent = Math.round((event.currentAttendees / event.maxCapacity) * 100) || 0

  return (
    <Link to={`/events/${event.id}`} style={{ display: 'block' }}>
      <div className="card" style={{
        height: '100%', cursor: 'pointer',
        transition: 'border-color 0.2s, transform 0.2s',
        display: 'flex', flexDirection: 'column', gap: 16,
      }}
        onMouseEnter={e => { e.currentTarget.style.borderColor = 'var(--gold)'; e.currentTarget.style.transform = 'translateY(-2px)' }}
        onMouseLeave={e => { e.currentTarget.style.borderColor = 'var(--border)'; e.currentTarget.style.transform = 'translateY(0)' }}
      >
        {/* Top row */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <span className={`badge ${STATUS_COLORS[event.status] || ''}`}>{event.status}</span>
          {spotsLeft <= 20 && event.status === 'PUBLISHED' && (
            <span className="badge" style={{ background: 'rgba(224,82,82,0.12)', color: 'var(--red)' }}>
              {spotsLeft === 0 ? 'SOLD OUT' : `${spotsLeft} left`}
            </span>
          )}
        </div>

        {/* Title */}
        <div>
          <h3 style={{ fontSize: 22, lineHeight: 1.2, marginBottom: 8 }}>{event.title}</h3>
          <p style={{ color: 'var(--text-muted)', fontSize: 13, lineHeight: 1.5, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
            {event.description}
          </p>
        </div>

        {/* Meta */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
          <Meta icon="📅" text={formatDate(event.startDate)} />
          {event.venue && <Meta icon="📍" text={event.venue.name} />}
        </div>

        {/* Capacity bar */}
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 12, color: 'var(--text-muted)', marginBottom: 6 }}>
            <span>{event.currentAttendees} / {event.maxCapacity} attendees</span>
            <span>{soldPercent}% full</span>
          </div>
          <div style={{ height: 3, background: 'var(--border)', borderRadius: 2 }}>
            <div style={{
              height: '100%', borderRadius: 2,
              width: `${soldPercent}%`,
              background: soldPercent > 80 ? 'var(--red)' : 'var(--gold)',
              transition: 'width 0.5s',
            }} />
          </div>
        </div>
      </div>
    </Link>
  )
}

function Meta({ icon, text }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 13, color: 'var(--text-muted)' }}>
      <span>{icon}</span>
      <span>{text}</span>
    </div>
  )
}