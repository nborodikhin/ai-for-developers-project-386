import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { server } from '../test/mocks/server';
import { renderWithProviders } from '../test/render';
import { BookingForm } from './BookingForm';

vi.mock('@mantine/notifications', () => ({
  notifications: { show: vi.fn() },
}));

const mockSlot = {
  startTime: '2026-05-15T09:00:00Z',
  endTime: '2026-05-15T09:30:00Z',
  available: true,
};

describe('BookingForm', () => {
  it('renders name, email, and comment fields', () => {
    renderWithProviders(
      <BookingForm eventTypeId={1} slot={mockSlot} onSuccess={vi.fn()} onCancel={vi.fn()} />,
    );
    expect(screen.getByRole('textbox', { name: /Имя/i })).toBeInTheDocument();
    expect(screen.getByRole('textbox', { name: /Email/i })).toBeInTheDocument();
    expect(screen.getByRole('textbox', { name: /Комментарий/i })).toBeInTheDocument();
  });

  it('calls onSuccess after successful submission', async () => {
    const onSuccess = vi.fn();
    renderWithProviders(
      <BookingForm eventTypeId={1} slot={mockSlot} onSuccess={onSuccess} onCancel={vi.fn()} />,
    );
    await userEvent.type(screen.getByRole('textbox', { name: /Имя/i }), 'Alice');
    await userEvent.type(screen.getByRole('textbox', { name: /Email/i }), 'alice@test.com');
    await userEvent.click(screen.getByRole('button', { name: /Записаться/i }));
    await waitFor(() => expect(onSuccess).toHaveBeenCalled());
  });

  it('shows conflict error notification on 409', async () => {
    const { notifications } = await import('@mantine/notifications');
    server.use(
      http.post('/api/event-types/:id/bookings', () =>
        new HttpResponse(null, { status: 409 })),
    );
    renderWithProviders(
      <BookingForm eventTypeId={1} slot={mockSlot} onSuccess={vi.fn()} onCancel={vi.fn()} />,
    );
    await userEvent.type(screen.getByRole('textbox', { name: /Имя/i }), 'Alice');
    await userEvent.type(screen.getByRole('textbox', { name: /Email/i }), 'alice@test.com');
    await userEvent.click(screen.getByRole('button', { name: /Записаться/i }));
    await waitFor(() =>
      expect(vi.mocked(notifications.show)).toHaveBeenCalledWith(
        expect.objectContaining({ message: expect.stringContaining('уже занят') }),
      ),
    );
  });

  it('calls onCancel when back button is clicked', async () => {
    const onCancel = vi.fn();
    renderWithProviders(
      <BookingForm eventTypeId={1} slot={mockSlot} onSuccess={vi.fn()} onCancel={onCancel} />,
    );
    await userEvent.click(screen.getByRole('button', { name: /Назад/i }));
    expect(onCancel).toHaveBeenCalled();
  });
});
