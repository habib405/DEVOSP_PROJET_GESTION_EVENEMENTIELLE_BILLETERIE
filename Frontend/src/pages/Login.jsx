import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Toast from '../components/Toast'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', password: '' })
  const [toast, setToast] = useState({ message: '', type: 'error' })
  const [loading, setLoading] = useState(false)

  const handleChange = (e) => setForm(f => ({ ...f, [e.target.name]: e.target.value }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    setToast({ message: '', type: 'error' })
    setLoading(true)
    try {
      await login(form.email, form.password)
      setToast({ message: 'Signed in successfully!', type: 'success' })
      setTimeout(() => navigate('/events'), 500)
    } catch (err) {
      const message = err.response?.data?.message || 'Invalid credentials.'
      setToast({ message, type: 'error' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{
      minHeight: '100vh', display: 'flex',
      alignItems: 'center', justifyContent: 'center',
      background: 'radial-gradient(ellipse at top, #161208 0%, var(--bg) 60%)',
      padding: 24,
    }}>
      <Toast message={toast.message} type={toast.type} onClose={() => setToast({ message: '', type: 'error' })} />
      <div className="fade-in" style={{ width: '100%', maxWidth: 400 }}>
        {/* Header */}
        <div style={{ textAlign: 'center', marginBottom: 40 }}>
          <div style={{
            width: 48, height: 48, background: 'var(--gold)',
            borderRadius: 8, display: 'flex', alignItems: 'center', justifyContent: 'center',
            margin: '0 auto 20px',
          }}>
            <span style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: 26, color: '#000' }}>E</span>
          </div>
          <h1 style={{ fontSize: 32, marginBottom: 8 }}>Welcome back</h1>
          <p style={{ color: 'var(--text-muted)', fontSize: 14 }}>Sign in to your account</p>
        </div>

        <div className="card" style={{ padding: 32 }}>
          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>

            <div className="input-group">
              <label>Email</label>
              <input
                type="email" name="email" required
                placeholder="you@example.com"
                value={form.email} onChange={handleChange}
              />
            </div>

            <div className="input-group">
              <label>Password</label>
              <input
                type="password" name="password" required
                placeholder="••••••••"
                value={form.password} onChange={handleChange}
              />
            </div>

            <button
              type="submit" className="btn btn-gold"
              style={{ width: '100%', justifyContent: 'center', padding: '12px 0', marginTop: 4 }}
              disabled={loading}
            >
              {loading ? <><div className="spinner" style={{ width: 14, height: 14 }} /> Signing in...</> : 'Sign In'}
            </button>
          </form>

          <div className="divider" />

          <p style={{ textAlign: 'center', color: 'var(--text-muted)', fontSize: 13 }}>
            Don't have an account?{' '}
            <Link to="/register" style={{ color: 'var(--gold)' }}>Register</Link>
          </p>
        </div>
      </div>
    </div>
  )
}