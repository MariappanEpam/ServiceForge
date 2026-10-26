import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { FeedbackComponent } from './feedback.component';
import { StarRatingComponent } from '../star-rating/star-rating.component';
import { environment } from '../../environments/environment';

describe('FeedbackComponent', () => {
  let fixture: ComponentFixture<FeedbackComponent>;
  let component: FeedbackComponent;
  let http: HttpTestingController;
  const url = `${environment.apiBaseUrl}/feedback`;

  const fillValid = (): void => {
    component.form = { jobIdInput: '4', rating: 5, comment: 'Great work', consent: true };
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [FeedbackComponent, StarRatingComponent],
      imports: [FormsModule, HttpClientTestingModule]
    });
    fixture = TestBed.createComponent(FeedbackComponent);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  it('renders the form fields in order with a consent notice', () => {
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('#feedback-job-id')).not.toBeNull();
    expect(el.querySelector('app-star-rating')).not.toBeNull();
    expect(el.querySelector('#feedback-comment')).not.toBeNull();
    expect(el.querySelector('input[type="checkbox"]')).not.toBeNull();
    expect(el.querySelector('.notice')?.textContent).toContain('personal data');
    expect(el.querySelector('button[type="submit"]')).not.toBeNull();
  });

  it('shows inline errors and sends no request while the form is invalid', () => {
    component.submit();
    fixture.detectChanges();
    http.expectNone(url);
    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Please enter a valid Job ID');
    expect(text).toContain('Please select a rating');
    expect(text).toContain('consent');
  });

  it('sends no request without consent even when everything else is valid', () => {
    fillValid();
    component.form.consent = false;
    component.submit();
    http.expectNone(url);
    expect(component.errors.consent).toBeDefined();
  });

  it('submits a valid form, shows a success message and resets the form', () => {
    fillValid();
    component.submit();
    expect(component.submitting).toBeTrue();
    const req = http.expectOne(url);
    expect(req.request.body).toEqual({ jobId: 4, rating: 5, comment: 'Great work', consent: true });
    req.flush({ id: 1, jobId: 4, rating: 5, comment: 'Great work', createdAt: '2026-01-01T00:00:00Z' });
    fixture.detectChanges();
    expect(component.submitting).toBeFalse();
    expect(component.successMessage).toBe('Thank you for your feedback!');
    expect(component.form).toEqual({ jobIdInput: '', rating: 0, comment: '', consent: false });
    expect(fixture.nativeElement.querySelector('.success')?.textContent).toContain('Thank you');
  });

  it('keeps the entered values and shows a message when the request fails', () => {
    fillValid();
    component.submit();
    http.expectOne(url).flush({ code: 'FEEDBACK_ALREADY_EXISTS' }, { status: 409, statusText: 'Conflict' });
    fixture.detectChanges();
    expect(component.errorMessage).toBe('Feedback already submitted for this job');
    expect(component.form).toEqual({ jobIdInput: '4', rating: 5, comment: 'Great work', consent: true });
    expect(component.successMessage).toBe('');
    expect(component.submitting).toBeFalse();
    expect(fixture.nativeElement.querySelector('.error')?.textContent).toContain('already submitted');
  });

  it('shows a generic message on a network failure', () => {
    fillValid();
    component.submit();
    http.expectOne(url).error(new ProgressEvent('error'));
    expect(component.errorMessage).toContain('Could not submit feedback');
    expect(component.form.jobIdInput).toBe('4');
  });

  it('sends only one request when submitted twice quickly', () => {
    fillValid();
    component.submit();
    component.submit();
    const requests = http.match(url);
    expect(requests.length).toBe(1);
    requests[0].flush({ id: 1, jobId: 4, rating: 5, comment: null, createdAt: '2026-01-01T00:00:00Z' });
  });

  it('disables the submit button while submitting and when the comment is too long', () => {
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('button[type="submit"]');
    expect(button.disabled).toBeFalse();
    component.form.comment = 'a'.repeat(501);
    fixture.detectChanges();
    expect(button.disabled).toBeTrue();
    expect(fixture.nativeElement.querySelector('.counter')?.textContent).toContain('501 / 500');
    fillValid();
    component.submit();
    fixture.detectChanges();
    expect(button.disabled).toBeTrue();
    http.expectOne(url).flush({});
  });

  it('renders markup in the comment as text, never as HTML', () => {
    fillValid();
    component.form.comment = '<img src=x onerror=alert(1)>';
    fixture.detectChanges();
    component.submit();
    http.expectOne(url).flush({}, { status: 400, statusText: 'Bad Request' });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('img')).toBeNull();
  });

  it('wires the star rating into the form model', () => {
    const star: HTMLElement = fixture.nativeElement.querySelectorAll('.star')[3];
    star.click();
    fixture.detectChanges();
    expect(component.form.rating).toBe(4);
  });

  it('submits through the form element', () => {
    fillValid();
    fixture.detectChanges();
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));
    expect(component.submitting).toBeTrue();
    http.expectOne(url).flush({});
  });
});
