import { TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { BreakpointObserver } from '@angular/cdk/layout';
import { Router } from '@angular/router';
import { BehaviorSubject, Subject, of } from 'rxjs';
import { ShellComponent } from './shell.component';
import { AuthService } from '../../core/services/auth.service';

describe('ShellComponent', () => {
  function setup(userRole: string | null | undefined) {
    const user$ = new BehaviorSubject<any>(userRole ? { id: 1, username: 'u', displayName: 'User', role: userRole, permissions: [] } : userRole ?? null);
    const authService = {
      user$,
      logout: jasmine.createSpy('logout').and.returnValue(of({}))
    };
    const breakpointSubject = new Subject<any>();
    const breakpointObserver = { observe: () => breakpointSubject.asObservable() };
    const navigate = jasmine.createSpy('navigate');

    TestBed.configureTestingModule({
      imports: [ShellComponent, NoopAnimationsModule],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: BreakpointObserver, useValue: breakpointObserver },
        { provide: Router, useValue: { navigate, createUrlTree: () => ({}), serializeUrl: () => '', events: of() } }
      ]
    });
    return { authService, breakpointSubject, navigate };
  }

  it('filters navigation items by user role (BUYER sees only buyer-facing entries)', () => {
    setup('BUYER');
    const fixture = TestBed.createComponent(ShellComponent);
    const component = fixture.componentInstance;

    const visible = component.visibleNavItems('BUYER').map((i) => i.label);
    expect(visible).toContain('Dashboard');
    expect(visible).toContain('Orders');
    expect(visible).toContain('Receipts');
    expect(visible).not.toContain('Admin');
    expect(visible).not.toContain('Order Review');
  });

  it('returns an empty list when user role is missing', () => {
    setup(null);
    const fixture = TestBed.createComponent(ShellComponent);
    expect(fixture.componentInstance.visibleNavItems(null).length).toBe(0);
    expect(fixture.componentInstance.visibleNavItems(undefined).length).toBe(0);
  });

  it('splits operations and governance navigation groups for SYSTEM_ADMINISTRATOR', () => {
    setup('SYSTEM_ADMINISTRATOR');
    const fixture = TestBed.createComponent(ShellComponent);
    const component = fixture.componentInstance;

    const operations = component.operationItems('SYSTEM_ADMINISTRATOR').map((i) => i.label);
    const governance = component.governanceItems('SYSTEM_ADMINISTRATOR').map((i) => i.label);

    expect(operations).toEqual(jasmine.arrayContaining(['Dashboard', 'Orders', 'Receipts']));
    expect(governance).toEqual(jasmine.arrayContaining(['Document Center', 'Approvals', 'Admin']));
    expect(operations.some((l) => l === 'Admin')).toBeFalse();
  });

  it('reacts to compact breakpoint by collapsing the drawer', () => {
    const { breakpointSubject } = setup('BUYER');
    const fixture = TestBed.createComponent(ShellComponent);
    const component = fixture.componentInstance;

    breakpointSubject.next({ matches: true });
    expect(component.isCompact).toBeTrue();
    expect(component.drawerOpened).toBeFalse();

    component.toggleDrawer();
    expect(component.drawerOpened).toBeTrue();

    component.closeDrawerOnSmallScreens();
    expect(component.drawerOpened).toBeFalse();
  });

  it('delegates logout to auth service and navigates to /login', () => {
    const { authService, navigate } = setup('BUYER');
    const fixture = TestBed.createComponent(ShellComponent);
    const component = fixture.componentInstance;

    component.logout();
    expect(authService.logout).toHaveBeenCalled();
    expect(navigate).toHaveBeenCalledWith(['/login']);
  });
});
