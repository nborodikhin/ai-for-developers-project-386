import { useState } from 'react';
import { Stack, TextInput, Textarea, Button, Group } from '@mantine/core';
import { notifications } from '@mantine/notifications';
import { createBooking, ApiError } from '../api';
import type { AvailableSlot } from '../api';

interface Props {
  eventTypeId: number;
  slot: AvailableSlot;
  onSuccess: () => void;
  onCancel: () => void;
}

export function BookingForm({ eventTypeId, slot, onSuccess, onCancel }: Props) {
  const [guestName, setGuestName] = useState('');
  const [guestEmail, setGuestEmail] = useState('');
  const [comment, setComment] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    try {
      await createBooking(eventTypeId, {
        guestName,
        guestEmail,
        comment: comment || undefined,
        startTime: slot.startTime,
      });
      notifications.show({
        title: 'Готово!',
        message: 'Встреча успешно забронирована.',
        color: 'green',
      });
      onSuccess();
    } catch (err) {
      const msg = err instanceof ApiError && err.status === 409
        ? 'Этот слот уже занят. Выберите другое время.'
        : 'Не удалось создать запись. Попробуйте ещё раз.';
      notifications.show({ title: 'Ошибка', message: msg, color: 'red' });
    } finally {
      setLoading(false);
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <Stack gap="sm">
        <TextInput
          label="Имя"
          placeholder="Ваше имя"
          required
          value={guestName}
          onChange={(e) => setGuestName(e.currentTarget.value)}
        />
        <TextInput
          label="Email"
          placeholder="you@example.com"
          type="email"
          required
          value={guestEmail}
          onChange={(e) => setGuestEmail(e.currentTarget.value)}
        />
        <Textarea
          label="Комментарий"
          placeholder="Необязательно"
          value={comment}
          onChange={(e) => setComment(e.currentTarget.value)}
          autosize
          minRows={2}
        />
        <Group justify="flex-end" mt="xs">
          <Button variant="subtle" color="gray" onClick={onCancel} disabled={loading}>
            Назад
          </Button>
          <Button type="submit" loading={loading}>
            Записаться
          </Button>
        </Group>
      </Stack>
    </form>
  );
}
