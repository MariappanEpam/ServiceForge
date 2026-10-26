import { Component } from '@angular/core';
import { FeedbackService, MAX_COMMENT_LENGTH } from '../services/feedback.service';
import { FeedbackFormErrors, FeedbackFormModel } from '../models/feedback.model';

@Component({
  selector: 'app-feedback',
  templateUrl: './feedback.component.html',
  styleUrls: ['./feedback.component.css']
})
export class FeedbackComponent {

  readonly maxCommentLength = MAX_COMMENT_LENGTH;

  form: FeedbackFormModel = { jobIdInput: '', rating: 0, comment: '', consent: false };
  errors: FeedbackFormErrors = {};
  submitting = false;              // blocks double submit
  successMessage = '';
  errorMessage = '';

  constructor(private feedbackService: FeedbackService) {}

  get commentTooLong(): boolean {
    return this.form.comment.trim().length > this.maxCommentLength;
  }

  submit(): void {
    if (this.submitting) {
      return;
    }

    this.successMessage = '';
    this.errorMessage = '';
    this.errors = this.feedbackService.validate(this.form);
    if (Object.keys(this.errors).length > 0) {
      return; // no request while the form is invalid
    }

    // Consent: validate() above guarantees it is ticked; no request is made without it.
    this.submitting = true;
    this.feedbackService.submitFeedback(this.feedbackService.toRequest(this.form)).subscribe({
      next: () => {
        this.successMessage = 'Thank you for your feedback!';
        this.form = { jobIdInput: '', rating: 0, comment: '', consent: false };
        this.submitting = false;
      },
      error: (err) => {
        this.errorMessage = this.feedbackService.describeError(err);
        this.submitting = false;
      }
    });
  }
}
