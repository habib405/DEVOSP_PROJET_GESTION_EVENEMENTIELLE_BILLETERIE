import { Link } from 'react-router-dom'

export default function PaymentCancel() {
  return (
    <div style={{ maxWidth: 560, margin: '80px auto', padding: '0 24px' }} className="fade-in">
      <div className="card" style={{ textAlign: 'center', padding: 40 }}>
        <div style={{ fontSize: 48, marginBottom: 16 }}>↩️</div>
        <h2 style={{ fontSize: 24, marginBottom: 8 }}>Paiement annulé</h2>
        <p style={{ color: 'var(--text-muted)', marginBottom: 24, fontSize: 14 }}>
          Vous avez annulé le paiement Stripe. Aucun débit n'a eu lieu.<br/>
          Vous pouvez réessayer à tout moment.
        </p>
        <div style={{ display: 'flex', gap: 10, justifyContent: 'center' }}>
          <Link to="/events" className="btn btn-outline">Retour aux événements</Link>
        </div>
      </div>
    </div>
  )
}
