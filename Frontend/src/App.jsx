import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import Navbar from './components/Navbar'
import Login from './pages/Login'
import Register from './pages/Register'
import Events from './pages/Events'
import EventDetail from './pages/EventDetail'
import MyTickets from './pages/MyTickets'
import AdminDashboard from './pages/AdminDashboard'
import CreateEvent from './pages/CreateEvent'

function ProtectedRoute({ children, adminOnly = false }) {
  const { user, isAdmin } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  if (adminOnly && !isAdmin) return <Navigate to="/events" replace />
  return children
}

function Layout({ children }) {
  return (
    <>
      <Navbar />
      <main style={{ minHeight: 'calc(100vh - 60px)' }}>
        {children}
      </main>
    </>
  )
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public auth routes (no navbar) */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Routes with Navbar */}
          <Route path="/events" element={<Layout><Events /></Layout>} />
          <Route path="/events/create" element={
            <Layout>
              <ProtectedRoute><CreateEvent /></ProtectedRoute>
            </Layout>
          } />
          <Route path="/events/:id" element={<Layout><EventDetail /></Layout>} />

          <Route path="/my-tickets" element={
            <Layout>
              <ProtectedRoute><MyTickets /></ProtectedRoute>
            </Layout>
          } />

          <Route path="/admin" element={
            <Layout>
              <ProtectedRoute adminOnly><AdminDashboard /></ProtectedRoute>
            </Layout>
          } />

          {/* Default redirect */}
          <Route path="*" element={<Navigate to="/events" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}