import { Card, Text, Badge, Group } from '@mantine/core';
import type { EventType } from '../api';

interface Props {
  eventType: EventType;
  onClick?: () => void;
}

export function EventTypeCard({ eventType, onClick }: Props) {
  return (
    <Card
      withBorder
      radius="md"
      padding="lg"
      style={{ cursor: onClick ? 'pointer' : undefined }}
      onClick={onClick}
    >
      <Group justify="space-between" align="flex-start" wrap="nowrap">
        <Text fw={600} size="md">{eventType.name}</Text>
        <Badge variant="light" color="gray" radius="sm" style={{ flexShrink: 0 }}>
          {eventType.durationMinutes} мин
        </Badge>
      </Group>
      <Text size="sm" c="dimmed" mt="xs">{eventType.description}</Text>
    </Card>
  );
}
