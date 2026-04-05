import { apiFetch } from './client';
import type { AvailableSlot } from './types';

export function getAvailableSlots(eventTypeId: number, date: string): Promise<AvailableSlot[]> {
  return apiFetch<AvailableSlot[]>(
    `/api/event-types/${eventTypeId}/available-slots?date=${encodeURIComponent(date)}`,
  );
}
