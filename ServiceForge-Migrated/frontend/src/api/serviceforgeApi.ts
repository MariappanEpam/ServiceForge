import { httpDelete, httpGet, httpPost, httpPut } from './http'

export type TechnicianSummary = {
  id: number
  name: string
  active?: boolean
}

export type VehicleDetails = {
  make?: string
  model?: string
  year?: number
}

export type Technician = {
  id: number
  name: string
  active: boolean
  age: number
  areaServed: string
  vehicle?: VehicleDetails
  skills: string[]
}

export type TechnicianUpsertRequest = {
  name: string
  age: number
  areaServed: string
  vehicle?: VehicleDetails
  skills?: string[]
}

export type JobSummary = {
  id: number
  technicianId: number
  customerName: string
  startTime: string
  endTime: string
}

export type CreateJobRequest = {
  technicianId: number
  customerName: string
  startTime: string
  endTime: string
}

export type Job = {
  id: number
  technicianId: number
  customerName: string
  startTime: string
  endTime: string
}

export type PartCatalogItem = {
  sku: string
  name: string
  description?: string
}

export type Part = {
  sku: string
  name: string
  description?: string
  quantityOnHand: number
  active: boolean
  orderable: boolean
}

export type CreatePartRequest = {
  sku: string
  name: string
  description?: string
  quantityOnHand: number
  active: boolean
  orderable: boolean
}

export type UpdatePartRequest = {
  name: string
  description?: string
  quantityOnHand: number
  active: boolean
  orderable: boolean
}

export const serviceforgeApi = {
  listTechnicians(): Promise<TechnicianSummary[]> {
    return httpGet('/api/technicians')
  },

  listTechniciansManaged(params?: { q?: string; status?: 'active' | 'inactive' | 'all' }): Promise<Technician[]> {
    const qs = new URLSearchParams()
    if (params?.q) qs.set('q', params.q)
    if (params?.status) qs.set('status', params.status)
    const suffix = qs.toString() ? `?${qs.toString()}` : ''
    return httpGet(`/api/technicians${suffix}`)
  },

  getTechnician(id: number): Promise<Technician> {
    return httpGet(`/api/technicians/${id}`)
  },

  createTechnician(req: TechnicianUpsertRequest): Promise<Technician> {
    return httpPost('/api/technicians', req)
  },

  updateTechnician(id: number, req: TechnicianUpsertRequest): Promise<Technician> {
    return httpPut(`/api/technicians/${id}`, req)
  },

  deactivateTechnician(id: number): Promise<Technician> {
    return httpPost(`/api/technicians/${id}/deactivate`, {})
  },

  reactivateTechnician(id: number): Promise<Technician> {
    return httpPost(`/api/technicians/${id}/reactivate`, {})
  },

  listTechnicianJobs(technicianId: number, from?: string, to?: string): Promise<JobSummary[]> {
    const params = new URLSearchParams()
    if (from) params.set('from', from)
    if (to) params.set('to', to)
    const qs = params.toString()
    return httpGet(`/api/technicians/${technicianId}/jobs${qs ? `?${qs}` : ''}`)
  },

  createJob(req: CreateJobRequest): Promise<Job> {
    return httpPost('/api/jobs', req)
  },

  listParts(): Promise<PartCatalogItem[]> {
    return httpGet('/api/parts')
  },

  listPartsManaged(params?: { q?: string; activeOnly?: boolean }): Promise<Part[]> {
    const qs = new URLSearchParams()
    if (params?.q) qs.set('q', params.q)
    if (typeof params?.activeOnly === 'boolean') qs.set('activeOnly', String(params.activeOnly))
    const suffix = qs.toString() ? `?${qs.toString()}` : ''
    return httpGet(`/api/parts${suffix}`)
  },

  getPart(sku: string): Promise<Part> {
    return httpGet(`/api/parts/${encodeURIComponent(sku)}`)
  },

  createPart(req: CreatePartRequest): Promise<Part> {
    return httpPost('/api/parts', req)
  },

  updatePart(sku: string, req: UpdatePartRequest): Promise<Part> {
    return httpPut(`/api/parts/${encodeURIComponent(sku)}`, req)
  },

  async deletePart(sku: string): Promise<void> {
    await httpDelete(`/api/parts/${encodeURIComponent(sku)}`)
  },
}
