import { Group, Button, Text } from '@mantine/core';
import { IconCalendarEvent } from '@tabler/icons-react';
import { Link, useNavigate } from 'react-router-dom';

export function Navbar() {
  const navigate = useNavigate();

  return (
    <Group h="100%" px="md" justify="space-between">
      <Group gap="xs" style={{ cursor: 'pointer' }} onClick={() => navigate('/')}>
        <IconCalendarEvent size={22} color="var(--mantine-color-orange-6)" />
        <Text fw={700} size="lg">Calendar</Text>
      </Group>
      <Group gap="xs">
        <Button variant="subtle" color="dark" component={Link} to="/book">
          Записаться
        </Button>
        <Button variant="subtle" color="dark" component={Link} to="/admin">
          Админка
        </Button>
      </Group>
    </Group>
  );
}
