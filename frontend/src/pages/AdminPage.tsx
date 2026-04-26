import { useState, useEffect } from 'react';
import {
  Container, Title, Tabs, Table, Button, Group, Text, Modal, Stack,
  TextInput, Textarea, NumberInput, Alert, Loader, Center, ActionIcon, Badge, Paper,
} from '@mantine/core';
import { IconEdit, IconTrash, IconPlus } from '@tabler/icons-react';
import { notifications } from '@mantine/notifications';
import { useEventTypes } from '../hooks/useEventTypes';
import { useBookings } from '../hooks/useBookings';
import { createEventType, updateEventType, deleteEventType, getSettings, updateTimezone } from '../api';
import type { EventType, CreateEventTypeRequest } from '../api';
import dayjs from '../lib/dayjs';
import { TimezonePicker } from '../components/TimezonePicker';

function EventTypeModal({
  opened,
  onClose,
  initial,
  onSaved,
}: {
  opened: boolean;
  onClose: () => void;
  initial: EventType | null;
  onSaved: () => void;
}) {
  const [name, setName] = useState(initial?.name ?? '');
  const [description, setDescription] = useState(initial?.description ?? '');
  const [durationMinutes, setDurationMinutes] = useState<number>(initial?.durationMinutes ?? 30);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    const body: CreateEventTypeRequest = { name, description, durationMinutes };
    setLoading(true);
    try {
      if (initial) {
        await updateEventType(initial.id, body);
        notifications.show({ message: 'Тип события обновлён', color: 'green' });
      } else {
        await createEventType(body);
        notifications.show({ message: 'Тип события создан', color: 'green' });
      }
      onSaved();
      onClose();
    } catch {
      notifications.show({ title: 'Ошибка', message: 'Не удалось сохранить', color: 'red' });
    } finally {
      setLoading(false);
    }
  }

  return (
    <Modal
      opened={opened}
      onClose={onClose}
      title={initial ? 'Редактировать тип события' : 'Новый тип события'}
    >
      <form onSubmit={handleSubmit}>
        <Stack gap="sm">
          <TextInput
            label="Название"
            required
            value={name}
            onChange={(e) => setName(e.currentTarget.value)}
          />
          <Textarea
            label="Описание"
            required
            value={description}
            onChange={(e) => setDescription(e.currentTarget.value)}
            autosize
            minRows={2}
          />
          <NumberInput
            label="Длительность (мин)"
            required
            min={5}
            step={5}
            value={durationMinutes}
            onChange={(v) => setDurationMinutes(Number(v))}
          />
          <Group justify="flex-end" mt="xs">
            <Button variant="subtle" color="gray" onClick={onClose} disabled={loading}>
              Отмена
            </Button>
            <Button type="submit" loading={loading}>
              Сохранить
            </Button>
          </Group>
        </Stack>
      </form>
    </Modal>
  );
}

