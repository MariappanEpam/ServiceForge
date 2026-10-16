import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { HttpError } from '../api/http'
import { serviceforgeApi, type CreateJobRequest, type TechnicianSummary } from '../api/serviceforgeApi'
import ErrorPanel from '../components/ErrorPanel'
import Loading from '../components/Loading'
import './Pages.css'

function toIsoLocal(date: Date): string {
  const tzOffsetMin = date.getTimezoneOffset()
  const sign = tzOffsetMin <= 0 ? '+' : '-'
  const abs = Math.abs(tzOffsetMin)
  const hh = String(Math.floor(abs / 60)).padStart(2, '0')
  const mm = String(abs % 60).padStart(2, '0')
  const yyyy = date.getFullYear()
  const mo = String(date.getMonth() + 1).padStart(2, '0')
  const dd = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const m = String(date.getMinutes()).padStart(2, '0')
  const s = String(date.getSeconds()).padStart(2, '0')
  return `${yyyy}-${mo}-${dd}T${h}:${m}:${s}${sign}${hh}:${mm}`
}

export default function BookJobPage() {
  const nav = useNavigate()
  const [techs, setTechs] = useState<TechnicianSummary[] | null>(null)
  const [error, setError] = useState<Error | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [success, setSuccess] = useState<string | null>(null)

  const [technicianId, setTechnicianId] = useState<number | null>(null)
  const [customerName, setCustomerName] = useState('')
  const [start, setStart] = useState('')
  const [end, setEnd] = useState('')

  useEffect(() => {
    let cancelled = false
    setError(null)
    serviceforgeApi
      .listTechnicians()
      .then((data) => {
        if (cancelled) return
        setTechs(data)
        if (data.length > 0) setTechnicianId((prev) => prev ?? data[0].id)
      })
      .catch((e) => {
        if (!cancelled) setError(e instanceof Error ? e : new Error(String(e)))
      })
    return () => {
      cancelled = true
    }
  }, [])

  const canSubmit = useMemo(() => {
    // Keep the button visible; only disable while submitting.
    // Validation errors are shown on submit.
    return !!technicianId && !submitting
  }, [technicianId, customerName, start, end, submitting])

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    setSuccess(null)

    if (!technicianId) return
    if (!customerName.trim()) {
      setError(new Error('Customer name is required.'))
      return
    }
    if (!start || !end) {
      setError(new Error('Start and end time are required.'))
      return
    }

    const startDate = new Date(start)
    const endDate = new Date(end)
    if (!(endDate > startDate)) {
      setError(new Error('End time must be after start time.'))
      return
    }

    const req: CreateJobRequest = {
      technicianId,
      customerName: customerName.trim(),
      startTime: toIsoLocal(startDate),
      endTime: toIsoLocal(endDate),
    }

    setSubmitting(true)
    try {
      const created = await serviceforgeApi.createJob(req)
      setSuccess(`Created job #${created.id}`)
      nav('/technicians')
    } catch (err) {
      if (err instanceof HttpError && err.status === 409) {
        setError(new Error('Schedule conflict detected (includes 45-minute travel buffer). Choose another time.'))
      } else {
        setError(err instanceof Error ? err : new Error(String(err)))
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="page">
      <h1>Book Job</h1>
      <p className="muted">Create a job for a technician. Conflicts return 409.</p>

      {error && <ErrorPanel error={error} />}
      {success && <div className="success">{success}</div>}

      {!techs && !error && <Loading label="Loading technicians…" />}

      {techs && techs.length === 0 && <div className="empty">No technicians available.</div>}

      {techs && techs.length > 0 && (
        <form className="card form" onSubmit={onSubmit}>
          <label className="field">
            <span>Technician</span>
            <select value={technicianId ?? ''} onChange={(e) => setTechnicianId(Number(e.target.value))}>
              {techs.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.name}
                </option>
              ))}
            </select>
          </label>

          <label className="field">
            <span>Customer name</span>
            <input value={customerName} onChange={(e) => setCustomerName(e.target.value)} placeholder="Customer" />
          </label>

          <div className="grid">
            <label className="field">
              <span>Start</span>
              <input type="datetime-local" value={start} onChange={(e) => setStart(e.target.value)} />
            </label>
            <label className="field">
              <span>End</span>
              <input type="datetime-local" value={end} onChange={(e) => setEnd(e.target.value)} />
            </label>
          </div>

          <div className="actions">
            <button type="submit" disabled={!canSubmit}>
              {submitting ? 'Creating…' : 'Create job'}
            </button>
          </div>
        </form>
      )}
    </div>
  )
}
