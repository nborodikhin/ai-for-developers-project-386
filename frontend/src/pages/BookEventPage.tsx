import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Container, Grid, Title, Text, Loader, Center, Alert, Paper, Button, Stack,
} from '@mantine/core';
import dayjs from 'dayjs';
import 'dayjs/locale/ru';
import { useEventType } from '../hooks/useEventType';
import { useAvailableSlots } from '../hooks/useAvailableSlots';
import { EventInfoPanel } from '../components/EventInfoPanel';
import { CalendarPicker } from '../components/CalendarPicker';
import { SlotList } from '../components/SlotList';
import { BookingForm } from '../components/BookingForm';
import type { AvailableSlot } from '../api';

dayjs.locale('ru');

export function BookEventPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const eventTypeId = id ? parseInt(id, 10) : null;

  const { data: eventType, loading: etLoading, error: etError } = useEventType(eventTypeId);
  const [selectedDate, setSelectedDate] = useState<Date | null>(null);
  const [selectedSlot, setSelectedSlot] = useState<AvailableSlot | null>(null);
  const [showForm, setShowForm] = useState(false);

  const dateStr = selectedDate ? dayjs(selectedDate).format('YYYY-MM-DD') : null;
  const { data: slots, loading: slotsLoading } = useAvailableSlots(eventTypeId, dateStr);

  function handleDateChange(date: Date) {
    setSelectedDate(date);
    setSelectedSlot(null);
    setShowForm(false);
  }

  function handleSlotSelect(slot: AvailableSlot) {
    setSelectedSlot(slot);
    setShowForm(true);
  }

  function handleBookingSuccess() {
    setSelectedDate(null);
    setSelectedSlot(null);
    setShowForm(false);
  }

  if (etLoading) {
    return (
      <Center h={300}>
        <Loader />
      </Center>
    );
  }

  if (etError || !eventType) {
    return (
      <Container size="md" py="xl">
        <Alert color="red" title="Ошибка">
          {etError ?? 'Тип события не найден'}
        </Alert>
        <Button mt="md" variant="subtle" onClick={() => navigate('/book')}>
          ← Назад
        </Button>
      </Container>
    );
  }

  return (
    <Container size="xl" py="xl">
      <Title order={2} mb="xl">{eventType.name}</Title>

      <Grid gutter="xl">
        <Grid.Col span={{ base: 12, md: 3 }}>
          <Paper withBorder p="md" radius="md">
            <EventInfoPanel
              eventType={eventType}
              selectedDate={selectedDate}
              selectedSlot={selectedSlot}
            />
            <Button
              variant="subtle"
              color="gray"
              mt="xl"
              size="xs"
              onClick={() => navigate('/book')}
            >
              ← Назад к списку
            </Button>
          </Paper>
        </Grid.Col>

        <Grid.Col span={{ base: 12, md: 4 }}>
          <Paper withBorder p="md" radius="md">
            <Text fw={600} mb="md">Календарь</Text>
            <CalendarPicker value={selectedDate} onChange={handleDateChange} />
          </Paper>
        </Grid.Col>

        <Grid.Col span={{ base: 12, md: 5 }}>
          <Paper withBorder p="md" radius="md" style={{ minHeight: 200 }}>
            {!selectedDate ? (
              <Stack align="center" justify="center" h={200}>
                <Text c="dimmed" size="sm">Выберите дату в календаре</Text>
              </Stack>
            ) : showForm && selectedSlot ? (
              <>
                <Text fw={600} mb="md">Ваши данные</Text>
                <BookingForm
                  eventTypeId={eventType.id}
                  slot={selectedSlot}
                  onSuccess={handleBookingSuccess}
                  onCancel={() => { setShowForm(false); setSelectedSlot(null); }}
                />
              </>
            ) : (
              <>
                <Text fw={600} mb="md">Статус слотов</Text>
                <SlotList
                  slots={slots}
                  loading={slotsLoading}
                  selectedSlot={selectedSlot}
                  onSelect={handleSlotSelect}
                />
              </>
            )}
          </Paper>
        </Grid.Col>
      </Grid>
    </Container>
  );
}