export function AdminPage() {
  const { data: eventTypes, loading: etLoading, error: etError, refetch: refetchET } = useEventTypes();
  const { data: bookings, loading: bLoading, error: bError } = useBookings();

  const [modalOpen, setModalOpen] = useState(false);
  const [editTarget, setEditTarget] = useState<EventType | null>(null);

  const [ownerTimezone, setOwnerTimezone] = useState<string>('UTC');
  const [tzSaving, setTzSaving] = useState(false);

  useEffect(() => {
    getSettings().then((s) => setOwnerTimezone(s.ownerTimezone)).catch(() => {});
  }, []);

  function openCreate() {
    setEditTarget(null);
    setModalOpen(true);
  }

  function openEdit(et: EventType) {
    setEditTarget(et);
    setModalOpen(true);
  }

  async function handleDelete(et: EventType) {
    if (!confirm(`Удалить «${et.name}»?`)) return;
    try {
      await deleteEventType(et.id);
      notifications.show({ message: 'Тип события удалён', color: 'green' });
      refetchET();
    } catch {
      notifications.show({ title: 'Ошибка', message: 'Не удалось удалить', color: 'red' });
    }
  }

  async function handleTimezoneChange(tz: string) {
    setOwnerTimezone(tz);
    setTzSaving(true);
    try {
      await updateTimezone(tz);
      notifications.show({ message: 'Часовой пояс обновлён', color: 'green' });
    } catch {
      notifications.show({ title: 'Ошибка', message: 'Не удалось обновить часовой пояс', color: 'red' });
    } finally {
      setTzSaving(false);
    }
  }

  return (
    <Container size="lg" py="xl">
      <Title order={2} mb="xl">Админка</Title>

      <Paper withBorder p="md" radius="md" mb="xl">
        <Title order={4} mb="sm">Настройки</Title>
        <TimezonePicker
          value={ownerTimezone}
          onChange={handleTimezoneChange}
          label="Часовой пояс владельца (рабочее время 09:00–18:00 в этом поясе)"
          disabled={tzSaving}
        />
      </Paper>

      <Tabs defaultValue="event-types">
        <Tabs.List mb="lg">
          <Tabs.Tab value="event-types">Типы событий</Tabs.Tab>
          <Tabs.Tab value="bookings">Записи</Tabs.Tab>
        </Tabs.List>

        <Tabs.Panel value="event-types">
          <Group justify="flex-end" mb="md">
            <Button leftSection={<IconPlus size={16} />} onClick={openCreate}>
              Добавить
            </Button>
          </Group>

          {etError && <Alert color="red" mb="md">{etError}</Alert>}
          {etLoading ? (
            <Center h={100}><Loader /></Center>
          ) : (
            <Table withTableBorder withColumnBorders>
              <Table.Thead>
                <Table.Tr>
                  <Table.Th>ID</Table.Th>
                  <Table.Th>Название</Table.Th>
                  <Table.Th>Описание</Table.Th>
                  <Table.Th>Длительность</Table.Th>
                  <Table.Th>Действия</Table.Th>
                </Table.Tr>
              </Table.Thead>
              <Table.Tbody>
                {eventTypes.map((et) => (
                  <Table.Tr key={et.id}>
                    <Table.Td>{et.id}</Table.Td>
                    <Table.Td>{et.name}</Table.Td>
                    <Table.Td>
                      <Text size="sm" c="dimmed" lineClamp={2}>{et.description}</Text>
                    </Table.Td>
                    <Table.Td>
                      <Badge variant="light" color="gray">{et.durationMinutes} мин</Badge>
                    </Table.Td>
                    <Table.Td>
                      <Group gap="xs">
                        <ActionIcon variant="subtle" onClick={() => openEdit(et)}>
                          <IconEdit size={16} />
                        </ActionIcon>
                        <ActionIcon variant="subtle" color="red" onClick={() => handleDelete(et)}>
                          <IconTrash size={16} />
                        </ActionIcon>
                      </Group>
                    </Table.Td>
                  </Table.Tr>
                ))}
              </Table.Tbody>
            </Table>
          )}

          <EventTypeModal
            opened={modalOpen}
            onClose={() => setModalOpen(false)}
            initial={editTarget}
            onSaved={refetchET}
          />
        </Tabs.Panel>

        <Tabs.Panel value="bookings">
          {bError && <Alert color="red" mb="md">{bError}</Alert>}
          {bLoading ? (
            <Center h={100}><Loader /></Center>
          ) : (
            <Table withTableBorder withColumnBorders>
              <Table.Thead>
                <Table.Tr>
                  <Table.Th>Гость</Table.Th>
                  <Table.Th>Email</Table.Th>
                  <Table.Th>Тип события</Table.Th>
                  <Table.Th>Начало ({ownerTimezone})</Table.Th>
                  <Table.Th>Конец</Table.Th>
                  <Table.Th>Комментарий</Table.Th>
                </Table.Tr>
              </Table.Thead>
              <Table.Tbody>
                {bookings.map((b) => (
                  <Table.Tr key={b.id}>
                    <Table.Td>{b.guestName}</Table.Td>
                    <Table.Td>{b.guestEmail}</Table.Td>
                    <Table.Td>{b.eventTypeName}</Table.Td>
                    <Table.Td>{dayjs(b.startTime).tz(ownerTimezone).format('DD.MM.YYYY HH:mm')}</Table.Td>
                    <Table.Td>{dayjs(b.endTime).tz(ownerTimezone).format('HH:mm')}</Table.Td>
                    <Table.Td>
                      <Text size="sm" c="dimmed">{b.comment ?? '—'}</Text>
                    </Table.Td>
                  </Table.Tr>
                ))}
              </Table.Tbody>
            </Table>
          )}
        </Tabs.Panel>
      </Tabs>
    </Container>
  );
}
