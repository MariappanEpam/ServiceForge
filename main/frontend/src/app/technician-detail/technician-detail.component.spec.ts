import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { TechnicianDetailComponent } from './technician-detail.component';
import { StarRatingComponent } from '../star-rating/star-rating.component';
import { environment } from '../../environments/environment';

describe('TechnicianDetailComponent rating summary', () => {
  let fixture: ComponentFixture<TechnicianDetailComponent>;
  let component: TechnicianDetailComponent;
  let http: HttpTestingController;
  const api = environment.apiBaseUrl;
  const technician = { id: 3, name: 'Test Technician', region: 'East', status: 'ACTIVE' };

  const load = (): void => {
    component.technicianIdInput = '3';
    component.load();
    http.expectOne(`${api}/technicians/3`).flush(technician);
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TechnicianDetailComponent, StarRatingComponent],
      imports: [FormsModule, HttpClientTestingModule]
    });
    fixture = TestBed.createComponent(TechnicianDetailComponent);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  it('shows the customer rating with read-only stars', () => {
    load();
    http.expectOne(`${api}/technicians/3/feedback-summary`).flush({ technicianId: 3, averageRating: 4.0, feedbackCount: 2 });
    fixture.detectChanges();
    const text: string = fixture.nativeElement.querySelector('.rating-summary').textContent;
    expect(text).toContain('Customer rating: 4.0 / 5');
    expect(text).toContain('(2 ratings)');
    expect(fixture.nativeElement.querySelector('app-star-rating [role="img"]')).not.toBeNull();
  });

  it('uses the singular for one rating', () => {
    load();
    http.expectOne(`${api}/technicians/3/feedback-summary`).flush({ technicianId: 3, averageRating: 5.0, feedbackCount: 1 });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.rating-summary').textContent).toContain('(1 rating)');
  });

  it('shows "No ratings yet" when there is no feedback', () => {
    load();
    http.expectOne(`${api}/technicians/3/feedback-summary`).flush({ technicianId: 3, averageRating: null, feedbackCount: 0 });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.rating-summary').textContent).toContain('No ratings yet');
  });

  it('does not block the technician view when the summary call fails', () => {
    load();
    http.expectOne(`${api}/technicians/3/feedback-summary`).flush({}, { status: 500, statusText: 'Server Error' });
    fixture.detectChanges();
    expect(component.technician?.id).toBe(3);
    expect(component.ratingSummaryUnavailable).toBeTrue();
    expect(fixture.nativeElement.querySelector('.detail')).not.toBeNull();
    expect(fixture.nativeElement.textContent).toContain('Ratings unavailable');
    expect(fixture.nativeElement.textContent).toContain('Status:');
  });

  it('resets the summary at the start of each load and shows no summary for a failed technician load', () => {
    load();
    http.expectOne(`${api}/technicians/3/feedback-summary`).flush({ technicianId: 3, averageRating: 4.0, feedbackCount: 2 });
    component.technicianIdInput = '9';
    component.load();
    expect(component.ratingSummary).toBeNull();
    expect(component.ratingSummaryUnavailable).toBeFalse();
    http.expectOne(`${api}/technicians/9`).flush({ message: 'nope' }, { status: 404, statusText: 'Not Found' });
    expect(component.technician).toBeNull();
    http.expectNone(`${api}/technicians/9/feedback-summary`);
  });
});
