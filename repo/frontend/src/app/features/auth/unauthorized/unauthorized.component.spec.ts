import { TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { UnauthorizedComponent } from './unauthorized.component';

describe('UnauthorizedComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UnauthorizedComponent, NoopAnimationsModule],
      providers: [provideRouter([])]
    }).compileComponents();
  });

  it('renders without throwing and exposes a return-to-login affordance', () => {
    const fixture = TestBed.createComponent(UnauthorizedComponent);
    fixture.detectChanges();

    const link: HTMLAnchorElement | null = fixture.nativeElement.querySelector('a[href], a[routerLink], [routerlink]');
    expect(link).not.toBeNull();
  });
});
