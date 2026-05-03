import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const { user, logout, isAdmin, isOrganizer } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const isActive = (path) => location.pathname.startsWith(path)

  return (
    <nav style={{
      position: 'sticky', top: 0, zIndex: 100,
      background: 'rgba(10,10,10,0.92)',
      backdropFilter: 'blur(12px)',
      borderBottom: '1px solid var(--border)',
    }}>
      <div style={{
        maxWidth: 1200, margin: '0 auto',
        padding: '0 32px',
        height: 60,
        display: 'flex', alignItems: 'center', justifyContent: 'space-between'
      }}>
        {/* Logo */}
        <Link to="/events" style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <div style={{
            width: 28, height: 28,
            background: 'var(--gold)',
            borderRadius: 4,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <span style={{ color: '#000', fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: 16 }}>E</span>
          </div>
          <span style={{ fontFamily: 'var(--font-display)', fontSize: 20, letterSpacing: '0.04em' }}>
            EventPlatform
          </span>
        </Link>

        {/* Nav links */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
          <NavLink to="/events" active={isActive('/events')}>Events</NavLink>
          {user && !isOrganizer && <NavLink to="/my-tickets" active={isActive('/my-tickets')}>My Tickets</NavLink>}
          {isAdmin && <NavLink to="/admin" active={isActive('/admin')}>Admin</NavLink>}
          {isAdmin && <NavLink to="/admin/fraud" active={isActive('/admin/fraud')}>Fraud</NavLink>}
        </div>

        {/* Auth */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          {user ? (
            <>
              <span style={{ fontSize: 13, color: 'var(--text-muted)' }}>
                {user.email}
                <span style={{
                  marginLeft: 8, fontSize: 11,
                  color: 'var(--gold)', background: 'var(--gold-dim)',
                  padding: '2px 8px', borderRadius: 20,
                }}>
                  {user.role}
                </span>
              </span>
              <button className="btn btn-outline" style={{ padding: '6px 16px' }} onClick={handleLogout}>
                Sign out
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn btn-ghost" style={{ padding: '6px 16px' }}>Sign in</Link>
              <Link to="/register" className="btn btn-gold" style={{ padding: '6px 16px' }}>Register</Link>
            </>
          )}
        </div>
      </div>
    </nav>
  )
}

function NavLink({ to, active, children }) {
  return (
    <Link to={to} style={{
      padding: '6px 14px',
      borderRadius: 4,
      fontSize: 13,
      fontWeight: 500,
      letterSpacing: '0.04em',
      color: active ? 'var(--gold)' : 'var(--text-muted)',
      background: active ? 'var(--gold-dim)' : 'transparent',
      transition: 'all 0.2s',
    }}>
      {children}
    </Link>
  )
}