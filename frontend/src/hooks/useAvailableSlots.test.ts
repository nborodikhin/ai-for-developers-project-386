import { renderHook, waitFor } from '@testing-library/react';
import { useAvailableSlots } from './useAvailableSlots';

describe('useAvailableSlots', () => {
  it('does not fetch when id or date is null', () => {
    const { result } = renderHook(() => useAvailableSlots(null, null));
    expect(result.current.loading).toBe(false);
    expect(result.current.data).toHaveLength(0);
  });

  it('fetches slots when id and date are provided', async () => {
    const { result } = renderHook(() => useAvailableSlots(1, '2026-05-15'));
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.data).toHaveLength(3);
    expect(result.current.data[0].available).toBe(true);
    expect(result.current.data[1].available).toBe(false);
  });
});
