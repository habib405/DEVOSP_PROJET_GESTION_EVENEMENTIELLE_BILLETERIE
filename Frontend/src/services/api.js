import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' }
})

// Attach JWT token to every request
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Redirect to login on 401 — but only for protected actions, not public browsing
api.interceptors.response.use(
  res => res,
  err => {
    const url = err.config?.url || ''
    const isPublicFetch = url.includes('/ticket-types/event/') || url.includes('/events')
    if (err.response?.status === 401 && !isPublicFetch) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

// ─── AUTH ────────────────────────────────────────────────────────────────────
export const authAPI = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
}

// ─── EVENTS ──────────────────────────────────────────────────────────────────
export const eventsAPI = {
  getPublished: () => api.get('/events'),
  getAll: () => api.get('/events/all'),           // ADMIN / ORGANIZER
  getById: (id) => api.get(`/events/${id}`),
  create: (data) => api.post('/events', data),
  update: (id, data) => api.put(`/events/${id}`, data),
  publish: (id) => api.patch(`/events/${id}/publish`),
  cancel: (id) => api.patch(`/events/${id}/cancel`),
  delete: (id) => api.delete(`/events/${id}`),
}

// ─── VENUES ──────────────────────────────────────────────────────────────────
export const venuesAPI = {
  getAll: () => api.get('/venues'),
  getById: (id) => api.get(`/venues/${id}`),
  create: (data) => api.post('/venues', data),
  update: (id, data) => api.put(`/venues/${id}`, data),
  delete: (id) => api.delete(`/venues/${id}`),
}

// ─── TICKET TYPES ────────────────────────────────────────────────────────────
export const ticketTypesAPI = {
  getByEvent: (eventId) => api.get(`/ticket-types/event/${eventId}`),
  getById: (id) => api.get(`/ticket-types/${id}`),
  create: (data) => api.post('/ticket-types', data),
  update: (id, data) => api.put(`/ticket-types/${id}`, data),
  delete: (id) => api.delete(`/ticket-types/${id}`),
}

// ─── ORDERS ──────────────────────────────────────────────────────────────────
export const ordersAPI = {
  create: (ticketTypeIds) => api.post('/orders', { ticketTypeIds }),
  getById: (id) => api.get(`/orders/${id}`),
  getMyOrders: () => api.get('/orders/my'),
  lock: (id) => api.patch(`/orders/${id}/lock`),
  pay: (id) => api.patch(`/orders/${id}/pay`),
  confirm: (id, ticketTypeIds) => api.patch(`/orders/${id}/confirm`, { ticketTypeIds }),
  cancel: (id) => api.patch(`/orders/${id}/cancel`),
  refund: (id) => api.patch(`/orders/${id}/refund`),  // ADMIN only
}

// ─── PAYMENTS (Stripe Sandbox) ───────────────────────────────────────────────
export const paymentsAPI = {
  // Creates a Stripe Checkout session for an existing order.
  // Returns { sessionId, checkoutUrl }
  checkout: (orderId, ticketTypeIds) =>
    api.post('/payments/checkout', { orderId, ticketTypeIds }),

  // Called after Stripe redirects back with ?session_id=...
  // Confirms the order, generates registrations + QR + invoice PDF email.
  finalize: (sessionId) =>
    api.post(`/payments/finalize?session_id=${encodeURIComponent(sessionId)}`),
}

// ─── REGISTRATIONS ───────────────────────────────────────────────────────────
export const registrationsAPI = {
  getMy: () => api.get('/registrations/my'),
  getByOrder: (orderId) => api.get(`/registrations/order/${orderId}`),
  getById: (id) => api.get(`/registrations/${id}`),
  cancel: (id) => api.patch(`/registrations/${id}/cancel`),
}

// ─── CHECKINS ────────────────────────────────────────────────────────────────
export const checkinsAPI = {
  scan: (registrationId) => api.post('/checkins', { registrationId }),
  getByRegistration: (regId) => api.get(`/checkins/registration/${regId}`),
  getById: (id) => api.get(`/checkins/${id}`),
}

// ─── FRAUD ───────────────────────────────────────────────────────────────────
export const fraudAPI = {
  getAll: () => api.get('/fraud'),
  getPending: (page = 0, size = 20) => api.get('/fraud/pending-review', { params: { page, size } }),
  getByOrder: (orderId) => api.get(`/fraud/order/${orderId}`),
  getById: (id) => api.get(`/fraud/${id}`),
  create: (data) => api.post('/fraud', data),
  markFalsePositive: (id, comment) => api.post(`/fraud/${id}/false-positive`, null, { params: { comment } }),
}

export default api