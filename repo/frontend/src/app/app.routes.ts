import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { ShellComponent } from './layout/shell/shell.component';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./features/auth/login/login.component').then((m) => m.LoginComponent) },
  { path: 'unauthorized', loadComponent: () => import('./features/auth/unauthorized/unauthorized.component').then((m) => m.UnauthorizedComponent) },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', loadComponent: () => import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent) },
      { path: 'orders', loadComponent: () => import('./features/orders/order-workspace/order-workspace.component').then((m) => m.OrderWorkspaceComponent), canActivate: [roleGuard], data: { roles: ['BUYER', 'SYSTEM_ADMINISTRATOR'] } },
      { path: 'orders/review', loadComponent: () => import('./features/orders/review/review-orders.component').then((m) => m.ReviewOrdersComponent), canActivate: [roleGuard], data: { roles: ['QUALITY_REVIEWER', 'SYSTEM_ADMINISTRATOR'] } },
      { path: 'orders/finance', loadComponent: () => import('./features/orders/finance/finance-orders.component').then((m) => m.FinanceOrdersComponent), canActivate: [roleGuard], data: { roles: ['FINANCE', 'SYSTEM_ADMINISTRATOR'] } },
      { path: 'fulfillment', loadComponent: () => import('./features/orders/fulfillment/fulfillment-orders.component').then((m) => m.FulfillmentOrdersComponent), canActivate: [roleGuard], data: { roles: ['FULFILLMENT_CLERK', 'SYSTEM_ADMINISTRATOR'] } },
      { path: 'orders/receipts', loadComponent: () => import('./features/orders/receipts/receipts.component').then((m) => m.ReceiptsComponent), canActivate: [roleGuard], data: { roles: ['BUYER', 'SYSTEM_ADMINISTRATOR'] } },
      { path: 'orders/returns', loadComponent: () => import('./features/orders/returns/returns.component').then((m) => m.ReturnsComponent), canActivate: [roleGuard], data: { roles: ['BUYER', 'SYSTEM_ADMINISTRATOR'] } },
      { path: 'orders/:id', loadComponent: () => import('./features/orders/order-detail/order-detail.component').then((m) => m.OrderDetailComponent), canActivate: [roleGuard], data: { roles: ['BUYER', 'FULFILLMENT_CLERK', 'QUALITY_REVIEWER', 'FINANCE', 'SYSTEM_ADMINISTRATOR'] } },
      { path: 'document-center', loadComponent: () => import('./features/documents/document-center/document-center.component').then((m) => m.DocumentCenterComponent), canActivate: [roleGuard], data: { roles: ['BUYER', 'QUALITY_REVIEWER', 'SYSTEM_ADMINISTRATOR'] } },
      { path: 'check-ins', loadComponent: () => import('./features/check-ins/check-ins-page/check-ins-page.component').then((m) => m.CheckInsPageComponent), canActivate: [roleGuard], data: { roles: ['BUYER', 'SYSTEM_ADMINISTRATOR'] } },
      { path: 'approvals', loadComponent: () => import('./features/approvals/approvals-page/approvals-page.component').then((m) => m.ApprovalsPageComponent), canActivate: [roleGuard], data: { roles: ['QUALITY_REVIEWER', 'FINANCE', 'SYSTEM_ADMINISTRATOR'] } },
      { path: 'admin', loadComponent: () => import('./features/admin/admin-page/admin-page.component').then((m) => m.AdminPageComponent), canActivate: [roleGuard], data: { roles: ['SYSTEM_ADMINISTRATOR'] } }
    ]
  },
  { path: '**', redirectTo: '' }
];
