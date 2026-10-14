import { HttpError } from '../api/http'
import './ErrorPanel.css'

export default function ErrorPanel({ error }: { error: unknown }) {
  if (!error) return null

  if (error instanceof HttpError) {
    const title = error.status === 401 ? 'Unauthorized' : error.status === 403 ? 'Forbidden' : 'Request failed'
    return (
      <div className="error-panel" role="alert">
        <div className="error-title">{title}</div>
        <div className="error-message">{error.message}</div>
      </div>
    )
  }

  return (
    <div className="error-panel" role="alert">
      <div className="error-title">Unexpected error</div>
      <div className="error-message">{String(error)}</div>
    </div>
  )
}
