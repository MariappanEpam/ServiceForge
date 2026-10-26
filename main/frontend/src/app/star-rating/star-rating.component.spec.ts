import { ComponentFixture, TestBed } from '@angular/core/testing';
import { StarRatingComponent } from './star-rating.component';

describe('StarRatingComponent', () => {
  let fixture: ComponentFixture<StarRatingComponent>;
  let component: StarRatingComponent;
  let emitted: number[];

  const stars = (): HTMLElement[] => Array.from(fixture.nativeElement.querySelectorAll('.star'));
  const filledCount = (): number => stars().filter((s) => s.classList.contains('filled')).length;
  const press = (key: string): KeyboardEvent => {
    const event = new KeyboardEvent('keydown', { key, bubbles: true, cancelable: true });
    fixture.nativeElement.querySelector('[role="radiogroup"]').dispatchEvent(event);
    fixture.detectChanges();
    return event;
  };

  beforeEach(() => {
    TestBed.configureTestingModule({ declarations: [StarRatingComponent] });
    fixture = TestBed.createComponent(StarRatingComponent);
    component = fixture.componentInstance;
    emitted = [];
    component.valueChange.subscribe((v) => emitted.push(v));
    fixture.detectChanges();
  });

  it('renders five unselected stars in a radiogroup', () => {
    expect(stars().length).toBe(5);
    expect(filledCount()).toBe(0);
    expect(fixture.nativeElement.querySelector('[role="radiogroup"]')).not.toBeNull();
    expect(stars().every((s) => s.getAttribute('role') === 'radio')).toBeTrue();
  });

  it('clicking star n fills 1..n and emits n', () => {
    stars()[2].click();
    fixture.detectChanges();
    expect(filledCount()).toBe(3);
    expect(emitted).toEqual([3]);
    expect(stars()[2].getAttribute('aria-checked')).toBe('true');
    expect(stars()[1].getAttribute('aria-checked')).toBe('false');
  });

  it('changes the selection when another star is clicked', () => {
    stars()[4].click();
    stars()[1].click();
    fixture.detectChanges();
    expect(filledCount()).toBe(2);
    expect(emitted).toEqual([5, 2]);
  });

  it('labels each star "n out of 5" and the group', () => {
    expect(stars().map((s) => s.getAttribute('aria-label'))).toEqual(['1 out of 5', '2 out of 5', '3 out of 5', '4 out of 5', '5 out of 5']);
    component.label = 'Service rating';
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role="radiogroup"]').getAttribute('aria-label')).toBe('Service rating');
  });

  it('announces the selection in the live region', () => {
    stars()[3].click();
    fixture.detectChanges();
    const live = fixture.nativeElement.querySelector('[aria-live="polite"]');
    expect(live.textContent.trim()).toBe('4 out of 5');
  });

  it('arrow keys move the selection and clamp to 1..5', () => {
    press('ArrowRight');
    expect(component.value).toBe(1);
    press('ArrowRight');
    press('ArrowUp');
    expect(component.value).toBe(3);
    press('ArrowLeft');
    press('ArrowDown');
    expect(component.value).toBe(1);
    press('ArrowLeft');
    expect(component.value).toBe(1);
    press('End');
    expect(component.value).toBe(5);
    press('ArrowRight');
    expect(component.value).toBe(5);
    press('Home');
    expect(component.value).toBe(1);
  });

  it('prevents default for handled keys and ignores other keys', () => {
    expect(press('ArrowRight').defaultPrevented).toBeTrue();
    const before = emitted.length;
    expect(press('a').defaultPrevented).toBeFalse();
    expect(emitted.length).toBe(before);
  });

  it('uses a roving tabindex on the selected star', () => {
    expect(stars().map((s) => s.getAttribute('tabindex'))).toEqual(['0', '-1', '-1', '-1', '-1']);
    stars()[3].click();
    fixture.detectChanges();
    expect(stars().map((s) => s.getAttribute('tabindex'))).toEqual(['-1', '-1', '-1', '0', '-1']);
  });

  it('hover previews without changing the value, and leaving restores it', () => {
    stars()[1].click();
    fixture.detectChanges();
    stars()[4].dispatchEvent(new Event('mouseenter'));
    fixture.detectChanges();
    expect(filledCount()).toBe(5);
    expect(component.value).toBe(2);
    fixture.nativeElement.querySelector('[role="radiogroup"]').dispatchEvent(new Event('mouseleave'));
    fixture.detectChanges();
    expect(filledCount()).toBe(2);
    expect(emitted).toEqual([2]);
  });

  describe('read-only', () => {
    beforeEach(() => {
      component.readonly = true;
      component.value = 4;
      fixture.detectChanges();
    });

    it('renders an image role with the value and does not react to clicks or keys', () => {
      const img = fixture.nativeElement.querySelector('[role="img"]');
      expect(img.getAttribute('aria-label')).toBe('4 out of 5');
      expect(fixture.nativeElement.querySelector('[role="radiogroup"]')).toBeNull();
      expect(filledCount()).toBe(4);
      component.select(1);
      component.onKeydown(new KeyboardEvent('keydown', { key: 'ArrowRight' }));
      expect(component.value).toBe(4);
      expect(emitted).toEqual([]);
    });

    it('rounds a fractional average for display and uses a custom label', () => {
      component.value = 4.5;
      component.label = 'Average rating 4.5 out of 5';
      fixture.detectChanges();
      expect(filledCount()).toBe(5);
      expect(fixture.nativeElement.querySelector('[role="img"]').getAttribute('aria-label')).toBe('Average rating 4.5 out of 5');
    });
  });
});
