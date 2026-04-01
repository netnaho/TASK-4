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

test('buyer creates check-in and document draft, reviewer approves the document', async ({ page }) => {
  const draftTitle = `Regulated draft ${Date.now()}`;
  const checkInComment = `Dock check ${Date.now()}`;

  await login(page, 'buyer1', 'PortalAccess2026!');

  await page.getByRole('link', { name: 'Check-ins' }).click();
  await page.getByLabel('Comment').fill(checkInComment);
  await page.getByRole('button', { name: 'Create check-in' }).click();
  await expect(page.getByText('Buyer One').first()).toBeVisible();
  await expect(page.getByText(checkInComment).last()).toBeVisible();

  await page.getByRole('link', { name: 'Document Center' }).click();
  await page.getByRole('combobox', { name: 'Document type' }).click({ force: true });
  await page.getByRole('option').first().click();
  await page.getByLabel('Title').fill(draftTitle);
  await page.getByLabel('Draft content').fill('Controlled draft content for approval flow verification.');
  await page.getByRole('button', { name: 'Create draft' }).click();
  await expect(page.locator('.document-grid').getByText(draftTitle).first()).toBeVisible();
  await page.getByRole('button', { name: 'Submit for approval' }).click();
  await logout(page);

  await login(page, 'quality1', 'PortalAccess2026!');
  await page.getByRole('link', { name: 'Document Center' }).click();
  await page.locator('.approval-item', { hasText: draftTitle }).getByRole('button', { name: 'Approve' }).click();
  await page.locator('.stack-item', { hasText: draftTitle }).first().click();
  await expect(page.locator('.detail-panel .status-pill', { hasText: 'APPROVED' })).toBeVisible();
});
