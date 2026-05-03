import { createContext, useContext, useState, useCallback } from 'react'
import { authAPI } from '../services/api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      const token = localStorage.getItem('token')
      const userData = localStorage.getItem('user')
      // Both token and user must exist to be considered authenticated
      if (!token || !userData) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        return null
      }
      return JSON.parse(userData)
    } catch {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      return null
    }
  })

  const login = useCallback(async (email, password) => {
    const { data } = await authAPI.login({ email, password })
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify({ email: data.email, role: data.role }))
    setUser({ email: data.email, role: data.role })
    return data
  }, [])

  const register = useCallback(async (formData) => {
    const { data } = await authAPI.register(formData)
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify({ email: data.email, role: data.role }))
    setUser({ email: data.email, role: data.role })
    return data
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setUser(null)
  }, [])

  const isAdmin = user?.role === 'ORGANIZER'  // ORGANIZER is the admin role in this system
  const isOrganizer = user?.role === 'ORGANIZER'
  const isStaff = user?.role === 'STAFF' || user?.role === 'ORGANIZER'

  return (
    <AuthContext.Provider value={{ user, login, register, logout, isAdmin, isOrganizer, isStaff }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider')
  return ctx
}