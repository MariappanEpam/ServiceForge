import { useEffect, useMemo, useState } from 'react'
import { HttpError } from '../api/http'
import {
  serviceforgeApi,
  type Technician,
  type TechnicianUpsertRequest,
  type VehicleDetails,
} from '../api/serviceforgeApi'
import ErrorPanel from '../components/ErrorPanel'
import Loading from '../components/Loading'
import './Pages.css'

function normalizeSkills(input: string): string[] {
  return input
    .split(',')
    .map((s) => s.trim())
    .filter((s) => s.length > 0)
}

type FormState = {
  name: string
  age: string
  areaServed: string
  vehicleMake: string
  vehicleModel: string
  vehicleYear: string
  skillsCsv: string
}

function toUpsertRequest(f: FormState): TechnicianUpsertRequest {
  const vehicle: VehicleDetails | undefined =
    f.vehicleMake.trim() || f.vehicleModel.trim() || f.vehicleYear.trim()
      ? {
          make: f.vehicleMake.trim() || undefined,
          model: f.vehicleModel.trim() || undefined,
          year: f.vehicleYear.trim() ? Number(f.vehicleYear.trim()) : undefined,
        }
      : undefined

  return {
    name: f.name.trim(),
    age: Number(f.age),
    areaServed: f.areaServed.trim(),
    vehicle,
    skills: normalizeSkills(f.skillsCsv),
  }
}

function fromTechnician(t: Technician): FormState {
  return {
    name: t.name ?? '',
    age: String(t.age ?? ''),
    areaServed: t.areaServed ?? '',
    vehicleMake: t.vehicle?.make ?? '',
    vehicleModel: t.vehicle?.model ?? '',
    vehicleYear: t.vehicle?.year ? String(t.vehicle.year) : '',
    skillsCsv: (t.skills || []).join(', '),
  }
}

