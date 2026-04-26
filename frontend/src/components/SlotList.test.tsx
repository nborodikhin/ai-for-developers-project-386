import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithProviders } from '../test/render';
import { SlotList } from './SlotList';

const availableSlot = {
  startTime: '2026-05-15T09:00:00Z',
  endTime: '2026-05-15T09:30:00Z',
  available: true,
};

const bookedSlot = {
  startTime: '2026-05-15T09:30:00Z',
  endTime: '2026-05-15T10:00:00Z',
  available: false,
};

describe('SlotList', () => {
  it('shows loader when loading', () => {
    renderWithProviders(
      <SlotList slots={[]} loading={true} selectedSlot={null} onSelect={vi.fn()} userTimezone="UTC" />,
    );
    expect(document.querySelector('.mantine-Loader-root')).not.toBeNull();
  });

  it('shows empty message when no slots', () => {
    renderWithProviders(
      <SlotList slots={[]} loading={false} selectedSlot={null} onSelect={vi.fn()} userTimezone="UTC" />,
    );
    expect(screen.getByText(/Нет доступных слотов/i)).toBeInTheDocument();
  });

  it('shows available slot as clickable button and calls onSelect', async () => {
    const onSelect = vi.fn();
    renderWithProviders(
      <SlotList slots={[availableSlot]} loading={false} selectedSlot={null} onSelect={onSelect} userTimezone="UTC" />,
    );
    expect(screen.getByText(/Свободен/i)).toBeInTheDocument();
    // Click the only button (the available slot); time is formatted in the given TZ so we avoid asserting exact time
    await userEvent.click(screen.getByRole('button'));
    expect(onSelect).toHaveBeenCalledWith(availableSlot);
  });

  it('shows booked slot as "Занято" without button', () => {
    renderWithProviders(
      <SlotList slots={[bookedSlot]} loading={false} selectedSlot={null} onSelect={vi.fn()} userTimezone="UTC" />,
    );
    expect(screen.getByText(/Занято/i)).toBeInTheDocument();
    expect(screen.queryByRole('button')).toBeNull();
  });
});
