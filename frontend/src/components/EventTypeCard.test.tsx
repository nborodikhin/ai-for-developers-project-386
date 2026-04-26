import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithProviders } from '../test/render';
import { EventTypeCard } from './EventTypeCard';

const eventType = { id: 1, name: 'Quick Call', description: 'A short call', durationMinutes: 15 };

describe('EventTypeCard', () => {
  it('renders event name and duration', () => {
    renderWithProviders(<EventTypeCard eventType={eventType} />);
    expect(screen.getByText('Quick Call')).toBeInTheDocument();
    expect(screen.getByText(/15 мин/i)).toBeInTheDocument();
  });

  it('calls onClick when clicked', async () => {
    const onClick = vi.fn();
    renderWithProviders(<EventTypeCard eventType={eventType} onClick={onClick} />);
    await userEvent.click(screen.getByText('Quick Call'));
    expect(onClick).toHaveBeenCalled();
  });
});
