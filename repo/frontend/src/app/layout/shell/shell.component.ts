import { Component } from '@angular/core';
import { AsyncPipe, CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { BreakpointObserver } from '@angular/cdk/layout';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { NavItemModel } from '../../core/models/nav-item.model';
import { LoadingIndicatorComponent } from '../../shared/components/loading-indicator/loading-indicator.component';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [
    CommonModule,
    AsyncPipe,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatCardModule,
    MatDividerModule,
    LoadingIndicatorComponent
  ],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss'
})
export class ShellComponent {
  readonly navItems: NavItemModel[] = [
    { label: 'Dashboard', icon: 'dashboard', route: '/dashboard', roles: ['BUYER', 'FULFILLMENT_CLERK', 'QUALITY_REVIEWER', 'FINANCE', 'SYSTEM_ADMINISTRATOR'] },
    { label: 'Orders', icon: 'orders', route: '/orders', roles: ['BUYER', 'SYSTEM_ADMINISTRATOR'] },
    { label: 'Order Review', icon: 'review', route: '/orders/review', roles: ['QUALITY_REVIEWER', 'SYSTEM_ADMINISTRATOR'] },
    { label: 'Payments', icon: 'payments', route: '/orders/finance', roles: ['FINANCE', 'SYSTEM_ADMINISTRATOR'] },
    { label: 'Fulfillment', icon: 'fulfillment', route: '/fulfillment', roles: ['FULFILLMENT_CLERK', 'SYSTEM_ADMINISTRATOR'] },
    { label: 'Receipts', icon: 'receipts', route: '/orders/receipts', roles: ['BUYER', 'SYSTEM_ADMINISTRATOR'] },
    { label: 'Returns', icon: 'returns', route: '/orders/returns', roles: ['BUYER', 'SYSTEM_ADMINISTRATOR'] },
    { label: 'Document Center', icon: 'documents', route: '/document-center', roles: ['BUYER', 'QUALITY_REVIEWER', 'SYSTEM_ADMINISTRATOR'] },
    { label: 'Check-ins', icon: 'checkins', route: '/check-ins', roles: ['BUYER', 'SYSTEM_ADMINISTRATOR'] },
    { label: 'Approvals', icon: 'approvals', route: '/approvals', roles: ['QUALITY_REVIEWER', 'FINANCE', 'SYSTEM_ADMINISTRATOR'] },
    { label: 'Admin', icon: 'admin', route: '/admin', roles: ['SYSTEM_ADMINISTRATOR'] }
  ];

  readonly user$ = this.authService.user$;
  isCompact = false;
  drawerOpened = true;

  constructor(
    private readonly router: Router,
    private readonly authService: AuthService,
    private readonly breakpointObserver: BreakpointObserver
  ) {
    this.breakpointObserver.observe('(max-width: 1100px)').subscribe((state) => {
      this.isCompact = state.matches;
      this.drawerOpened = !state.matches;
    });
  }

  visibleNavItems(userRole: string | null | undefined): NavItemModel[] {
    if (!userRole) {
      return [];
    }
    return this.navItems.filter((item) => item.roles.includes(userRole));
  }

  operationItems(userRole: string | null | undefined): NavItemModel[] {
    return this.visibleNavItems(userRole).filter((item) => ['Dashboard', 'Orders', 'Order Review', 'Payments', 'Fulfillment', 'Receipts', 'Returns'].includes(item.label));
  }

  governanceItems(userRole: string | null | undefined): NavItemModel[] {
    return this.visibleNavItems(userRole).filter((item) => !['Dashboard', 'Orders', 'Order Review', 'Payments', 'Fulfillment', 'Receipts', 'Returns'].includes(item.label));
  }

  toggleDrawer(): void {
    this.drawerOpened = !this.drawerOpened;
  }

  closeDrawerOnSmallScreens(): void {
    if (this.isCompact) {
      this.drawerOpened = false;
    }
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/login'])
    });
  }
}
