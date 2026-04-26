import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithProviders } from '../test/render';
import { AdminPage } from './AdminPage';

vi.mock('@mantine/notifications', () => ({
  notifications: { show: vi.fn() },
}));

describe('AdminPage', () => {
  it('shows event types in the event types tab', async () => {
    renderWithProviders(<AdminPage />);
    // findAllByText tolerates the name appearing in both table row and potentially the modal portal
    await waitFor(() =>
      expect(screen.getAllByText('Quick Call').length).toBeGreaterThanOrEqual(1),
    );
    expect(screen.getAllByText('Deep Dive').length).toBeGreaterThanOrEqual(1);
  });

  it('shows bookings in the bookings tab', async () => {
    renderWithProviders(<AdminPage />);
    const bookingsTab = screen.getByRole('tab', { name: /Записи/i });
    await userEvent.click(bookingsTab);
    await screen.findByText('Alice');
    expect(screen.getByText('alice@test.com')).toBeInTheDocument();
    expect(screen.getByText('Bob')).toBeInTheDocument();
  });

  it('opens create modal when Add button is clicked', async () => {
    renderWithProviders(<AdminPage />);
    await screen.findByText('Quick Call');
    await userEvent.click(screen.getByRole('button', { name: /Добавить/i }));
    await waitFor(() =>
      expect(screen.getByText('Новый тип события')).toBeInTheDocument(),
    );
  });
});
