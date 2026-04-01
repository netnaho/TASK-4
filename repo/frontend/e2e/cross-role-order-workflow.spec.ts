import { expect, test } from '@playwright/test';

async function login(page: import('@playwright/test').Page, username: string, password: string) {
  await page.goto('/login');
  await page.getByLabel('Username').fill(username);
  await page.getByLabel('Password').fill(password);
  await page.getByRole('button', { name: 'Sign in' }).click();
  await expect(page).toHaveURL(/dashboard/);
}

async function logout(page: import('@playwright/test').Page) {
  const csrfResponse = await page.context().request.get('/api/auth/csrf');
  expect(csrfResponse.ok()).toBeTruthy();
  const csrf = await csrfResponse.json();
  const response = await page.context().request.post('/api/auth/logout', {
    headers: { 'X-XSRF-TOKEN': csrf.token },
    data: {}
  });
  expect(response.ok()).toBeTruthy();
  await page.goto('/login');
  await expect(page).toHaveURL(/login/);
}

async function apiGet(page: import('@playwright/test').Page, path: string) {
  const response = await page.context().request.get(path);
  expect(response.ok()).toBeTruthy();
  return response.json();
}

async function apiPost(page: import('@playwright/test').Page, path: string, data: unknown) {
  const csrfResponse = await page.context().request.get('/api/auth/csrf');
  expect(csrfResponse.ok()).toBeTruthy();
  const csrf = await csrfResponse.json();
  const response = await page.context().request.post(path, {
    headers: { 'X-XSRF-TOKEN': csrf.token },
    data
  });
  expect(response.ok()).toBeTruthy();
  return response.json();
}

test('procurement -> fulfillment -> receipt cross-role workflow', async ({ page }) => {
  await login(page, 'buyer1', 'PortalAccess2026!');

  const createdOrder = await apiPost(page, '/api/orders', { notes: 'Playwright seeded draft', items: [{ productId: 1, quantity: 2 }] });
  const orderId = createdOrder.id as number;
  const orderNumber = createdOrder.orderNumber as string;
  await apiPost(page, `/api/orders/${orderId}/submit-review`, {});
  await logout(page);

  await login(page, 'quality1', 'PortalAccess2026!');
  await page.getByRole('link', { name: 'Order Review' }).click();
  await page.locator('.queue-item', { hasText: orderNumber }).getByRole('button', { name: 'Approve order' }).click();
  await logout(page);

  await login(page, 'finance1', 'PortalAccess2026!');
  await page.getByRole('link', { name: 'Payments' }).click();
  await page.locator('.queue-item', { hasText: orderNumber }).getByRole('button', { name: 'Record payment' }).click();
  await logout(page);

  await login(page, 'fulfillment1', 'PortalAccess2026!');
  await page.getByRole('link', { name: 'Fulfillment' }).click();
  await page.locator('.queue-item', { hasText: orderNumber }).getByRole('button', { name: 'Pick/pack' }).click();
  const orderDetail = await apiGet(page, `/api/orders/${orderId}`);
  const orderItemId = orderDetail.items[0].id;
  await apiPost(page, `/api/orders/${orderId}/shipments`, { notes: 'Wave 1', items: [{ orderItemId, quantity: 1 }] });
  await page.reload();
  await page.locator('.queue-item', { hasText: orderNumber }).getByRole('button', { name: 'Open' }).click();
  await expect(page.getByRole('heading', { name: 'Shipment 2' })).toBeVisible();
  await expect(page.locator('.detail-card').getByText('1 shipment(s) already dispatched')).toBeVisible();
  await apiPost(page, `/api/orders/${orderId}/shipments`, { notes: 'Wave 2', items: [{ orderItemId, quantity: 1 }] });
  await logout(page);

  await login(page, 'buyer1', 'PortalAccess2026!');
  await page.getByRole('link', { name: 'Receipts' }).click();
  await page.locator('.queue-item', { hasText: orderNumber }).getByRole('button', { name: 'Open' }).click();
  await page.locator('.detail-card').getByRole('spinbutton', { name: 'Received quantity' }).fill('2');
  await page.getByRole('button', { name: 'Record partial receipt' }).click();

  await page.getByRole('link', { name: 'Orders' }).click();
  await page.locator('table tr[mat-row]', { hasText: orderNumber }).first().click();
  await expect(page.getByText('ORDER RECEIVED')).toBeVisible();
});