export default function TechnicianManagementPage() {
  const [items, setItems] = useState<Technician[] | null>(null)
  const [error, setError] = useState<Error | null>(null)
  const [query, setQuery] = useState('')
  const [status, setStatus] = useState<'all' | 'active' | 'inactive'>('all')

  const [editing, setEditing] = useState<Technician | null>(null)
  const [form, setForm] = useState<FormState>({
    name: '',
    age: '',
    areaServed: '',
    vehicleMake: '',
    vehicleModel: '',
    vehicleYear: '',
    skillsCsv: '',
  })
  const [saving, setSaving] = useState(false)
  const [success, setSuccess] = useState<string | null>(null)

  async function load() {
    setError(null)
    setItems(null)
    try {
      const data = await serviceforgeApi.listTechniciansManaged({
        q: query.trim() ? query.trim() : undefined,
        status,
      })
      setItems(data)
    } catch (e) {
      setError(e instanceof Error ? e : new Error(String(e)))
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const canSave = useMemo(() => {
    return !saving
  }, [saving])

  function startCreate() {
    setSuccess(null)
    setError(null)
    setEditing(null)
    setForm({
      name: '',
      age: '',
      areaServed: '',
      vehicleMake: '',
      vehicleModel: '',
      vehicleYear: '',
      skillsCsv: '',
    })
  }

  function startEdit(t: Technician) {
    setSuccess(null)
    setError(null)
    setEditing(t)
    setForm(fromTechnician(t))
  }

  async function onSave(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    setSuccess(null)
    setSaving(true)
    try {
      const req = toUpsertRequest(form)
      if (editing) {
        await serviceforgeApi.updateTechnician(editing.id, req)
        setSuccess('Technician updated')
      } else {
        await serviceforgeApi.createTechnician(req)
        setSuccess('Technician created')
      }
      setEditing(null)
      await load()
    } catch (err) {
      if (err instanceof HttpError && err.status === 400) {
        setError(new Error(err.message))
      } else {
        setError(err instanceof Error ? err : new Error(String(err)))
      }
    } finally {
      setSaving(false)
    }
  }

  async function toggleActive(t: Technician) {
    setError(null)
    setSuccess(null)
    try {
      if (t.active) {
        await serviceforgeApi.deactivateTechnician(t.id)
        setSuccess('Technician deactivated')
      } else {
        await serviceforgeApi.reactivateTechnician(t.id)
        setSuccess('Technician reactivated')
      }
      await load()
    } catch (err) {
      setError(err instanceof Error ? err : new Error(String(err)))
    }
  }

  return (
    <div className="page">
      <h1>Technician Management</h1>
      <p className="muted">Create, edit, deactivate/reactivate technicians.</p>

      <div className="card grid">
        <label className="field">
          <span>Search (name)</span>
          <input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search by name" />
        </label>

        <label className="field">
          <span>Status</span>
          <select value={status} onChange={(e) => setStatus(e.target.value as any)}>
            <option value="all">All</option>
            <option value="active">Active</option>
            <option value="inactive">Inactive</option>
          </select>
        </label>

        <div className="actions" style={{ justifyContent: 'flex-start', gap: 12 }}>
          <button type="button" onClick={load} data-testid="tech-refresh">
            Refresh
          </button>
          <button type="button" onClick={startCreate} data-testid="tech-add">
            Add Technician
          </button>
        </div>
      </div>

      {error && <ErrorPanel error={error} />}
      {success && <div className="success">{success}</div>}
      {!items && !error && <Loading label="Loading technicians…" />}

      {items && items.length === 0 && <div className="empty">No technicians found.</div>}

      {items && items.length > 0 && (
        <div className="table">
          <div className="row header">
            <div>Name</div>
            <div>Age</div>
            <div>Area</div>
            <div>Vehicle</div>
            <div>Skills</div>
            <div>Status</div>
            <div></div>
          </div>
          {items.map((t) => (
            <div className="row" key={t.id}>
              <div>{t.name}</div>
              <div className="mono">{t.age}</div>
              <div>{t.areaServed}</div>
              <div className="muted">
                {t.vehicle?.make || t.vehicle?.model || t.vehicle?.year
                  ? `${t.vehicle?.make ?? ''} ${t.vehicle?.model ?? ''} ${t.vehicle?.year ?? ''}`.trim()
                  : '—'}
              </div>
              <div className="muted">{t.skills?.length ? t.skills.join(', ') : '—'}</div>
              <div>{t.active ? 'Active' : 'Inactive'}</div>
              <div className="actions" style={{ justifyContent: 'flex-end', gap: 8 }}>
                <button type="button" onClick={() => startEdit(t)} data-testid={`tech-edit-${t.id}`}>
                  Edit
                </button>
                <button type="button" onClick={() => toggleActive(t)} data-testid={`tech-toggle-${t.id}`}>
                  {t.active ? 'Deactivate' : 'Reactivate'}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      <div className="card" style={{ marginTop: 16 }}>
        <h2 style={{ marginTop: 0 }}>{editing ? `Edit Technician #${editing.id}` : 'Create Technician'}</h2>
        <form className="form" onSubmit={onSave}>
          <div className="grid">
            <label className="field">
              <span>Name</span>
              <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
            </label>
            <label className="field">
              <span>Age</span>
              <input value={form.age} onChange={(e) => setForm({ ...form, age: e.target.value })} />
            </label>
            <label className="field">
              <span>Area served</span>
              <input value={form.areaServed} onChange={(e) => setForm({ ...form, areaServed: e.target.value })} />
            </label>
          </div>

          <div className="grid">
            <label className="field">
              <span>Vehicle make</span>
              <input value={form.vehicleMake} onChange={(e) => setForm({ ...form, vehicleMake: e.target.value })} />
            </label>
            <label className="field">
              <span>Vehicle model</span>
              <input value={form.vehicleModel} onChange={(e) => setForm({ ...form, vehicleModel: e.target.value })} />
            </label>
            <label className="field">
              <span>Vehicle year</span>
              <input value={form.vehicleYear} onChange={(e) => setForm({ ...form, vehicleYear: e.target.value })} />
            </label>
          </div>

          <label className="field">
            <span>Skills (comma-separated)</span>
            <input value={form.skillsCsv} onChange={(e) => setForm({ ...form, skillsCsv: e.target.value })} />
          </label>

          <div className="actions">
            <button type="submit" disabled={!canSave} data-testid="tech-save">
              {saving ? 'Saving…' : editing ? 'Save changes' : 'Create technician'}
            </button>
            <button type="button" onClick={() => setEditing(null)} disabled={saving}>
              Clear
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
