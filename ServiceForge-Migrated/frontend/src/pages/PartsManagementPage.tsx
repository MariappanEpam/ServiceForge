import { useEffect, useMemo, useState } from 'react'
import { HttpError } from '../api/http'
import {
  serviceforgeApi,
  type CreatePartRequest,
  type Part,
  type UpdatePartRequest,
} from '../api/serviceforgeApi'
import ErrorPanel from '../components/ErrorPanel'
import Loading from '../components/Loading'
import './Pages.css'

function normalizeError(e: unknown): Error {
  return e instanceof Error ? e : new Error(String(e))
}

const emptyCreate: CreatePartRequest = {
  sku: '',
  name: '',
  description: '',
  quantityOnHand: 0,
  active: true,
  orderable: true,
}

export default function PartsManagementPage() {
  const [items, setItems] = useState<Part[] | null>(null)
  const [error, setError] = useState<Error | null>(null)
  const [query, setQuery] = useState('')
  const [activeOnly, setActiveOnly] = useState(true)

  const [creating, setCreating] = useState(false)
  const [createForm, setCreateForm] = useState<CreatePartRequest>(emptyCreate)

  const [editingSku, setEditingSku] = useState<string | null>(null)
  const [editForm, setEditForm] = useState<UpdatePartRequest | null>(null)

  async function refresh() {
    setError(null)
    setItems(null)
    try {
      const data = await serviceforgeApi.listPartsManaged({ q: query.trim() || undefined, activeOnly })
      setItems(data)
    } catch (e) {
      setError(normalizeError(e))
    }
  }

  useEffect(() => {
    void refresh()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeOnly])

  const canCreate = useMemo(() => {
    return (
      !creating &&
      createForm.sku.trim().length > 0 &&
      createForm.name.trim().length > 0 &&
      Number.isFinite(createForm.quantityOnHand) &&
      createForm.quantityOnHand >= 0
    )
  }, [creating, createForm])

  async function onCreate(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    setCreating(true)
    try {
      const req: CreatePartRequest = {
        ...createForm,
        sku: createForm.sku.trim(),
        name: createForm.name.trim(),
        description: createForm.description?.trim() || undefined,
      }
      await serviceforgeApi.createPart(req)
      setCreateForm(emptyCreate)
      await refresh()
    } catch (err) {
      if (err instanceof HttpError && err.status === 409) {
        setError(new Error('SKU already exists. Choose a unique SKU.'))
      } else {
        setError(normalizeError(err))
      }
    } finally {
      setCreating(false)
    }
  }

  function startEdit(p: Part) {
    setEditingSku(p.sku)
    setEditForm({
      name: p.name,
      description: p.description,
      quantityOnHand: p.quantityOnHand,
      active: p.active,
      orderable: p.orderable,
    })
  }

  async function saveEdit() {
    if (!editingSku || !editForm) return
    setError(null)
    try {
      const req: UpdatePartRequest = {
        ...editForm,
        name: editForm.name.trim(),
        description: editForm.description?.trim() || undefined,
      }
      await serviceforgeApi.updatePart(editingSku, req)
      setEditingSku(null)
      setEditForm(null)
      await refresh()
    } catch (e) {
      setError(normalizeError(e))
    }
  }

  async function onDelete(sku: string) {
    const ok = window.confirm(`Delete part ${sku}? This will deactivate it (soft delete).`)
    if (!ok) return
    setError(null)
    try {
      await serviceforgeApi.deletePart(sku)
      await refresh()
    } catch (e) {
      setError(normalizeError(e))
    }
  }

  return (
    <div className="page">
      <h1>Parts Management</h1>
      <p className="muted">Create, edit, and deactivate parts (Feature 7).</p>

      {error && <ErrorPanel error={error} />}

      <div className="card form" data-testid="parts-manage-filters">
        <div className="grid">
          <label className="field">
            <span>Search</span>
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Search by SKU or name"
              data-testid="parts-search"
            />
          </label>

          <label className="field">
            <span>Status</span>
            <select
              value={activeOnly ? 'active' : 'all'}
              onChange={(e) => setActiveOnly(e.target.value === 'active')}
              data-testid="parts-status"
            >
              <option value="active">Active only</option>
              <option value="all">All</option>
            </select>
          </label>
        </div>

        <div className="actions">
          <button type="button" onClick={() => void refresh()} data-testid="parts-refresh">
            Refresh
          </button>
        </div>
      </div>

      <form className="card form" onSubmit={onCreate} data-testid="parts-create-form">
        <h2 style={{ marginTop: 0 }}>Add Part</h2>
        <div className="grid">
          <label className="field">
            <span>SKU</span>
            <input
              value={createForm.sku}
              onChange={(e) => setCreateForm((p) => ({ ...p, sku: e.target.value }))}
              placeholder="P-200"
              data-testid="parts-create-sku"
            />
          </label>
          <label className="field">
            <span>Name</span>
            <input
              value={createForm.name}
              onChange={(e) => setCreateForm((p) => ({ ...p, name: e.target.value }))}
              placeholder="Part name"
              data-testid="parts-create-name"
            />
          </label>
        </div>

        <label className="field">
          <span>Description</span>
          <input
            value={createForm.description || ''}
            onChange={(e) => setCreateForm((p) => ({ ...p, description: e.target.value }))}
            placeholder="Optional"
            data-testid="parts-create-description"
          />
        </label>

        <div className="grid">
          <label className="field">
            <span>Quantity on hand</span>
            <input
              type="number"
              min={0}
              value={createForm.quantityOnHand}
              onChange={(e) => setCreateForm((p) => ({ ...p, quantityOnHand: Number(e.target.value) }))}
              data-testid="parts-create-qoh"
            />
          </label>

          <label className="field">
            <span>Orderable</span>
            <select
              value={createForm.orderable ? 'yes' : 'no'}
              onChange={(e) => setCreateForm((p) => ({ ...p, orderable: e.target.value === 'yes' }))}
              data-testid="parts-create-orderable"
            >
              <option value="yes">Yes</option>
              <option value="no">No</option>
            </select>
          </label>
        </div>

        <div className="actions">
          <button type="submit" disabled={!canCreate} data-testid="parts-create-submit">
            {creating ? 'Creating…' : 'Create'}
          </button>
        </div>
      </form>

      {!items && !error && <Loading label="Loading parts…" />}

      {items && items.length === 0 && <div className="empty">No parts found.</div>}

      {items && items.length > 0 && (
        <div className="table" data-testid="parts-table">
          <div className="row header">
            <div>SKU</div>
            <div>Name</div>
            <div>Qty</div>
            <div>Status</div>
            <div>Orderable</div>
            <div></div>
          </div>
          {items.map((p) => (
            <div className="row" key={p.sku}>
              <div className="mono">{p.sku}</div>
              <div>{p.name}</div>
              <div className="mono">{p.quantityOnHand}</div>
              <div className={p.active ? 'success' : 'muted'}>{p.active ? 'Active' : 'Inactive'}</div>
              <div className={p.orderable ? '' : 'muted'}>{p.orderable ? 'Yes' : 'No'}</div>
              <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
                <button type="button" onClick={() => startEdit(p)} data-testid={`parts-edit-${p.sku}`}>
                  Edit
                </button>
                <button type="button" onClick={() => void onDelete(p.sku)} data-testid={`parts-delete-${p.sku}`}>
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {editingSku && editForm && (
        <div className="card form" data-testid="parts-edit-form">
          <h2 style={{ marginTop: 0 }}>Edit {editingSku}</h2>
          <div className="grid">
            <label className="field">
              <span>Name</span>
              <input
                value={editForm.name}
                onChange={(e) => setEditForm((p) => (p ? { ...p, name: e.target.value } : p))}
              />
            </label>
            <label className="field">
              <span>Quantity on hand</span>
              <input
                type="number"
                min={0}
                value={editForm.quantityOnHand}
                onChange={(e) => setEditForm((p) => (p ? { ...p, quantityOnHand: Number(e.target.value) } : p))}
              />
            </label>
          </div>

          <label className="field">
            <span>Description</span>
            <input
              value={editForm.description || ''}
              onChange={(e) => setEditForm((p) => (p ? { ...p, description: e.target.value } : p))}
            />
          </label>

          <div className="grid">
            <label className="field">
              <span>Status</span>
              <select value={editForm.active ? 'active' : 'inactive'} onChange={(e) => setEditForm((p) => (p ? { ...p, active: e.target.value === 'active' } : p))}>
                <option value="active">Active</option>
                <option value="inactive">Inactive</option>
              </select>
            </label>
            <label className="field">
              <span>Orderable</span>
              <select value={editForm.orderable ? 'yes' : 'no'} onChange={(e) => setEditForm((p) => (p ? { ...p, orderable: e.target.value === 'yes' } : p))}>
                <option value="yes">Yes</option>
                <option value="no">No</option>
              </select>
            </label>
          </div>

          <div className="actions">
            <button type="button" onClick={() => void saveEdit()} data-testid="parts-edit-save">
              Save
            </button>
            <button
              type="button"
              onClick={() => {
                setEditingSku(null)
                setEditForm(null)
              }}
              data-testid="parts-edit-cancel"
            >
              Cancel
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
