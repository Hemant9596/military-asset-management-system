import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react'
import {
  BrowserRouter,
  Link,
  Navigate,
  Outlet,
  Route,
  Routes,
  useLocation,
  useNavigate,
} from 'react-router-dom'
import axios from 'axios'

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080'
const api = axios.create({ baseURL: API_BASE })

const AuthContext = createContext(null)

async function fetchJson(url, options = {}, token) {
  const headers = { ...(options.headers || {}) }
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }
  if (options.body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }
  try {
    const response = await api.request({
      url,
      method: options.method || 'GET',
      params: options.params,
      data: typeof options.body === 'string' ? JSON.parse(options.body) : options.body,
      headers,
    })
    return response.data
  } catch (error) {
    throw new Error(error.response?.data?.message || error.message || 'Request failed')
  }
}

function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('military-token'))
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem('military-user')
    return raw ? JSON.parse(raw) : null
  })

  const login = (nextToken, nextUser) => {
    localStorage.setItem('military-token', nextToken)
    localStorage.setItem('military-user', JSON.stringify(nextUser))
    setToken(nextToken)
    setUser(nextUser)
  }

  const logout = () => {
    localStorage.removeItem('military-token')
    localStorage.removeItem('military-user')
    setToken(null)
    setUser(null)
  }

  const value = useMemo(() => ({ token, user, login, logout }), [token, user])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

function useAuth() {
  return useContext(AuthContext)
}

function ProtectedRoute({ allowedRoles = [] }) {
  const { token, user } = useAuth()

  if (!token) {
    return <Navigate to="/login" replace />
  }

  if (allowedRoles.length > 0 && (!user || !allowedRoles.includes(user.role))) {
    return <Navigate to="/dashboard" replace />
  }

  return <Outlet />
}

function StatCard({ label, value, tone = 'default' }) {
  return (
    <div className={`stat-card tone-${tone}`}>
      <span>{label}</span>
      <strong>{value ?? '—'}</strong>
    </div>
  )
}

function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setIsSubmitting(true)

    try {
      const result = await fetchJson('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify(form),
      })

      login(result.token, result.user)
      navigate('/dashboard')
    } catch (err) {
      setError(err.message || 'Login failed')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="login-shell">
      <div className="login-panel">
        <div className="brand-block">
          <span className="eyebrow">MILITARY OPERATIONS</span>
          <h1>Asset Management</h1>
          <p>Secure inventory, logistics movement, and assignment accountability.</p>
        </div>

        <form onSubmit={handleSubmit} className="login-form">
          <label>
            Email
            <input
              type="email"
              value={form.email}
              onChange={(event) => setForm({ ...form, email: event.target.value })}
              required
            />
          </label>

          <label>
            Password
            <input
              type="password"
              value={form.password}
              onChange={(event) => setForm({ ...form, password: event.target.value })}
              required
            />
          </label>

          {error ? <div className="error-box">{error}</div> : null}

          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Signing in...' : 'Sign in'}
          </button>

        </form>
      </div>
    </div>
  )
}

