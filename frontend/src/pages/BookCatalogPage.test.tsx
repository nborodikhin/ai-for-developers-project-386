import { screen, waitFor } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { server } from '../test/mocks/server';
import { renderWithProviders } from '../test/render';
import { BookCatalogPage } from './BookCatalogPage';

describe('BookCatalogPage', () => {
  it('shows event type cards after data loads', async () => {
    renderWithProviders(<BookCatalogPage />);
    await screen.findByText('Quick Call');
    expect(screen.getByText('Deep Dive')).toBeInTheDocument();
  });

  it('shows error alert when API fails', async () => {
    server.use(
      http.get('/api/event-types', () =>
        HttpResponse.json({ message: 'Server error' }, { status: 500 })),
    );
    renderWithProviders(<BookCatalogPage />);
    await waitFor(() =>
      expect(screen.getByRole('alert')).toBeInTheDocument(),
    );
  });

  it('renders page heading', () => {
    renderWithProviders(<BookCatalogPage />);
    expect(screen.getByRole('heading', { name: /Выберите тип события/i })).toBeInTheDocument();
  });
});
