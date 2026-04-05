import { apiFetch } from './client';
import type { EventType, CreateEventTypeRequest } from './types';

export function listEventTypes(): Promise<EventType[]> {
  return apiFetch<EventType[]>('/api/event-types');
}

export function getEventType(id: number): Promise<EventType> {
  return apiFetch<EventType>(`/api/event-types/${id}`);
}

export function createEventType(body: CreateEventTypeRequest): Promise<EventType> {
  return apiFetch<EventType>('/api/event-types', {
    method: 'POST',
    body: JSON.stringify(body),
  });
}

export function updateEventType(id: number, body: CreateEventTypeRequest): Promise<EventType> {
  return apiFetch<EventType>(`/api/event-types/${id}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  });
}

export function deleteEventType(id: number): Promise<void> {
  return apiFetch<void>(`/api/event-types/${id}`, { method: 'DELETE' });
}
