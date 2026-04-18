import { TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { DashboardComponent } from './dashboard.component';

describe('DashboardComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardComponent, NoopAnimationsModule]
    }).compileComponents();
  });

  it('exposes summary cards, queue items, and highlights', () => {
    const fixture = TestBed.createComponent(DashboardComponent);
    const component = fixture.componentInstance;

    expect(component.cards.length).toBe(4);
    expect(component.cards.map((c) => c.label)).toEqual([
      'Orders in Pipeline',
      'Documents in Review',
      'Field Check-ins Today',
      'Approvals Awaiting Action'
    ]);
    expect(component.queue.length).toBe(3);
    expect(component.highlights.length).toBe(2);
    expect(component.highlights[0].value).toBe('94%');
  });

  it('renders the card labels in the template', () => {
    const fixture = TestBed.createComponent(DashboardComponent);
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Orders in Pipeline');
    expect(text).toContain('Documents in Review');
    expect(text).toContain('Field Check-ins Today');
    expect(text).toContain('Approvals Awaiting Action');
  });
});
