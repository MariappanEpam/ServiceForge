export interface Technician {
  id: number;
  name: string;
  region: string;
  status?: 'INVITED' | 'ACTIVE' | 'SUSPENDED' | 'OFFBOARDED';
  trade?: string;
  email?: string;
  phone?: string;
  preferredWorkingHours?: string;
  emergencyContact?: string;
  onboardingCompletedAt?: string;
}

export interface CreateTechnicianRequest {
  name: string;
  trade: string;
  region: string;
  email?: string;
  phone?: string;
}

export interface UpdateTechnicianRequest {
  name: string;
  trade: string;
  region: string;
  email?: string;
  phone?: string;
}

export interface ActivateTechnicianRequest {
  token: string;
  preferredWorkingHours?: string;
  emergencyContact?: string;
}

export interface TechnicianInviteResponse {
  technician: Technician;
  invitationToken: string;
}
