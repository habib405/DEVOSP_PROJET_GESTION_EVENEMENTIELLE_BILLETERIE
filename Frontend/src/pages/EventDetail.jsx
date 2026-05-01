import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { eventsAPI, ticketTypesAPI, ordersAPI, paymentsAPI } from '../services/api'
import { useAuth } from '../context/AuthContext'

function formatDate(d) {
  if (!d) return '—'
  return new Date(d).toLocaleDateString('en-GB', {
    weekday: 'long', day: 'numeric', month: 'long', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}

const STEPS = ['Select Tickets', 'Review & Pay']

export default function EventDetail() {
  const { id } = useParams()
  const { user, isOrganizer, isAdmin } = useAuth()
  const canManage = isOrganizer || isAdmin

  const [event, setEvent] = useState(null)
  const [ticketTypes, setTicketTypes] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  // Ticket type form
  const [showTTForm, setShowTTForm] = useState(false)
  const [ttForm, setTTForm] = useState({ name: '', price: '', totalQuantity: '' })
  const [ttError, setTTError] = useState('')
  const [ttSaving, setTTSaving] = useState(false)

  // Purchase flow
  const [step, setStep] = useState(0)
  const [quantities, setQuantities] = useState({})
  const [order, setOrder] = useState(null)
  const [processing, setProcessing] = useState(false)
  const [purchaseError, setPurchaseError] = useState('')

  const loadData = async () => {
    try {
      const evRes = await eventsAPI.getById(id)
      setEvent(evRes.data)
      // Ticket types require auth — skip silently if not logged in
      try {
        const ttRes = await ticketTypesAPI.getByEvent(id)
        setTicketTypes(ttRes.data)
      } catch {
        setTicketTypes([])
      }
    } catch {
      setError('Event not found.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { loadData() }, [id])

  const handleAddTicketType = async (e) => {
    e.preventDefault()
    setTTSaving(true)
    setTTError('')
    try {
      await ticketTypesAPI.create({
        eventId: id,
        name: ttForm.name,
        price: parseFloat(ttForm.price),
        totalQuantity: parseInt(ttForm.totalQuantity),
      })
      setTTForm({ name: '', price: '', totalQuantity: '' })
      setShowTTForm(false)
      loadData()
    } catch (err) {
      setTTError(err.response?.data?.message || JSON.stringify(err.response?.data) || 'Failed to create ticket type.')
    } finally {
      setTTSaving(false)
    }
  }

  const handleDeleteTT = async (ttId) => {
    if (!confirm('Delete this ticket type?')) return
    await ticketTypesAPI.delete(ttId)
    setTicketTypes(t => t.filter(tt => tt.id !== ttId))
  }

  if (loading) return <div className="loading"><div className="spinner" /> Loading...</div>
  if (error || !event) return (
    <div style={{ textAlign: 'center', padding: '80px 24px' }}>
      <p style={{ color: 'var(--text-muted)' }}>{error}</p>
      <Link to="/events" className="btn btn-outline" style={{ marginTop: 16 }}>Back to Events</Link>
    </div>
  )

  const selectedItems = Object.entries(quantities)
    .filter(([, qty]) => qty > 0)
    .flatMap(([id, qty]) => Array(qty).fill(id))

  const totalAmount = ticketTypes.reduce((sum, tt) => {
    return sum + (tt.price || 0) * (quantities[tt.id] || 0)
  }, 0)

  const handleCreateOrder = async () => {
    if (selectedItems.length === 0) return
    setProcessing(true)
    setPurchaseError('')
    try {
      const { data } = await ordersAPI.create(selectedItems)
      setOrder(data)
      setStep(1)
    } catch (e) {
      setPurchaseError(e.response?.data?.message || 'Could not create order.')
    } finally {
      setProcessing(false)
    }
  }

  const handleCompleteOrder = async () => {
    setProcessing(true)
    setPurchaseError('')
    try {
      // Persist context so the success page can show details
      localStorage.setItem('pendingOrderId', order.id)
      localStorage.setItem('pendingTicketTypeIds', JSON.stringify(selectedItems))

      const { data } = await paymentsAPI.checkout(order.id, selectedItems)
      // Redirect to Stripe-hosted Checkout (sandbox)
      window.location.href = data.checkoutUrl
    } catch (e) {
      setPurchaseError(e.response?.data?.message || 'Could not start payment. Please try again.')
      setProcessing(false)
    }
  }

  return (
    <div style={{ maxWidth: 1100, margin: '0 auto', padding: '40px 24px' }} className="fade-in">
      <Link to="/events" style={{ color: 'var(--text-muted)', fontSize: 13, display: 'inline-flex', alignItems: 'center', gap: 6, marginBottom: 32 }}>
        ← Back to Events
      </Link>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 380px', gap: 32, alignItems: 'start' }}>
        {/* ── Left ── */}
        <div>
          <div style={{ marginBottom: 24 }}>
            <span className={`badge badge-${event.status?.toLowerCase()}`} style={{ marginBottom: 16, display: 'inline-block' }}>
              {event.status}
            </span>
            <h1 style={{ fontSize: 44, lineHeight: 1.1, marginBottom: 16 }}>{event.title}</h1>
            <p style={{ color: 'var(--text-muted)', fontSize: 15, lineHeight: 1.7 }}>{event.description}</p>
          </div>

          <div className="card" style={{ marginBottom: 24 }}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>
              <InfoItem label="Start" value={formatDate(event.startDate)} />
              <InfoItem label="End" value={formatDate(event.endDate)} />
              {event.venue && <>
                <InfoItem label="Venue" value={event.venue.name} />
                <InfoItem label="City" value={event.venue.city} />
              </>}
              <InfoItem label="Attendees" value={`${event.currentAttendees} / ${event.maxCapacity}`} />
            </div>
          </div>

          {/* Organizer actions */}
          {canManage && (
            <div style={{ display: 'flex', gap: 10, marginBottom: 32 }}>
              {event.status === 'DRAFT' && (
                <button className="btn btn-gold" onClick={async () => { await eventsAPI.publish(id); window.location.reload() }}>
                  Publish Event
                </button>
              )}
              {event.status !== 'CANCELLED' && event.status !== 'COMPLETED' && (
                <button className="btn btn-danger" onClick={async () => { await eventsAPI.cancel(id); window.location.reload() }}>
                  Cancel Event
                </button>
              )}
            </div>
          )}

          {/* ── Ticket Type Management (ADMIN/ORGANIZER only) ── */}
          {canManage && (
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
                <h3 style={{ fontSize: 22 }}>Ticket Types</h3>
                <button className="btn btn-gold" style={{ padding: '6px 16px' }}
                  onClick={() => { setShowTTForm(!showTTForm); setTTError('') }}>
                  {showTTForm ? 'Cancel' : '+ Add Ticket Type'}
                </button>
              </div>

              {showTTForm && (
                <div className="card" style={{ marginBottom: 16, background: 'var(--bg-elevated)' }}>
                  <form onSubmit={handleAddTicketType} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                    {ttError && <div className="error-msg">{ttError}</div>}
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 16 }}>
                      <div className="input-group">
                        <label>Name</label>
                        <input required placeholder="e.g. VIP" value={ttForm.name}
                          onChange={e => setTTForm(f => ({ ...f, name: e.target.value }))} />
                      </div>
                      <div className="input-group">
                        <label>Price (€)</label>
                        <input required type="number" min="0" step="0.01" placeholder="50.00"
                          value={ttForm.price} onChange={e => setTTForm(f => ({ ...f, price: e.target.value }))} />
                      </div>
                      <div className="input-group">
                        <label>Total Quantity</label>
                        <input required type="number" min="1" placeholder="100"
                          value={ttForm.totalQuantity} onChange={e => setTTForm(f => ({ ...f, totalQuantity: e.target.value }))} />
                      </div>
                    </div>
                    <button type="submit" className="btn btn-gold" style={{ alignSelf: 'flex-end', padding: '8px 24px' }} disabled={ttSaving}>
                      {ttSaving ? 'Saving...' : 'Save Ticket Type'}
                    </button>
                  </form>
                </div>
              )}

              {ticketTypes.length === 0 ? (
                <div style={{ padding: 20, textAlign: 'center', color: 'var(--text-muted)', border: '1px dashed var(--border)', borderRadius: 8, fontSize: 14 }}>
                  No ticket types yet — add one above to start selling tickets.
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                  {ticketTypes.map(tt => (
                    <div key={tt.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px 16px', background: 'var(--bg-elevated)', borderRadius: 6, border: '1px solid var(--border)' }}>
                      <div>
                        <span style={{ fontWeight: 500 }}>{tt.name}</span>
                        <span style={{ marginLeft: 12, color: 'var(--gold)', fontFamily: 'var(--font-display)', fontSize: 18 }}>€{tt.price?.toFixed(2)}</span>
                      </div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
                        <span style={{ fontSize: 13, color: 'var(--text-muted)' }}>{tt.soldQuantity} / {tt.totalQuantity} sold</span>
                        <button className="btn btn-danger" style={{ padding: '4px 12px', fontSize: 11 }} onClick={() => handleDeleteTT(tt.id)}>Delete</button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>

        {/* ── Right: Purchase flow (ATTENDEE only) ── */}
        {!canManage && (
        <div>
          {event.status !== 'PUBLISHED' ? (
            <div className="card" style={{ textAlign: 'center', padding: 32, color: 'var(--text-muted)' }}>
              <p style={{ fontSize: 16, marginBottom: 8 }}>Tickets not available</p>
              <p style={{ fontSize: 13 }}>This event is {event.status?.toLowerCase()}.</p>
            </div>
          ) : !user ? (
            <div className="card" style={{ textAlign: 'center', padding: 32 }}>
              <p style={{ marginBottom: 16, color: 'var(--text-muted)' }}>Sign in to purchase tickets</p>
              <Link to="/login" className="btn btn-gold" style={{ width: '100%', justifyContent: 'center' }}>Sign In</Link>
            </div>
          ) : step === 2 ? (
            <ConfirmationPanel order={order} />
          ) : (
            <div className="card">
              <div style={{ display: 'flex', gap: 0, marginBottom: 24 }}>
                {STEPS.map((s, i) => (
                  <div key={i} style={{ flex: 1, textAlign: 'center' }}>
                    <div style={{ height: 3, background: i <= step ? 'var(--gold)' : 'var(--border)', marginBottom: 6, borderRadius: 2, transition: 'background 0.3s' }} />
                    <span style={{ fontSize: 11, color: i <= step ? 'var(--gold)' : 'var(--text-dim)', letterSpacing: '0.06em', textTransform: 'uppercase' }}>{s}</span>
                  </div>
                ))}
              </div>

              {step === 0 && (
                <SelectStep ticketTypes={ticketTypes} quantities={quantities} setQuantities={setQuantities}
                  total={totalAmount} onNext={handleCreateOrder} processing={processing} error={purchaseError} />
              )}
              {step === 1 && (
                <ReviewStep ticketTypes={ticketTypes} quantities={quantities} total={totalAmount}
                  order={order} onConfirm={handleCompleteOrder} onBack={() => setStep(0)}
                  processing={processing} error={purchaseError} />
              )}
            </div>
          )}
        </div>
        )}
      </div>
    </div>
  )
}

function SelectStep({ ticketTypes, quantities, setQuantities, total, onNext, processing, error }) {
  const hasSelection = Object.values(quantities).some(q => q > 0)
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <h3 style={{ fontSize: 20, marginBottom: 4 }}>Select Tickets</h3>
      {ticketTypes.length === 0 && (
        <p style={{ color: 'var(--text-muted)', fontSize: 13 }}>No ticket types available yet.</p>
      )}
      {ticketTypes.map(tt => {
        const available = tt.totalQuantity - tt.soldQuantity
        const qty = quantities[tt.id] || 0
        return (
          <div key={tt.id} style={{
            border: `1px solid ${qty > 0 ? 'var(--gold)' : 'var(--border)'}`,
            borderRadius: 6, padding: 16,
            background: qty > 0 ? 'var(--gold-dim)' : 'transparent',
            transition: 'all 0.2s',
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 12 }}>
              <div>
                <div style={{ fontWeight: 500, fontSize: 15 }}>{tt.name}</div>
                <div style={{ color: 'var(--text-muted)', fontSize: 12, marginTop: 2 }}>{available} of {tt.totalQuantity} remaining</div>
              </div>
              <div style={{ fontFamily: 'var(--font-display)', fontSize: 22, color: 'var(--gold)' }}>€{tt.price?.toFixed(2)}</div>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <button onClick={() => setQuantities(q => ({ ...q, [tt.id]: Math.max(0, (q[tt.id] || 0) - 1) }))}
                style={{ width: 28, height: 28, border: '1px solid var(--border)', borderRadius: 4, color: 'var(--text)', cursor: 'pointer', background: 'var(--bg-elevated)' }}>−</button>
              <span style={{ minWidth: 20, textAlign: 'center', fontWeight: 500 }}>{qty}</span>
              <button onClick={() => setQuantities(q => ({ ...q, [tt.id]: Math.min(available, (q[tt.id] || 0) + 1) }))}
                disabled={qty >= available}
                style={{ width: 28, height: 28, border: '1px solid var(--border)', borderRadius: 4, color: qty >= available ? 'var(--text-dim)' : 'var(--text)', cursor: 'pointer', background: 'var(--bg-elevated)' }}>+</button>
              <span style={{ marginLeft: 'auto', fontSize: 13, color: 'var(--text-muted)' }}>
                {qty > 0 && `€${(tt.price * qty).toFixed(2)}`}
              </span>
            </div>
          </div>
        )
      })}
      {hasSelection && (
        <div style={{ borderTop: '1px solid var(--border)', paddingTop: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span style={{ color: 'var(--text-muted)' }}>Total</span>
          <span style={{ fontFamily: 'var(--font-display)', fontSize: 24, color: 'var(--gold)' }}>€{total.toFixed(2)}</span>
        </div>
      )}
      {error && <div className="error-msg">{error}</div>}
      <button className="btn btn-gold" style={{ width: '100%', justifyContent: 'center', padding: '12px 0' }}
        onClick={onNext} disabled={!hasSelection || processing}>
        {processing ? <><div className="spinner" style={{ width: 14, height: 14 }} /> Processing...</> : 'Continue →'}
      </button>
    </div>
  )
}

function ReviewStep({ ticketTypes, quantities, total, order, onConfirm, onBack, processing, error }) {
  const selectedTypes = ticketTypes.filter(tt => quantities[tt.id] > 0)
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <h3 style={{ fontSize: 20 }}>Review Order</h3>
      <div style={{ background: 'var(--bg-elevated)', borderRadius: 6, padding: 16 }}>
        <div style={{ fontSize: 11, color: 'var(--text-muted)', letterSpacing: '0.08em', textTransform: 'uppercase', marginBottom: 12 }}>
          Order #{order?.id?.slice(0, 8)}...
        </div>
        {selectedTypes.map(tt => (
          <div key={tt.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 0', borderBottom: '1px solid var(--border)' }}>
            <span>{tt.name} × {quantities[tt.id]}</span>
            <span style={{ color: 'var(--gold)' }}>€{(tt.price * quantities[tt.id]).toFixed(2)}</span>
          </div>
        ))}
        <div style={{ display: 'flex', justifyContent: 'space-between', paddingTop: 12, fontWeight: 500 }}>
          <span>Total</span>
          <span style={{ fontFamily: 'var(--font-display)', fontSize: 22, color: 'var(--gold)' }}>€{total.toFixed(2)}</span>
        </div>
      </div>
      <div style={{ padding: 12, background: 'rgba(201,168,76,0.06)', borderRadius: 6, border: '1px solid rgba(201,168,76,0.2)', fontSize: 13, color: 'var(--text-muted)' }}>
        💳 Paiement sécurisé via Stripe (sandbox).<br/>
        Carte test : <code>4242 4242 4242 4242</code> · date future · CVC libre.<br/>
        📧 Une facture PDF + vos billets QR seront envoyés par email.
      </div>
      {error && <div className="error-msg">{error}</div>}
      <div style={{ display: 'flex', gap: 10 }}>
        <button className="btn btn-outline" style={{ flex: 1, justifyContent: 'center' }} onClick={onBack} disabled={processing}>← Back</button>
        <button className="btn btn-gold" style={{ flex: 2, justifyContent: 'center' }} onClick={onConfirm} disabled={processing}>
          {processing ? <><div className="spinner" style={{ width: 14, height: 14 }} /> Redirecting to Stripe...</> : 'Pay with Stripe →'}
        </button>
      </div>
    </div>
  )
}

function ConfirmationPanel({ order }) {
  return (
    <div className="card" style={{ textAlign: 'center', padding: 32 }}>
      <div style={{ fontSize: 48, marginBottom: 16 }}>🎉</div>
      <h3 style={{ fontSize: 26, marginBottom: 8 }}>Booking Confirmed!</h3>
      <p style={{ color: 'var(--text-muted)', marginBottom: 20, fontSize: 14 }}>
        Your tickets have been sent to your email with QR codes.
      </p>
      <div style={{ background: 'var(--bg-elevated)', borderRadius: 6, padding: 12, marginBottom: 20, fontSize: 13, color: 'var(--text-muted)' }}>
        Order ID: <span style={{ color: 'var(--gold)', fontFamily: 'monospace' }}>{order?.id}</span>
      </div>
      <Link to="/my-tickets" className="btn btn-gold" style={{ width: '100%', justifyContent: 'center' }}>
        View My Tickets
      </Link>
    </div>
  )
}

function InfoItem({ label, value }) {
  return (
    <div>
      <div style={{ fontSize: 11, color: 'var(--text-muted)', letterSpacing: '0.08em', textTransform: 'uppercase', marginBottom: 4 }}>{label}</div>
      <div style={{ fontSize: 14 }}>{value}</div>
    </div>
  )
}