function AppShell() {
  const { token, user, logout } = useAuth()
  const location = useLocation()

  const navItems = [
    { label: 'Dashboard', path: '/dashboard', roles: ['ADMIN', 'BASE_COMMANDER', 'LOGISTICS_OFFICER'] },
    { label: 'Inventory', path: '/inventory', roles: ['ADMIN', 'BASE_COMMANDER', 'LOGISTICS_OFFICER'] },
    { label: 'Opening balance', path: '/opening-balance', roles: ['ADMIN'] },
    { label: 'Assets', path: '/assets', roles: ['ADMIN', 'LOGISTICS_OFFICER'] },
    { label: 'Purchases', path: '/purchases', roles: ['ADMIN', 'LOGISTICS_OFFICER'] },
    { label: 'Transfer history', path: '/transfers', roles: ['ADMIN', 'BASE_COMMANDER', 'LOGISTICS_OFFICER'] },
    { label: 'Assignments', path: '/assignments', roles: ['ADMIN', 'BASE_COMMANDER'] },
    { label: 'Expenditures', path: '/expenditures', roles: ['ADMIN', 'BASE_COMMANDER'] },
    { label: 'Bases', path: '/bases', roles: ['ADMIN'] },
    { label: 'Users', path: '/users', roles: ['ADMIN'] },
  ]

  const visibleNav = navItems.filter((item) => !user || item.roles.includes(user.role))

  if (!token) {
    return <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-header">
          <div className="logo-mark">M</div>
          <div>
            <small>UNIFIED</small>
            <h2>Command Center</h2>
          </div>
        </div>

        <nav className="nav">
          {visibleNav.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              className={location.pathname === item.path ? 'active' : ''}
            >
              {item.label}
            </Link>
          ))}
        </nav>

        <div className="user-box">
          <strong>{user?.firstName} {user?.lastName}</strong>
          <span>{user?.role}</span>
          <button type="button" onClick={logout}>Sign out</button>
        </div>
      </aside>

      <main className="content-panel">
        <Routes>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />

          <Route element={<ProtectedRoute allowedRoles={['ADMIN', 'BASE_COMMANDER', 'LOGISTICS_OFFICER']} />}>
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/inventory" element={<InventoryPage />} />
            <Route path="/transfers" element={<OperationsPage kind="transfer" />} />
          </Route>

          <Route element={<ProtectedRoute allowedRoles={['ADMIN', 'LOGISTICS_OFFICER']} />}>
            <Route path="/assets" element={<AssetsPage />} />
            <Route path="/purchases" element={<OperationsPage kind="purchase" />} />
          </Route>

          <Route element={<ProtectedRoute allowedRoles={['ADMIN', 'BASE_COMMANDER']} />}>
            <Route path="/assignments" element={<OperationsPage kind="assignment" />} />
            <Route path="/expenditures" element={<OperationsPage kind="expenditure" />} />
          </Route>

          <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
            <Route path="/opening-balance" element={<OperationsPage kind="opening" />} />
            <Route path="/bases" element={<BasesPage />} />
            <Route path="/users" element={<UsersPage />} />
          </Route>

          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </main>
    </div>
  )
}

