import { routes } from './app.routes';

describe('app routes', () => {
  it('restricts the approvals page to approver roles', () => {
    const approvalsRoute = routes[2].children?.find((route) => route.path === 'approvals');

    expect(approvalsRoute?.data?.['roles']).toEqual(['QUALITY_REVIEWER', 'FINANCE', 'SYSTEM_ADMINISTRATOR']);
  });
});
