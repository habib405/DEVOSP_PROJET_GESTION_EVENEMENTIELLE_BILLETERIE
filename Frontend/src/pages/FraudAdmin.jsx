import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { fraudAPI } from '../services/api'
import { useAuth } from '../context/AuthContext'

export default function FraudAdmin() {
  const { isAdmin } = useAuth()
  const navigate = useNavigate()
  const [frauds, setFrauds] = useState([])
  const [loading, setLoading] = useState(true)

  if (!isAdmin) return <div style={{ textAlign: 'center', padding: 80 }}>Access denied — Admin only</div>

  const [page, setPage] = useState(0)
  const [size] = useState(20)
  const [total, setTotal] = useState(0)

  const load = (p = 0) => {
    setLoading(true)
    fraudAPI.getPending(p, size)
      .then(({ data }) => {
        setFrauds(data.content || [])
        setTotal(data.totalElements || 0)
        setPage(p)
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => { load(0) }, [])

  const markFP = async (id) => {
    if (!confirm('Mark this alert as false-positive?')) return
    await fraudAPI.markFalsePositive(id)
    // reload current page
    load(page)
  }

  if (loading) return <div className="loading"><div className="spinner" /></div>

  return (
    <div style={{ maxWidth: 1000, margin: '0 auto', padding: 24 }}>
      <h2>Fraud Monitoring — Pending review</h2>
      <div style={{ margin: '12px 0 20px', color: 'var(--text-muted)' }}>{frauds.length} alerts</div>

      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr>
            <th style={{ textAlign: 'left', padding: 8 }}>Order</th>
            <th style={{ textAlign: 'left', padding: 8 }}>Type</th>
            <th style={{ textAlign: 'left', padding: 8 }}>Score</th>
            <th style={{ textAlign: 'left', padding: 8 }}>Detected</th>
            <th style={{ textAlign: 'left', padding: 8 }}>Actions</th>
          </tr>
        </thead>
        <tbody>
          {frauds.map(f => (
            <tr key={f.id} style={{ borderTop: '1px solid var(--border)' }}>
              <td style={{ padding: 10, fontFamily: 'monospace', fontSize: 13 }}>{f.orderId?.slice(0, 16)}...</td>
              <td style={{ padding: 10, color: 'var(--red)' }}>{f.typeFraude}</td>
              <td style={{ padding: 10 }}>{Math.round((f.scoreAnomalie || 0) * 100)}%</td>
              <td style={{ padding: 10 }}>{f.detectedAt ? new Date(f.detectedAt).toLocaleString() : '—'}</td>
              <td style={{ padding: 10 }}>
                <button className="btn" onClick={() => navigate(`/orders/${f.orderId}`)}>View order</button>
                <button className="btn btn-gold" style={{ marginLeft: 8 }} onClick={() => markFP(f.id)}>Mark false-positive</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 12, alignItems: 'center' }}>
        <div style={{ color: 'var(--text-muted)' }}>{total} total</div>
        <div>
          <button className="btn" disabled={page === 0} onClick={() => load(page - 1)}>Prev</button>
          <button className="btn" style={{ marginLeft: 8 }} disabled={(page + 1) * size >= total} onClick={() => load(page + 1)}>Next</button>
        </div>
      </div>
    </div>
  )
}