function DashboardPage() {
  const { token, user } = useAuth()
  const [summary, setSummary] = useState(null)
  const [error, setError] = useState('')
  const [bases, setBases] = useState([])
  const [equipmentTypes, setEquipmentTypes] = useState([])
  const [details, setDetails] = useState(null)
  const [detailError, setDetailError] = useState('')
  const [loadedFilterKey, setLoadedFilterKey] = useState('')
  const today = new Date().toISOString().slice(0, 10)
  const monthStart = `${today.slice(0, 8)}01`
  const [filters, setFilters] = useState({ from: monthStart, to: today, baseId: '', equipmentTypeId: '' })
  const queryParameters = new URLSearchParams()
  if (filters.from) queryParameters.set('from', filters.from)
  if (filters.to) queryParameters.set('to', filters.to)
  if (user?.role === 'ADMIN' && filters.baseId) queryParameters.set('baseId', filters.baseId)
  if (filters.equipmentTypeId) queryParameters.set('equipmentTypeId', filters.equipmentTypeId)
  const dashboardQuery = queryParameters.toString()

  useEffect(() => {
    Promise.all([
      fetchJson('/api/bases', {}, token),
      fetchJson('/api/equipment-types', {}, token),
    ]).then(([nextBases, nextTypes]) => {
      setBases(nextBases || [])
      setEquipmentTypes(nextTypes || [])
    }).catch((err) => setError(err.message))
  }, [token])

  useEffect(() => {
    fetchJson(`/api/dashboard?${dashboardQuery}`, {}, token)
      .then((data) => {
        setSummary(data)
        setLoadedFilterKey(dashboardQuery)
      })
      .catch((err) => setError(err.message))
  }, [token, dashboardQuery])

  async function showMovementDetails() {
    setDetailError('')
    try {
      const params = {
        from: filters.from || undefined,
        to: filters.to || undefined,
        baseId: user?.role === 'ADMIN' ? filters.baseId || undefined : undefined,
        equipmentTypeId: filters.equipmentTypeId || undefined,
      }
      setDetails(await fetchJson('/api/dashboard/movement-details', { params }, token))
    } catch (err) {
      setDetailError(err.message)
    }
  }

  if (error) return <div className="page-card"><p className="error-box">{error}</p></div>
  if (!summary || loadedFilterKey !== dashboardQuery) return <div className="page-card">Loading dashboard...</div>

  return (
    <div className="page-card">
      <div className="page-header">
        <div>
          <span className="eyebrow">OPERATIONS OVERVIEW</span>
          <h1>Dashboard</h1>
        </div>
        <div className="filter-row" aria-label="Dashboard filters">
          <label>From<input type="date" value={filters.from} onChange={(event) => setFilters({ ...filters, from: event.target.value })} /></label>
          <label>To<input type="date" value={filters.to} onChange={(event) => setFilters({ ...filters, to: event.target.value })} /></label>
          {user?.role === 'ADMIN' && <label>Base<select value={filters.baseId} onChange={(event) => setFilters({ ...filters, baseId: event.target.value })}><option value="">All bases</option>{bases.map((base) => <option key={base.id} value={base.id}>{base.name}</option>)}</select></label>}
          <label>Equipment type<select value={filters.equipmentTypeId} onChange={(event) => setFilters({ ...filters, equipmentTypeId: event.target.value })}><option value="">All types</option>{equipmentTypes.map((type) => <option key={type.id} value={type.id}>{type.name}</option>)}</select></label>
        </div>
      </div>

      <div className="stats-grid">
        <StatCard label="Opening balance" value={summary.openingBalance} />
        <StatCard label="Purchases" value={summary.purchases} tone="positive" />
        <StatCard label="Transfer in" value={summary.transferIn} tone="info" />
        <StatCard label="Transfer out" value={summary.transferOut} tone="warning" />
        <StatCard label="Assigned assets" value={summary.assignedAssets} />
        <StatCard label="Expended assets" value={summary.expendedAssets} tone="danger" />
        <StatCard label="Closing balance" value={summary.closingBalance} tone="success" />
        <button type="button" className="stat-card tone-info detail-trigger" onClick={showMovementDetails}>
          <span>Net movement</span><strong>{summary.netMovement}</strong><small>View transaction details</small>
        </button>
      </div>

      <div className="two-column">
        <div className="panel">
          <h3>Movement trend</h3>
          <ul className="list-stack">
            {(summary.movementTrend || []).map((item, index) => (
              <li key={`${item.label || item.date}-${index}`}><span>{item.label || item.date}</span><strong>{item.value ?? item.movement}</strong></li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h3>Inventory by type</h3>
          <ul className="list-stack">
            {(summary.inventoryByType || []).map((item, index) => (
              <li key={`${item.name}-${index}`}><span>{item.name}</span><strong>{item.quantity ?? item.value}</strong></li>
            ))}
          </ul>
        </div>
      </div>
      {details && <div className="modal-backdrop" role="presentation" onClick={() => setDetails(null)}>
        <section className="detail-modal" role="dialog" aria-modal="true" aria-labelledby="movement-title" onClick={(event) => event.stopPropagation()}>
          <div className="page-header"><div><span className="eyebrow">JOURNAL</span><h2 id="movement-title">Net movement details</h2></div><button type="button" className="icon-button" aria-label="Close details" onClick={() => setDetails(null)}>×</button></div>
          {detailError && <p className="error-box">{detailError}</p>}
          <p>Opening {details.openingBalance} · Net movement {details.netMovement} · Closing {details.closingBalance}</p>
          {['openingBalances', 'purchases', 'transfers', 'expenditures'].map((group) => <section className="detail-group" key={group}>
            <h3>{group[0].toUpperCase() + group.slice(1)}</h3>
            {details[group]?.length ? <div className="table-wrap"><table><thead><tr>{Object.keys(details[group][0]).map((key) => <th key={key}>{key}</th>)}</tr></thead><tbody>{details[group].map((row, index) => <tr key={`${group}-${index}`}>{Object.values(row).map((value, cellIndex) => <td key={cellIndex}>{value ?? '—'}</td>)}</tr>)}</tbody></table></div> : <p>No records in this period.</p>}
          </section>)}
        </section>
      </div>}
    </div>
  )
}

const operationConfigs = {
  opening: {
    title: 'Opening balance', endpoint: '/api/opening-balances', dateKey: 'effectiveDate',
    columns: [['effectiveDate', 'Effective date'], ['baseName', 'Base'], ['assetName', 'Asset'], ['quantity', 'Quantity'], ['createdBy', 'Recorded by']],
  },
  purchase: {
    title: 'Purchases', endpoint: '/api/purchases', dateKey: 'purchaseDate',
    columns: [['purchaseDate', 'Date'], ['baseName', 'Base'], ['assetName', 'Asset'], ['quantity', 'Quantity'], ['referenceNumber', 'Reference'], ['vendor', 'Vendor']],
  },
  transfer: {
    title: 'Transfer history', endpoint: '/api/transfers', dateKey: 'transferDate',
    columns: [['transferDate', 'Date'], ['sourceBaseName', 'From'], ['destinationBaseName', 'To'], ['assetName', 'Asset'], ['quantity', 'Quantity'], ['referenceNumber', 'Reference']],
  },
  assignment: {
    title: 'Asset assignments', endpoint: '/api/assignments', dateKey: 'assignmentDate',
    columns: [['assignmentDate', 'Date'], ['baseName', 'Base'], ['assetName', 'Asset'], ['assignedToName', 'Assigned to'], ['quantity', 'Quantity'], ['status', 'Status']],
  },
  expenditure: {
    title: 'Expenditures', endpoint: '/api/expenditures', dateKey: 'expenditureDate',
    columns: [['expenditureDate', 'Date'], ['baseName', 'Base'], ['assetName', 'Asset'], ['quantity', 'Quantity'], ['reason', 'Reason'], ['reference', 'Reference']],
  },
}

function OperationsPage({ kind }) {
  const { token, user } = useAuth()
  const config = operationConfigs[kind]
  const today = new Date().toISOString().slice(0, 10)
  const [records, setRecords] = useState([])
  const [bases, setBases] = useState([])
  const [assets, setAssets] = useState([])
  const [recipients, setRecipients] = useState([])
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)
  const [form, setForm] = useState({
    baseId: user?.assignedBaseId ? String(user.assignedBaseId) : '',
    sourceBaseId: user?.assignedBaseId ? String(user.assignedBaseId) : '',
    destinationBaseId: '',
    assetId: '',
    assignedToUserId: '',
    quantity: '',
    [config.dateKey]: today,
    unitCost: '',
    referenceNumber: '',
    reference: '',
    vendor: '',
    reason: '',
    notes: '',
    status: 'ACTIVE',
  })

  async function loadRecords() {
    const data = await fetchJson(config.endpoint, {}, token)
    setRecords(data || [])
  }

  useEffect(() => {
    Promise.all([
      fetchJson(config.endpoint, {}, token),
      fetchJson('/api/bases', {}, token),
      fetchJson('/api/assets', {}, token),
    ]).then(([nextRecords, nextBases, nextAssets]) => {
      setRecords(nextRecords || [])
      setBases(nextBases || [])
      setAssets((nextAssets || []).filter((asset) => asset.active))
    }).catch((err) => setError(err.message))
  }, [token, config.endpoint])

  useEffect(() => {
    if (kind !== 'assignment') return
    const baseId = user?.role === 'ADMIN' ? form.baseId : user?.assignedBaseId
    fetchJson('/api/users/assignable', { params: baseId ? { baseId } : {} }, token)
      .then((data) => setRecipients(data || []))
      .catch((err) => setError(err.message))
  }, [kind, token, user?.role, user?.assignedBaseId, form.baseId])

  async function submit(event) {
    event.preventDefault()
    setSaving(true)
    setError('')
    const request = {
      assetId: Number(form.assetId),
      quantity: Number(form.quantity),
      [config.dateKey]: form[config.dateKey],
      notes: form.notes,
    }
    if (kind === 'transfer') {
      request.sourceBaseId = Number(form.sourceBaseId)
      request.destinationBaseId = Number(form.destinationBaseId)
    } else {
      request.baseId = Number(form.baseId)
    }
    if (kind === 'purchase') {
      request.unitCost = Number(form.unitCost)
      request.vendor = form.vendor
      request.referenceNumber = form.referenceNumber
    }
    if (kind === 'opening') request.effectiveDate = form.effectiveDate
    if (kind === 'transfer') request.referenceNumber = form.referenceNumber
    if (kind === 'assignment') {
      request.assignedToUserId = Number(form.assignedToUserId)
      request.status = 'ACTIVE'
    }
    if (kind === 'expenditure') {
      request.reason = form.reason
      request.reference = form.reference
    }
    try {
      await fetchJson(config.endpoint, { method: 'POST', body: request }, token)
      await loadRecords()
      setForm((current) => ({ ...current, quantity: '', unitCost: '', referenceNumber: '', reference: '', vendor: '', reason: '', notes: '' }))
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  const isAdmin = user?.role === 'ADMIN'
  const baseSelect = (name, label, disabled = false) => <label key={name}>{label}<select required value={form[name]} disabled={disabled} onChange={(event) => setForm({ ...form, [name]: event.target.value })}><option value="">Select base</option>{bases.map((base) => <option key={base.id} value={base.id}>{base.name}</option>)}</select></label>
  const assetSelect = <label key="assetId">Asset<select required value={form.assetId} onChange={(event) => setForm({ ...form, assetId: event.target.value })}><option value="">Select asset</option>{assets.map((asset) => <option key={asset.id} value={asset.id}>{asset.name}</option>)}</select></label>
  const quantityField = <label key="quantity">Quantity<input type="number" min="1" step="1" required value={form.quantity} onChange={(event) => setForm({ ...form, quantity: event.target.value })} /></label>
  const dateField = <label key={config.dateKey}>Date<input type="date" required value={form[config.dateKey]} onChange={(event) => setForm({ ...form, [config.dateKey]: event.target.value })} /></label>
  const optionalInput = (name, label, type = 'text', required = false) => <label key={name}>{label}<input type={type} min={type === 'number' ? '0' : undefined} step={type === 'number' ? '0.01' : undefined} required={required} value={form[name]} onChange={(event) => setForm({ ...form, [name]: event.target.value })} /></label>

  let fields
  if (kind === 'opening') fields = [baseSelect('baseId', 'Base'), assetSelect, quantityField, dateField]
  if (kind === 'purchase') fields = [baseSelect('baseId', 'Base', !isAdmin), assetSelect, quantityField, dateField, optionalInput('unitCost', 'Unit cost', 'number', true), optionalInput('vendor', 'Vendor'), optionalInput('referenceNumber', 'Reference')]
  if (kind === 'transfer') fields = [baseSelect('sourceBaseId', 'From base', !isAdmin), baseSelect('destinationBaseId', 'To base', false), assetSelect, quantityField, dateField, optionalInput('referenceNumber', 'Reference')]
  if (kind === 'assignment') fields = [baseSelect('baseId', 'Base', !isAdmin), assetSelect, <label key="assignedToUserId">Assign to<select required value={form.assignedToUserId} onChange={(event) => setForm({ ...form, assignedToUserId: event.target.value })}><option value="">Select user</option>{recipients.map((recipient) => <option key={recipient.id} value={recipient.id}>{recipient.firstName} {recipient.lastName}</option>)}</select></label>, quantityField, dateField]
  if (kind === 'expenditure') fields = [baseSelect('baseId', 'Base', !isAdmin), assetSelect, quantityField, dateField, optionalInput('reason', 'Reason', 'text', true), optionalInput('reference', 'Reference')]

  return <div className="page-card">
    <div className="page-header"><div><span className="eyebrow">OPERATIONS</span><h1>{config.title}</h1></div></div>
    <form className="entry-form" onSubmit={submit}>
      <div className="form-grid">{fields}<label className="wide-field">Notes<textarea rows="2" value={form.notes} onChange={(event) => setForm({ ...form, notes: event.target.value })} /></label></div>
      {error && <p className="error-box">{error}</p>}
      <button className="primary-button" type="submit" disabled={saving || bases.length === 0 || assets.length === 0}>{saving ? 'Saving...' : `Record ${kind}`}</button>
    </form>
    <div className="section-heading"><h2>Recorded transactions</h2><span>{records.length} records</span></div>
    <div className="table-wrap"><table><thead><tr>{config.columns.map(([key, label]) => <th key={key}>{label}</th>)}</tr></thead><tbody>
      {records.map((record) => <tr key={record.id}>{config.columns.map(([key]) => <td key={key}>{record[key] ?? '—'}</td>)}</tr>)}
      {records.length === 0 && <tr><td colSpan={config.columns.length}>No transactions recorded.</td></tr>}
    </tbody></table></div>
  </div>
}

function InventoryPage() {
  const { token } = useAuth()
  const [items, setItems] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    fetchJson('/api/inventory', {}, token)
      .then((data) => setItems(data || []))
      .catch((err) => setError(err.message))
  }, [token])

  if (error) return <div className="page-card"><p className="error-box">{error}</p></div>

  return (
    <div className="page-card">
      <div className="page-header">
        <div>
          <span className="eyebrow">CURRENT STOCK</span>
          <h1>Inventory</h1>
        </div>
      </div>

      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Base</th>
              <th>Asset</th>
              <th>Type</th>
              <th>Total</th>
              <th>Available</th>
              <th>Assigned</th>
              <th>Expended</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id}>
                <td>{item.baseName}</td>
                <td>{item.assetName}</td>
                <td>{item.equipmentTypeName}</td>
                <td>{item.totalQuantity}</td>
                <td>{item.availableQuantity}</td>
                <td>{item.assignedQuantity}</td>
                <td>{item.expendedQuantity}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

function AssetsPage() {
  const { token, user } = useAuth()
  const [items, setItems] = useState([])
  const [types, setTypes] = useState([])
  const [error, setError] = useState('')
  const [assetForm, setAssetForm] = useState({ name: '', equipmentTypeId: '', serialNumber: '', model: '', unit: '', description: '' })
  const [typeForm, setTypeForm] = useState({ name: '', description: '' })

  useEffect(() => {
    Promise.all([fetchJson('/api/assets', {}, token), fetchJson('/api/equipment-types', {}, token)])
      .then(([assets, equipmentTypes]) => { setItems(assets || []); setTypes(equipmentTypes || []) })
      .catch((err) => setError(err.message))
  }, [token])

  async function createAsset(event) {
    event.preventDefault()
    try {
      await fetchJson('/api/assets', { method: 'POST', body: { ...assetForm, equipmentTypeId: Number(assetForm.equipmentTypeId), active: true } }, token)
      setItems(await fetchJson('/api/assets', {}, token))
      setAssetForm({ name: '', equipmentTypeId: '', serialNumber: '', model: '', unit: '', description: '' })
    } catch (err) { setError(err.message) }
  }

  async function createType(event) {
    event.preventDefault()
    try {
      await fetchJson('/api/equipment-types', { method: 'POST', body: typeForm }, token)
      setTypes(await fetchJson('/api/equipment-types', {}, token))
      setTypeForm({ name: '', description: '' })
    } catch (err) { setError(err.message) }
  }

  if (error) return <div className="page-card"><p className="error-box">{error}</p></div>

  return (
    <div className="page-card">
      <div className="page-header">
        <div>
          <span className="eyebrow">CATALOG</span>
          <h1>Assets</h1>
        </div>
      </div>
      {error && <p className="error-box">{error}</p>}
      <form className="entry-form" onSubmit={createAsset}>
        <h2>Register asset</h2>
        <div className="form-grid">
          <label>Name<input required value={assetForm.name} onChange={(event) => setAssetForm({ ...assetForm, name: event.target.value })} /></label>
          <label>Equipment type<select required value={assetForm.equipmentTypeId} onChange={(event) => setAssetForm({ ...assetForm, equipmentTypeId: event.target.value })}><option value="">Select type</option>{types.map((type) => <option key={type.id} value={type.id}>{type.name}</option>)}</select></label>
          <label>Serial number<input value={assetForm.serialNumber} onChange={(event) => setAssetForm({ ...assetForm, serialNumber: event.target.value })} /></label>
          <label>Model<input value={assetForm.model} onChange={(event) => setAssetForm({ ...assetForm, model: event.target.value })} /></label>
          <label>Unit<input value={assetForm.unit} onChange={(event) => setAssetForm({ ...assetForm, unit: event.target.value })} /></label>
          <label>Description<input value={assetForm.description} onChange={(event) => setAssetForm({ ...assetForm, description: event.target.value })} /></label>
        </div>
        <button className="primary-button" type="submit" disabled={!types.length}>Save asset</button>
      </form>
      {user?.role === 'ADMIN' && <form className="entry-form compact-form" onSubmit={createType}>
        <h2>Add equipment type</h2><div className="form-grid">
          <label>Name<input required value={typeForm.name} onChange={(event) => setTypeForm({ ...typeForm, name: event.target.value })} /></label>
          <label>Description<input value={typeForm.description} onChange={(event) => setTypeForm({ ...typeForm, description: event.target.value })} /></label>
        </div><button className="secondary-button" type="submit">Save type</button>
      </form>}

      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Type</th>
              <th>Serial</th>
              <th>Model</th>
              <th>Unit</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id}>
                <td>{item.name}</td>
                <td>{item.equipmentTypeName}</td>
                <td>{item.serialNumber}</td>
                <td>{item.model}</td>
                <td>{item.unit}</td>
                <td>{item.active ? 'Active' : 'Inactive'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

function BasesPage() {
  const { token } = useAuth()
  const [items, setItems] = useState([])
  const [error, setError] = useState('')
  const [form, setForm] = useState({ name: '', location: '', description: '' })

  useEffect(() => {
    fetchJson('/api/bases', {}, token)
      .then((data) => setItems(data || []))
      .catch((err) => setError(err.message))
  }, [token])

  async function createBase(event) {
    event.preventDefault()
    setError('')
    try {
      await fetchJson('/api/bases', { method: 'POST', body: { ...form, active: true } }, token)
      setItems(await fetchJson('/api/bases', {}, token))
      setForm({ name: '', location: '', description: '' })
    } catch (err) { setError(err.message) }
  }

  if (error) return <div className="page-card"><p className="error-box">{error}</p></div>

  return (
    <div className="page-card">
      <div className="page-header">
        <div>
          <span className="eyebrow">INSTALLATIONS</span>
          <h1>Bases</h1>
        </div>
      </div>
      {error && <p className="error-box">{error}</p>}
      <form className="entry-form" onSubmit={createBase}>
        <h2>Register base</h2><div className="form-grid">
          <label>Name<input required value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} /></label>
          <label>Location<input value={form.location} onChange={(event) => setForm({ ...form, location: event.target.value })} /></label>
          <label>Description<input value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} /></label>
        </div><button className="primary-button" type="submit">Save base</button>
      </form>
      <div className="panel-grid">
        {items.map((item) => (
          <div className="panel" key={item.id}>
            <h3>{item.name}</h3>
            <p>{item.location}</p>
            <small>{item.description}</small>
            <div className="status-pill">{item.active ? 'Operational' : 'Inactive'}</div>
          </div>
        ))}
      </div>
    </div>
  )
}

function UsersPage() {
  const { token } = useAuth()
  const [items, setItems] = useState([])
  const [bases, setBases] = useState([])
  const [error, setError] = useState('')
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', password: '', role: 'BASE_COMMANDER', baseId: '' })

  useEffect(() => {
    Promise.all([fetchJson('/api/users', {}, token), fetchJson('/api/bases', {}, token)])
      .then(([users, nextBases]) => { setItems(users || []); setBases(nextBases || []) })
      .catch((err) => setError(err.message))
  }, [token])

  async function createUser(event) {
    event.preventDefault()
    setError('')
    try {
      const request = { ...form, baseId: form.role === 'ADMIN' || !form.baseId ? null : Number(form.baseId) }
      await fetchJson('/api/users', { method: 'POST', body: request }, token)
      setItems(await fetchJson('/api/users', {}, token))
      setForm({ firstName: '', lastName: '', email: '', password: '', role: 'BASE_COMMANDER', baseId: '' })
    } catch (err) { setError(err.message) }
  }

  if (error) return <div className="page-card"><p className="error-box">{error}</p></div>

  return (
    <div className="page-card">
      <div className="page-header">
        <div>
          <span className="eyebrow">TEAM ACCESS</span>
          <h1>Users</h1>
        </div>
      </div>
      {error && <p className="error-box">{error}</p>}
      <form className="entry-form" onSubmit={createUser}>
        <h2>Create user</h2><div className="form-grid">
          <label>First name<input required value={form.firstName} onChange={(event) => setForm({ ...form, firstName: event.target.value })} /></label>
          <label>Last name<input required value={form.lastName} onChange={(event) => setForm({ ...form, lastName: event.target.value })} /></label>
          <label>Email<input type="email" required value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} /></label>
          <label>Temporary password<input type="password" minLength="12" required value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} /></label>
          <label>Role<select value={form.role} onChange={(event) => setForm({ ...form, role: event.target.value, baseId: '' })}><option value="ADMIN">Admin</option><option value="BASE_COMMANDER">Base Commander</option><option value="LOGISTICS_OFFICER">Logistics Officer</option></select></label>
          {form.role !== 'ADMIN' && <label>Assigned base<select required value={form.baseId} onChange={(event) => setForm({ ...form, baseId: event.target.value })}><option value="">Select base</option>{bases.map((base) => <option key={base.id} value={base.id}>{base.name}</option>)}</select></label>}
        </div><button className="primary-button" type="submit">Create account</button>
      </form>

      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Role</th>
              <th>Base</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id}>
                <td>{item.firstName} {item.lastName}</td>
                <td>{item.email}</td>
                <td>{item.role}</td>
                <td>{item.assignedBaseName || '—'}</td>
                <td>{item.enabled ? 'Active' : 'Disabled'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppShell />
      </AuthProvider>
    </BrowserRouter>
  )
}

export default App
