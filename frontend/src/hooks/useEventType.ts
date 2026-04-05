import { useState, useEffect } from 'react';
import { getEventType } from '../api';
import type { EventType } from '../api';

export function useEventType(id: number | null) {
  const [data, setData] = useState<EventType | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (id === null) return;
    setLoading(true);
    setError(null);
    getEventType(id)
      .then(setData)
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false));
  }, [id]);

  return { data, loading, error };
}
