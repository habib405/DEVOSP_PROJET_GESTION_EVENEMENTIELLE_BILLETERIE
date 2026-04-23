import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Toast from '../components/Toast'

const ROLES = ['ATTENDEE', 'ORGANIZER']

export default function Register() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({
    firstName: '', lastName: '', email: '', password: '', role: 'ATTENDEE'
  })
  const [toast, setToast] = useState({ message: '', type: 'error' })
  const [loading, setLoading] = useState(false)

  const handleChange = (e) => setForm(f => ({ ...f, [e.target.name]: e.target.value }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    setToast({ message: '', type: 'error' })
    setLoading(true)
    try {
      await register(form)
      setToast({ message: 'Account created successfully!', type: 'success' })
      setTimeout(() => navigate('/events'), 500)
    } catch (err) {
      const message = err.response?.data?.message || 'Registration failed. Please try again.'
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
      <div className="fade-in" style={{ width: '100%', maxWidth: 440 }}>
        <div style={{ textAlign: 'center', marginBottom: 40 }}>
          <div style={{
            width: 48, height: 48, background: 'var(--gold)',
            borderRadius: 8, display: 'flex', alignItems: 'center', justifyContent: 'center',
            margin: '0 auto 20px',
          }}>
            <span style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: 26, color: '#000' }}>E</span>
          </div>
          <h1 style={{ fontSize: 32, marginBottom: 8 }}>Create account</h1>
          <p style={{ color: 'var(--text-muted)', fontSize: 14 }}>Join the platform</p>
        </div>

        <div className="card" style={{ padding: 32 }}>
          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
              <div className="input-group">
                <label>First name</label>
                <input name="firstName" required placeholder="Jean" value={form.firstName} onChange={handleChange} />
              </div>
              <div className="input-group">
                <label>Last name</label>
                <input name="lastName" required placeholder="Dupont" value={form.lastName} onChange={handleChange} />
              </div>
            </div>

            <div className="input-group">
              <label>Email</label>
              <input type="email" name="email" required placeholder="you@example.com" value={form.email} onChange={handleChange} />
            </div>

            <div className="input-group">
              <label>Password</label>
              <input type="password" name="password" required placeholder="Min. 8 characters" value={form.password} onChange={handleChange} />
            </div>

            <div className="input-group">
              <label>Role</label>
              <select name="role" value={form.role} onChange={handleChange}>
                {ROLES.map(r => <option key={r} value={r}>{r}</option>)}
              </select>
            </div>

            <button
              type="submit" className="btn btn-gold"
              style={{ width: '100%', justifyContent: 'center', padding: '12px 0', marginTop: 4 }}
              disabled={loading}
            >
              {loading ? <><div className="spinner" style={{ width: 14, height: 14 }} /> Creating account...</> : 'Create Account'}
            </button>
          </form>

          <div className="divider" />

          <p style={{ textAlign: 'center', color: 'var(--text-muted)', fontSize: 13 }}>
            Already have an account?{' '}
            <Link to="/login" style={{ color: 'var(--gold)' }}>Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  )
}