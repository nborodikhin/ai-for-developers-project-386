import { screen } from '@testing-library/react';
import { renderWithProviders } from '../test/render';
import { HomePage } from './HomePage';

describe('HomePage', () => {
  it('renders the Calendar title', () => {
    renderWithProviders(<HomePage />);
    expect(screen.getByRole('heading', { name: /Calendar/i })).toBeInTheDocument();
  });

  it('renders CTA button linking to /book flow', () => {
    renderWithProviders(<HomePage />);
    expect(screen.getByRole('button', { name: /Записаться/i })).toBeInTheDocument();
  });
});
