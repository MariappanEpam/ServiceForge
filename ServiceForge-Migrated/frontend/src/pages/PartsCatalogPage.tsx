import { useEffect, useMemo, useState } from 'react'
import { serviceforgeApi, type PartCatalogItem } from '../api/serviceforgeApi'
import ErrorPanel from '../components/ErrorPanel'
import Loading from '../components/Loading'
import './Pages.css'

export default function PartsCatalogPage() {
  const [items, setItems] = useState<PartCatalogItem[] | null>(null)
  const [error, setError] = useState<Error | null>(null)
  const [query, setQuery] = useState('')

  useEffect(() => {
    let cancelled = false
    setError(null)
    serviceforgeApi
      .listParts()
      .then((data) => {
        if (!cancelled) setItems(data)
      })
      .catch((e) => {
        if (!cancelled) setError(e instanceof Error ? e : new Error(String(e)))
      })
    return () => {
      cancelled = true
    }
  }, [])

  const filtered = useMemo(() => {
    if (!items) return []
    const q = query.trim().toLowerCase()
    if (!q) return items
    return items.filter((p) => p.sku.toLowerCase().includes(q) || p.name.toLowerCase().includes(q))
  }, [items, query])

  return (
    <div className="page">
      <h1>Parts Catalog</h1>
      <p className="muted">Active + orderable parts from the backend catalog.</p>

      <div className="card">
        <label className="field">
          <span>Search</span>
          <input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search by SKU or name" />
        </label>
      </div>

      {error && <ErrorPanel error={error} />}
      {!items && !error && <Loading label="Loading parts…" />}

      {items && filtered.length === 0 && <div className="empty">No parts found.</div>}

      {items && filtered.length > 0 && (
        <div className="table">
          <div className="row header">
            <div>SKU</div>
            <div>Name</div>
            <div>Description</div>
          </div>
          {filtered.map((p) => (
            <div className="row" key={p.sku}>
              <div className="mono">{p.sku}</div>
              <div>{p.name}</div>
              <div className="muted">{p.description || '—'}</div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
