import { renderHook, waitFor } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { server } from '../test/mocks/server';
import { useEventType } from './useEventType';

describe('useEventType', () => {
  it('does not fetch when id is null', () => {
    const { result } = renderHook(() => useEventType(null));
    expect(result.current.loading).toBe(false);
    expect(result.current.data).toBeNull();
  });

  it('fetches and returns event type by id', async () => {
    const { result } = renderHook(() => useEventType(1));
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.data?.name).toBe('Quick Call');
    expect(result.current.data?.durationMinutes).toBe(15);
  });

  it('sets error on 404', async () => {
    server.use(
      http.get('/api/event-types/:id', () => new HttpResponse(null, { status: 404 })),
    );
    const { result } = renderHook(() => useEventType(99999));
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.error).not.toBeNull();
  });
});
