import { apiFetch } from './client';
import type { Booking, CreateBookingRequest, UpdateBookingRequest } from './types';

export function listBookings(email?: string): Promise<Booking[]> {
  const url = email ? `/api/bookings?email=${encodeURIComponent(email)}` : '/api/bookings';
  return apiFetch<Booking[]>(url);
}

export function createBooking(eventTypeId: number, body: CreateBookingRequest): Promise<Booking> {
  return apiFetch<Booking>(`/api/event-types/${eventTypeId}/bookings`, {
    method: 'POST',
    body: JSON.stringify(body),
  });
}

export function updateBooking(id: number, body: UpdateBookingRequest): Promise<Booking> {
  return apiFetch<Booking>(`/api/bookings/${id}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  });
}

export function deleteBooking(id: number): Promise<void> {
  return apiFetch<void>(`/api/bookings/${id}`, { method: 'DELETE' });
}
