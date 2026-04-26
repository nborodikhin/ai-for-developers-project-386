import { renderHook, waitFor } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { server } from '../test/mocks/server';
import { useEventTypes } from './useEventTypes';

describe('useEventTypes', () => {
  it('returns event types after fetch', async () => {
    const { result } = renderHook(() => useEventTypes());
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.data).toHaveLength(2);
    expect(result.current.data[0].name).toBe('Quick Call');
  });

  it('sets error on API failure', async () => {
    server.use(
      http.get('/api/event-types', () =>
        HttpResponse.json({ message: 'Server error' }, { status: 500 })),
    );
    const { result } = renderHook(() => useEventTypes());
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.error).not.toBeNull();
  });

  it('refetch updates data', async () => {
    const { result } = renderHook(() => useEventTypes());
    await waitFor(() => expect(result.current.loading).toBe(false));
    result.current.refetch();
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.data).toHaveLength(2);
  });
});
