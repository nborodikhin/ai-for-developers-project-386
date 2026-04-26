import { http, HttpResponse } from 'msw';

export const mockEventTypes = [
  { id: 1, name: 'Quick Call', description: '15 min call', durationMinutes: 15 },
  { id: 2, name: 'Deep Dive', description: '60 min session', durationMinutes: 60 },
];

export const mockBookings = [
  {
    id: 1,
    eventTypeId: 1,
    eventTypeName: 'Quick Call',
    guestName: 'Alice',
    guestEmail: 'alice@test.com',
    comment: null,
    startTime: '2026-05-10T09:00:00Z',
    endTime: '2026-05-10T09:15:00Z',
  },
  {
    id: 2,
    eventTypeId: 1,
    eventTypeName: 'Quick Call',
    guestName: 'Bob',
    guestEmail: 'bob@test.com',
    comment: 'Discuss project',
    startTime: '2026-05-10T09:30:00Z',
    endTime: '2026-05-10T09:45:00Z',
  },
];

export const mockSlots = [
  { startTime: '2026-05-15T09:00:00Z', endTime: '2026-05-15T09:30:00Z', available: true },
  { startTime: '2026-05-15T09:30:00Z', endTime: '2026-05-15T10:00:00Z', available: false },
  { startTime: '2026-05-15T10:00:00Z', endTime: '2026-05-15T10:30:00Z', available: true },
];

export const handlers = [
  http.get('/api/event-types', () => HttpResponse.json(mockEventTypes)),

  http.get('/api/event-types/:id', ({ params }) => {
    const et = mockEventTypes.find((e) => e.id === Number(params.id));
    if (!et) return new HttpResponse(null, { status: 404 });
    return HttpResponse.json(et);
  }),

  http.post('/api/event-types', async ({ request }) => {
    const body = await request.json() as Record<string, unknown>;
    return HttpResponse.json({ id: 99, ...body }, { status: 201 });
  }),

  http.put('/api/event-types/:id', async ({ params, request }) => {
    const body = await request.json() as Record<string, unknown>;
    return HttpResponse.json({ id: Number(params.id), ...body });
  }),

  http.delete('/api/event-types/:id', () => new HttpResponse(null, { status: 204 })),

  http.get('/api/event-types/:id/available-slots', () => HttpResponse.json(mockSlots)),

  http.post('/api/event-types/:id/bookings', async ({ request }) => {
    const body = await request.json() as Record<string, unknown>;
    return HttpResponse.json(
      { id: 100, eventTypeId: 1, eventTypeName: 'Quick Call', ...body },
      { status: 201 },
    );
  }),

  http.get('/api/bookings', ({ request }) => {
    const url = new URL(request.url);
    const email = url.searchParams.get('email');
    const filtered = email ? mockBookings.filter((b) => b.guestEmail === email) : mockBookings;
    return HttpResponse.json(filtered);
  }),

  http.put('/api/bookings/:id', async ({ params, request }) => {
    const body = await request.json() as Record<string, unknown>;
    return HttpResponse.json({ ...mockBookings[0], id: Number(params.id), ...body });
  }),

  http.delete('/api/bookings/:id', () => new HttpResponse(null, { status: 204 })),

  http.get('/api/settings', () => HttpResponse.json({ ownerTimezone: 'UTC' })),

  http.put('/api/settings', async ({ request }) => {
    const body = await request.json() as Record<string, unknown>;
    return HttpResponse.json({ ownerTimezone: body.timezone });
  }),
];
