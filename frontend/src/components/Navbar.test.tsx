import { screen } from '@testing-library/react';
import { renderWithProviders } from '../test/render';
import { Navbar } from './Navbar';

describe('Navbar', () => {
  it('has "Записаться" link pointing to /book', () => {
    renderWithProviders(<Navbar />);
    const link = screen.getByRole('link', { name: /Записаться/i });
    expect(link).toHaveAttribute('href', '/book');
  });

  it('has "Админка" link pointing to /admin', () => {
    renderWithProviders(<Navbar />);
    const link = screen.getByRole('link', { name: /Админка/i });
    expect(link).toHaveAttribute('href', '/admin');
  });
});
