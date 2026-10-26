import { Component, ElementRef, EventEmitter, Input, Output, QueryList, ViewChildren } from '@angular/core';

@Component({
  selector: 'app-star-rating',
  templateUrl: './star-rating.component.html',
  styleUrls: ['./star-rating.component.css']
})
export class StarRatingComponent {

  readonly stars = [1, 2, 3, 4, 5];

  @Input() value = 0;            // 0..5
  @Input() readonly = false;
  @Input() label = '';           // accessible group label
  @Output() valueChange = new EventEmitter<number>();

  @ViewChildren('star') starElements?: QueryList<ElementRef<HTMLElement>>;

  hoverValue: number | null = null;   // preview only; leaving hover restores `value`
  liveMessage = '';

  select(n: number): void {
    if (this.readonly) {
      return;
    }
    const next = Math.min(5, Math.max(1, Math.round(n)));
    this.value = next;
    this.liveMessage = `${next} out of 5`;
    this.valueChange.emit(next);
  }

  onKeydown(event: KeyboardEvent): void {
    if (this.readonly) {
      return;
    }
    let target: number | null = null;
    switch (event.key) {
      case 'ArrowLeft':
      case 'ArrowDown':
        target = this.value - 1;
        break;
      case 'ArrowRight':
      case 'ArrowUp':
        target = this.value + 1;
        break;
      case 'Home':
        target = 1;
        break;
      case 'End':
        target = 5;
        break;
      default:
        return;
    }
    event.preventDefault();
    this.select(target);
    this.starElements?.get(this.value - 1)?.nativeElement.focus();
  }

  filled(n: number): boolean {
    const shown = this.hoverValue ?? this.value;
    return this.readonly ? n <= Math.round(this.value) : n <= shown;
  }

  tabIndexFor(n: number): number {
    const current = this.value >= 1 ? this.value : 1;
    return n === current ? 0 : -1;
  }
}
