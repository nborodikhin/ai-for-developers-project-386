import { test, expect } from '@playwright/test';

test.describe('Admin', () => {
  test('can create a new event type', async ({ page }) => {
    await page.goto('/admin');

    await page.getByRole('button', { name: /Добавить/i }).click();

    const modal = page.getByRole('dialog');
    await modal.getByRole('textbox', { name: /Название/i }).fill('Admin E2E Type');
    await modal.getByRole('textbox', { name: /Описание/i }).fill('Created via Playwright');
    await modal.getByRole('button', { name: 'Сохранить', exact: true }).click();

    await expect(page.getByRole('cell', { name: 'Admin E2E Type' })).toBeVisible();
  });

  test('can delete an event type', async ({ page }) => {
    // Create one to delete
    await page.request.post('/api/event-types', {
      data: { name: 'To Delete', description: 'Will be deleted', durationMinutes: 15 },
    });

    await page.goto('/admin');
    await expect(page.getByRole('cell', { name: 'To Delete' })).toBeVisible();

    page.once('dialog', (dialog) => dialog.accept());
    await page
      .getByRole('row', { name: /To Delete/ })
      .getByRole('button')
      .last()
      .click();

    await expect(page.getByRole('cell', { name: 'To Delete' })).not.toBeVisible({
      timeout: 5_000,
    });
  });

  test('bookings tab shows the bookings table', async ({ page }) => {
    await page.goto('/admin');
    await page.getByRole('tab', { name: /Записи/i }).click();
    await expect(page.getByRole('columnheader', { name: /Гость/i })).toBeVisible();
  });
});
