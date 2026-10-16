import { useEffect, useMemo, useState } from 'react'
import { serviceforgeApi, type JobSummary, type TechnicianSummary } from '../api/serviceforgeApi'
import ErrorPanel from '../components/ErrorPanel'
import Loading from '../components/Loading'
import './Pages.css'

function toIsoLocal(date: Date): string {
  // Use local time but include offset in ISO string.
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

function startOfWeekLocal(now: Date): Date {
  const d = new Date(now)
  const day = d.getDay() // 0=Sun
  const diff = (day === 0 ? -6 : 1) - day // Monday start
  d.setDate(d.getDate() + diff)
  d.setHours(0, 0, 0, 0)
  return d
}

function addDays(date: Date, days: number): Date {
  const d = new Date(date)
  d.setDate(d.getDate() + days)
  return d
}

export default function TechnicianAvailabilityPage() {
  const [techs, setTechs] = useState<TechnicianSummary[] | null>(null)
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [jobs, setJobs] = useState<JobSummary[] | null>(null)
  const [error, setError] = useState<Error | null>(null)

  const [from, setFrom] = useState(() => {
    const s = startOfWeekLocal(new Date())
    return s.toISOString().slice(0, 10)
  })
  const [to, setTo] = useState(() => {
    const s = startOfWeekLocal(new Date())
    const e = addDays(s, 7)
    return e.toISOString().slice(0, 10)
  })

  useEffect(() => {
    let cancelled = false
    setError(null)
    serviceforgeApi
      .listTechnicians()
      .then((data) => {
        if (cancelled) return
        setTechs(data)
        if (data.length > 0) setSelectedId((prev) => prev ?? data[0].id)
      })
      .catch((e) => {
        if (!cancelled) setError(e instanceof Error ? e : new Error(String(e)))
      })
    return () => {
      cancelled = true
    }
  }, [])

  const fromIso = useMemo(() => {
    const d = new Date(from + 'T00:00:00')
    return toIsoLocal(d)
  }, [from])

  const toIso = useMemo(() => {
    const d = new Date(to + 'T00:00:00')
    return toIsoLocal(d)
  }, [to])

  useEffect(() => {
    if (!selectedId) return
    let cancelled = false
    setJobs(null)
    setError(null)
    serviceforgeApi
      .listTechnicianJobs(selectedId, fromIso, toIso)
      .then((data) => {
        if (!cancelled) setJobs(data)
      })
      .catch((e) => {
        if (!cancelled) setError(e instanceof Error ? e : new Error(String(e)))
      })
    return () => {
      cancelled = true
    }
  }, [selectedId, fromIso, toIso])

  const sortedJobs = useMemo(() => {
    if (!jobs) return []
    return [...jobs].sort((a, b) => a.startTime.localeCompare(b.startTime))
  }, [jobs])

  return (
    <div className="page">
      <h1>Technician Availability</h1>
      <p className="muted">Select a technician and date range to view scheduled jobs.</p>

      <div className="card grid">
        <label className="field">
          <span>Technician</span>
          <select
            value={selectedId ?? ''}
            onChange={(e) => setSelectedId(Number(e.target.value))}
            disabled={!techs || techs.length === 0}
          >
            {(techs || []).map((t) => (
              <option key={t.id} value={t.id}>
                {t.name}
              </option>
            ))}
          </select>
        </label>

        <label className="field">
          <span>From</span>
          <input type="date" value={from} onChange={(e) => setFrom(e.target.value)} />
        </label>

        <label className="field">
          <span>To</span>
          <input type="date" value={to} onChange={(e) => setTo(e.target.value)} />
        </label>
      </div>

      {error && <ErrorPanel error={error} />}
      {!techs && !error && <Loading label="Loading technicians…" />}

      {techs && techs.length === 0 && <div className="empty">No technicians available.</div>}

      {techs && selectedId && !jobs && !error && <Loading label="Loading schedule…" />}

      {jobs && jobs.length === 0 && <div className="empty">No jobs in this range.</div>}

      {jobs && jobs.length > 0 && (
        <div className="table">
          <div className="row header">
            <div>Customer</div>
            <div>Start</div>
            <div>End</div>
          </div>
          {sortedJobs.map((j) => (
            <div className="row" key={j.id}>
              <div>{j.customerName}</div>
              <div className="mono">{new Date(j.startTime).toLocaleString()}</div>
              <div className="mono">{new Date(j.endTime).toLocaleString()}</div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
