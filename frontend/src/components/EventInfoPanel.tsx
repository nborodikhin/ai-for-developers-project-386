import { Stack, Avatar, Text, Badge, Divider } from '@mantine/core';
import type { EventType, AvailableSlot } from '../api';
import dayjs from '../lib/dayjs';

interface Props {
  eventType: EventType;
  selectedDate: Date | null;
  selectedSlot: AvailableSlot | null;
  userTimezone: string;
}

export function EventInfoPanel({ eventType, selectedDate, selectedSlot, userTimezone }: Props) {
  const dateLabel = selectedDate
    ? dayjs(selectedDate).format('D MMMM')
    : 'Выберите дату';

  const timeLabel = selectedSlot
    ? `${dayjs(selectedSlot.startTime).tz(userTimezone).format('HH:mm')} — ${dayjs(selectedSlot.endTime).tz(userTimezone).format('HH:mm')}`
    : 'Время не выбрано';

  return (
    <Stack gap="md">
      <Stack gap="xs" align="flex-start">
        <Avatar color="orange" radius="xl" size="md">T</Avatar>
        <Text fw={500}>Tota</Text>
        <Text size="xs" c="dimmed">Host</Text>
      </Stack>

      <Divider />

      <Stack gap="xs">
        <Text fw={600} size="lg">{eventType.name}</Text>
        <Badge variant="light" color="gray" radius="sm" w="fit-content">
          {eventType.durationMinutes} мин
        </Badge>
      </Stack>

      <Divider />

      <Stack gap={4}>
        <Text size="sm" c="dimmed">Выбранная дата</Text>
        <Text size="sm" fw={500}>{dateLabel}</Text>
      </Stack>

      <Stack gap={4}>
        <Text size="sm" c="dimmed">Выбранное время</Text>
        <Text size="sm" fw={500}>{timeLabel}</Text>
      </Stack>

      <Stack gap={4}>
        <Text size="sm" c="dimmed">Ваш часовой пояс</Text>
        <Text size="sm" fw={500}>{userTimezone}</Text>
      </Stack>
    </Stack>
  );
}
