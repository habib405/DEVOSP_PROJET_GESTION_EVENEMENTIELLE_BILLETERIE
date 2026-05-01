import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { paymentsAPI } from '../services/api'

export default function PaymentSuccess() {
  const [params] = useSearchParams()
  const sessionId = params.get('session_id')

  const [state, setState] = useState({ loading: true, error: '', result: null })

  useEffect(() => {
    if (!sessionId) {
      setState({ loading: false, error: 'Missing Stripe session_id in URL.', result: null })
      return
    }
    let cancelled = false
    ;(async () => {
      try {
        const { data } = await paymentsAPI.finalize(sessionId)
        if (cancelled) return
        // Clean up the redirect context
        localStorage.removeItem('pendingOrderId')
        localStorage.removeItem('pendingTicketTypeIds')
        setState({ loading: false, error: '', result: data })
      } catch (e) {
        if (cancelled) return
        setState({
          loading: false,
          error: e.response?.data?.message || 'Could not finalize payment.',
          result: null,
        })
      }
    })()
    return () => { cancelled = true }
  }, [sessionId])

  return (
    <div style={{ maxWidth: 560, margin: '80px auto', padding: '0 24px' }} className="fade-in">
      <div className="card" style={{ textAlign: 'center', padding: 40 }}>
        {state.loading && (
          <>
            <div className="spinner" style={{ margin: '0 auto 20px' }} />
            <h2 style={{ fontSize: 22, marginBottom: 8 }}>Finalisation du paiement…</h2>
            <p style={{ color: 'var(--text-muted)', fontSize: 14 }}>
              Nous vérifions auprès de Stripe et préparons vos billets.
            </p>
          </>
        )}

        {!state.loading && state.error && (
          <>
            <div style={{ fontSize: 48, marginBottom: 16 }}>⚠️</div>
            <h2 style={{ fontSize: 24, marginBottom: 8 }}>Paiement non confirmé</h2>
            <p style={{ color: 'var(--text-muted)', marginBottom: 24, fontSize: 14 }}>{state.error}</p>
            <Link to="/events" className="btn btn-outline" style={{ justifyContent: 'center' }}>
              Retour aux événements
            </Link>
          </>
        )}

        {!state.loading && state.result && (
          <>
            <div style={{ fontSize: 48, marginBottom: 16 }}>🎉</div>
            <h2 style={{ fontSize: 26, marginBottom: 8 }}>Paiement confirmé !</h2>
            <p style={{ color: 'var(--text-muted)', marginBottom: 20, fontSize: 14 }}>
              Votre facture PDF et vos billets avec QR code ont été envoyés à votre adresse email.
            </p>
            <div style={{ background: 'var(--bg-elevated)', borderRadius: 6, padding: 16, marginBottom: 24, fontSize: 13, textAlign: 'left' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', padding: '4px 0' }}>
                <span style={{ color: 'var(--text-muted)' }}>Order ID</span>
                <span style={{ fontFamily: 'monospace', color: 'var(--gold)' }}>{state.result.orderId}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', padding: '4px 0' }}>
                <span style={{ color: 'var(--text-muted)' }}>Statut commande</span>
                <span>{state.result.orderStatus}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', padding: '4px 0' }}>
                <span style={{ color: 'var(--text-muted)' }}>Statut paiement</span>
                <span>{state.result.paymentStatus}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', padding: '4px 0' }}>
                <span style={{ color: 'var(--text-muted)' }}>Total</span>
                <span style={{ color: 'var(--gold)', fontFamily: 'var(--font-display)' }}>
                  €{Number(state.result.totalAmount || 0).toFixed(2)}
                </span>
              </div>
            </div>
            <Link to="/my-tickets" className="btn btn-gold" style={{ width: '100%', justifyContent: 'center' }}>
              Voir mes billets
            </Link>
          </>
        )}
      </div>
    </div>
  )
}
