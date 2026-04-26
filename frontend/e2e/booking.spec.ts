import { test, expect } from '@playwright/test';

test.describe('Booking flow', () => {
  test.beforeAll(async ({ request }) => {
    await request.post('/api/event-types', {
      data: { name: 'E2E Call', description: 'Playwright test event', durationMinutes: 30 },
    });
  });

  test('home page renders with CTA', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByRole('heading', { name: 'Calendar' })).toBeVisible();
    await expect(page.getByRole('button', { name: /Записаться/i })).toBeVisible();
  });

  test('can complete a booking end-to-end', async ({ page }) => {
    await page.goto('/book');

    // Select the event type
    await page.getByText('E2E Call').first().click();

    // Click the first available (non-disabled, non-outside) day in the calendar
    await page
      .locator('table button:not([data-disabled]):not([data-outside])')
      .first()
      .click();

    // Wait for slots to load and click the first available one
    await expect(page.getByText('Свободен').first()).toBeVisible();
    await page.getByRole('button').filter({ hasText: 'Свободен' }).first().click();

    // Fill the booking form
    await page.getByRole('textbox', { name: /Имя/i }).fill('Playwright User');
    await page.getByRole('textbox', { name: /Email/i }).fill('pw@test.com');
    await page.getByRole('button', { name: 'Записаться', exact: true }).click();

    // Success notification
    await expect(page.getByText(/забронирована/i)).toBeVisible();
  });

  test('booked slot is shown as unavailable', async ({ page }) => {
    // Re-open the same event type and pick the same (first available) date
    await page.goto('/book');
    await page.getByText('E2E Call').first().click();
    await page
      .locator('table button:not([data-disabled]):not([data-outside])')
      .first()
      .click();

    // The slot booked in the previous test shows as "Занято"
    await expect(page.getByText('Занято').first()).toBeVisible({ timeout: 10_000 });
  });
});
