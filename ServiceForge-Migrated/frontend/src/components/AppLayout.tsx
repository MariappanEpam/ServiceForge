import { NavLink, Outlet } from 'react-router-dom'
import './AppLayout.css'

export default function AppLayout() {
  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand">ServiceForge (Migrated)</div>
        <nav className="nav">
          <NavLink to="/technicians" className={({ isActive }) => (isActive ? 'active' : '')}>
            Technician Availability
          </NavLink>
          <NavLink to="/technicians/manage" className={({ isActive }) => (isActive ? 'active' : '')}>
            Technician Management
          </NavLink>
          <NavLink to="/jobs/new" className={({ isActive }) => (isActive ? 'active' : '')}>
            Book Job
          </NavLink>
          <NavLink to="/parts" className={({ isActive }) => (isActive ? 'active' : '')}>
            Parts Catalog
          </NavLink>
          <NavLink to="/parts/manage" className={({ isActive }) => (isActive ? 'active' : '')}>
            Parts Management
          </NavLink>
        </nav>
      </header>

      <main className="content">
        <Outlet />
      </main>
    </div>
  )
}
