import { renderHook, waitFor } from '@testing-library/react';
import { useBookings } from './useBookings';

describe('useBookings', () => {
  it('fetches all bookings', async () => {
    const { result } = renderHook(() => useBookings());
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.data).toHaveLength(2);
  });

  it('filters by email when provided', async () => {
    const { result } = renderHook(() => useBookings('alice@test.com'));
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.data).toHaveLength(1);
    expect(result.current.data[0].guestEmail).toBe('alice@test.com');
  });
});
