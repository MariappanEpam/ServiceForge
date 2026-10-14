import { Navigate, Route, Routes } from 'react-router-dom'
import './App.css'
import AppLayout from './components/AppLayout'
import BookJobPage from './pages/BookJobPage'
import PartsCatalogPage from './pages/PartsCatalogPage'
import PartsManagementPage from './pages/PartsManagementPage'
import TechnicianAvailabilityPage from './pages/TechnicianAvailabilityPage'
import TechnicianManagementPage from './pages/TechnicianManagementPage'

function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route index element={<Navigate to="/technicians" replace />} />
        <Route path="/technicians" element={<TechnicianAvailabilityPage />} />
        <Route path="/technicians/manage" element={<TechnicianManagementPage />} />
        <Route path="/jobs/new" element={<BookJobPage />} />
        <Route path="/parts" element={<PartsCatalogPage />} />
        <Route path="/parts/manage" element={<PartsManagementPage />} />
        <Route path="*" element={<Navigate to="/technicians" replace />} />
      </Route>
    </Routes>
  )
}

export default App
