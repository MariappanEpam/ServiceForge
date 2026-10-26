import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { FeedbackService } from './feedback.service';
import { FeedbackFormModel } from '../models/feedback.model';
import { environment } from '../../environments/environment';

describe('FeedbackService', () => {
  let service: FeedbackService;
  let http: HttpTestingController;

  const validForm = (): FeedbackFormModel => ({ jobIdInput: '4', rating: 5, comment: 'Great', consent: true });

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
    service = TestBed.inject(FeedbackService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  describe('validate', () => {
    it('returns no errors for a valid form', () => {
      expect(service.validate(validForm())).toEqual({});
    });

    it('rejects an empty, non numeric, decimal, zero, negative or padded job id', () => {
      for (const bad of ['', 'abc', '1.5', '0', '-3', '007', '1e3', ' ', '12abc']) {
        expect(service.validate({ ...validForm(), jobIdInput: bad }).jobId).toBe('Please enter a valid Job ID', `input "${bad}"`);
      }
    });

    it('accepts a job id with surrounding whitespace', () => {
      expect(service.validate({ ...validForm(), jobIdInput: ' 12 ' }).jobId).toBeUndefined();
    });

    it('rejects job ids beyond the safe integer range', () => {
      expect(service.validate({ ...validForm(), jobIdInput: '9007199254740993' }).jobId).toBe('Please enter a valid Job ID');
    });

    it('requires a rating between 1 and 5', () => {
      for (const bad of [0, 6, -1, 2.5, NaN]) {
        expect(service.validate({ ...validForm(), rating: bad }).rating).toBe('Please select a rating', `rating ${bad}`);
      }
      for (const ok of [1, 2, 3, 4, 5]) {
        expect(service.validate({ ...validForm(), rating: ok }).rating).toBeUndefined();
      }
    });

    it('limits the trimmed comment to 500 characters', () => {
      expect(service.validate({ ...validForm(), comment: 'a'.repeat(500) }).comment).toBeUndefined();
      expect(service.validate({ ...validForm(), comment: 'a'.repeat(501) }).comment).toBeDefined();
      expect(service.validate({ ...validForm(), comment: '  ' + 'a'.repeat(500) + '  ' }).comment).toBeUndefined();
    });

    it('requires consent', () => {
      expect(service.validate({ ...validForm(), consent: false }).consent).toBeDefined();
    });

    it('reports several errors at once', () => {
      const errors = service.validate({ jobIdInput: '', rating: 0, comment: '', consent: false });
      expect(Object.keys(errors).sort()).toEqual(['consent', 'jobId', 'rating']);
    });
  });

  describe('toRequest', () => {
    it('contains only jobId, rating, comment and consent', () => {
      const request = service.toRequest(validForm());
      expect(Object.keys(request).sort()).toEqual(['comment', 'consent', 'jobId', 'rating']);
      expect(request).toEqual({ jobId: 4, rating: 5, comment: 'Great', consent: true });
    });

    it('trims the comment and omits it when blank', () => {
      expect(service.toRequest({ ...validForm(), comment: '  padded  ' }).comment).toBe('padded');
      const blank = service.toRequest({ ...validForm(), comment: '   ' });
      expect('comment' in blank).toBeFalse();
    });
  });

  describe('HTTP contract', () => {
    it('posts feedback to /feedback', () => {
      const request = service.toRequest(validForm());
      let result: unknown;
      service.submitFeedback(request).subscribe((r) => result = r);
      const req = http.expectOne(`${environment.apiBaseUrl}/feedback`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush({ id: 1, jobId: 4, rating: 5, comment: 'Great', createdAt: '2026-01-01T00:00:00Z' });
      expect(result).toEqual({ id: 1, jobId: 4, rating: 5, comment: 'Great', createdAt: '2026-01-01T00:00:00Z' });
    });

    it('gets the technician summary', () => {
      let result: unknown;
      service.getTechnicianSummary(3).subscribe((r) => result = r);
      const req = http.expectOne(`${environment.apiBaseUrl}/technicians/3/feedback-summary`);
      expect(req.request.method).toBe('GET');
      req.flush({ technicianId: 3, averageRating: 4.0, feedbackCount: 2 });
      expect(result).toEqual({ technicianId: 3, averageRating: 4.0, feedbackCount: 2 });
    });
  });

  describe('describeError', () => {
    it('maps 409 to already submitted', () => {
      expect(service.describeError({ status: 409, error: { code: 'FEEDBACK_ALREADY_EXISTS' } })).toBe('Feedback already submitted for this job');
    });

    it('maps 404 to job not found', () => {
      expect(service.describeError({ status: 404 })).toBe('Job not found');
    });

    it('maps 400 JOB_NOT_COMPLETED', () => {
      expect(service.describeError({ status: 400, error: { code: 'JOB_NOT_COMPLETED' } })).toBe('Feedback can only be given for completed jobs');
    });

    it('maps other 400 errors to a generic form message', () => {
      expect(service.describeError({ status: 400, error: { code: 'VALIDATION_ERROR' } })).toBe('Please check the form and try again');
    });

    it('maps network and unknown errors to a generic message', () => {
      expect(service.describeError({ status: 0 })).toContain('Could not submit feedback');
      expect(service.describeError(null)).toContain('Could not submit feedback');
      expect(service.describeError(new Error('x'))).toContain('Could not submit feedback');
    });

    it('never echoes server messages or the comment', () => {
      const text = service.describeError({ status: 400, error: { code: 'VALIDATION_ERROR', message: 'SENTINEL-COMMENT' } });
      expect(text).not.toContain('SENTINEL-COMMENT');
    });
  });
});
