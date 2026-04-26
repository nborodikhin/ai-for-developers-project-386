import { apiFetch } from './client';
import type { OwnerSettings } from './types';

export function getSettings(): Promise<OwnerSettings> {
  return apiFetch<OwnerSettings>('/api/settings');
}

export function updateTimezone(timezone: string): Promise<OwnerSettings> {
  return apiFetch<OwnerSettings>('/api/settings', {
    method: 'PUT',
    body: JSON.stringify({ timezone }),
  });
}
