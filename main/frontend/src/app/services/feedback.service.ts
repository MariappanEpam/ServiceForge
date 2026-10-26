import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  FeedbackFormErrors,
  FeedbackFormModel,
  FeedbackResponse,
  FeedbackSummary,
  SubmitFeedbackRequest
} from '../models/feedback.model';

export const MAX_COMMENT_LENGTH = 500;

@Injectable({ providedIn: 'root' })
export class FeedbackService {

  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  // Callers must have verified consent (validate() requires it) before calling this: no request without consent.
  submitFeedback(request: SubmitFeedbackRequest): Observable<FeedbackResponse> {
    return this.http.post<FeedbackResponse>(`${this.baseUrl}/feedback`, request);
  }

  getTechnicianSummary(technicianId: number): Observable<FeedbackSummary> {
    return this.http.get<FeedbackSummary>(`${this.baseUrl}/technicians/${technicianId}/feedback-summary`);
  }

  // Pure validation, no I/O.
  validate(form: FeedbackFormModel): FeedbackFormErrors {
    const errors: FeedbackFormErrors = {};

    const jobId = form.jobIdInput.trim();
    if (!/^[1-9][0-9]*$/.test(jobId) || !Number.isSafeInteger(Number(jobId))) {
      errors.jobId = 'Please enter a valid Job ID';
    }

    if (!Number.isInteger(form.rating) || form.rating < 1 || form.rating > 5) {
      errors.rating = 'Please select a rating';
    }

    if (form.comment.trim().length > MAX_COMMENT_LENGTH) {
      errors.comment = `Comment must be ${MAX_COMMENT_LENGTH} characters or fewer`;
    }

    if (!form.consent) {
      errors.consent = 'Please confirm that you consent to sharing this feedback';
    }

    return errors;
  }

  // Pure mapping from the form to the request payload: only jobId, rating, comment (when not blank) and consent.
  toRequest(form: FeedbackFormModel): SubmitFeedbackRequest {
    const request: SubmitFeedbackRequest = {
      jobId: Number(form.jobIdInput.trim()),
      rating: form.rating,
      consent: true
    };
    const comment = form.comment.trim();
    if (comment.length > 0) {
      request.comment = comment;
    }
    return request;
  }

  // Maps an HTTP failure to user text. Never echoes the comment or raw server messages.
  describeError(err: unknown): string {
    const status = (err as { status?: number } | null)?.status;
    const code = (err as { error?: { code?: string } } | null)?.error?.code;

    if (status === 409) {
      return 'Feedback already submitted for this job';
    }
    if (status === 404) {
      return 'Job not found';
    }
    if (status === 400 && code === 'JOB_NOT_COMPLETED') {
      return 'Feedback can only be given for completed jobs';
    }
    if (status === 400) {
      return 'Please check the form and try again';
    }
    return 'Could not submit feedback. Please try again later.';
  }
}
