// ETSTCC-13074: models for the client feedback form.
// PERSONAL-DATA: `comment` is free text and may contain personal data; consent must be explicitly ticked.

export interface SubmitFeedbackRequest {
  jobId: number;      // positive integer
  rating: number;     // integer 1..5
  comment?: string;   // optional, trimmed, max 500; omitted when blank
  consent: true;      // literal true; never sent as false
}

export interface FeedbackResponse {
  id: number;
  jobId: number;
  rating: number;
  comment: string | null;
  createdAt: string;  // UTC ISO-8601
}

export interface FeedbackSummary {
  technicianId: number;
  averageRating: number | null; // 1 decimal, null when feedbackCount === 0
  feedbackCount: number;
}

export interface FeedbackFormModel {
  jobIdInput: string;
  rating: number;     // 0 = not selected
  comment: string;
  consent: boolean;
}

export interface FeedbackFormErrors {
  jobId?: 'Please enter a valid Job ID';
  rating?: 'Please select a rating';
  comment?: string;   // length message
  consent?: string;   // consent-required message
}
