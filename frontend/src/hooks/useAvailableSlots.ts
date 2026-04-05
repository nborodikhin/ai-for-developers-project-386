import { useState, useEffect } from 'react';
import { getAvailableSlots } from '../api';
import type { AvailableSlot } from '../api';

export function useAvailableSlots(eventTypeId: number | null, date: string | null) {
  const [data, setData] = useState<AvailableSlot[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (eventTypeId === null || date === null) return;
    setLoading(true);
    setError(null);
    getAvailableSlots(eventTypeId, date)
      .then(setData)
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false));
  }, [eventTypeId, date]);

  return { data, loading, error };
}
