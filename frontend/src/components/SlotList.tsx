import { Stack, Button, Badge, Group, Text, Loader, Center } from '@mantine/core';
import type { AvailableSlot } from '../api';
import dayjs from 'dayjs';

interface Props {
  slots: AvailableSlot[];
  loading: boolean;
  selectedSlot: AvailableSlot | null;
  onSelect: (slot: AvailableSlot) => void;
}

function formatTime(iso: string) {
  return dayjs(iso).format('HH:mm');
}

export function SlotList({ slots, loading, selectedSlot, onSelect }: Props) {
  if (loading) {
    return (
      <Center h={120}>
        <Loader size="sm" />
      </Center>
    );
  }

  if (slots.length === 0) {
    return (
      <Text c="dimmed" size="sm" ta="center" mt="md">
        Нет доступных слотов на этот день
      </Text>
    );
  }

  return (
    <Stack gap="xs">
      {slots.map((slot) => {
        const label = `${formatTime(slot.startTime)} — ${formatTime(slot.endTime)}`;
        const isSelected = selectedSlot?.startTime === slot.startTime;

        if (!slot.available) {
          return (
            <Group key={slot.startTime} justify="space-between" px="xs">
              <Text size="sm" c="dimmed">{label}</Text>
              <Badge color="orange" variant="light" radius="sm">Занято</Badge>
            </Group>
          );
        }

        return (
          <Button
            key={slot.startTime}
            variant={isSelected ? 'filled' : 'light'}
            color="green"
            justify="space-between"
            rightSection={
              <Badge color="green" variant={isSelected ? 'white' : 'light'} radius="sm">
                Свободен
              </Badge>
            }
            onClick={() => onSelect(slot)}
            fullWidth
          >
            {label}
          </Button>
        );
      })}
    </Stack>
  );
}
