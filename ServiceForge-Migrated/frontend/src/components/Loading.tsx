export default function Loading({ label }: { label?: string }) {
  return <div style={{ opacity: 0.85 }}>{label || 'Loading…'}</div>
}
