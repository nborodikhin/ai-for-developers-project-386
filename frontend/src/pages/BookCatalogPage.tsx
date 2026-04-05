import { Container, Avatar, Text, Title, SimpleGrid, Alert, Loader, Center, Stack } from '@mantine/core';
import { useNavigate } from 'react-router-dom';
import { useEventTypes } from '../hooks/useEventTypes';
import { EventTypeCard } from '../components/EventTypeCard';

export function BookCatalogPage() {
  const { data, loading, error } = useEventTypes();
  const navigate = useNavigate();

  return (
    <Container size="lg" py="xl">
      <Stack gap="xl">
        <Stack gap="xs">
          <Avatar color="orange" radius="xl" size="lg">T</Avatar>
          <Text fw={600} size="md">Tota</Text>
          <Text size="xs" c="dimmed">Host</Text>
          <Title order={2} mt="sm">Выберите тип события</Title>
          <Text size="sm" c="dimmed">
            Нажмите на карточку, чтобы открыть календарь и выбрать удобный слот.
          </Text>
        </Stack>

        {error && (
          <Alert color="red" title="Ошибка загрузки">
            {error}
          </Alert>
        )}

        {loading ? (
          <Center h={120}>
            <Loader />
          </Center>
        ) : (
          <SimpleGrid cols={{ base: 1, sm: 2 }} spacing="md">
            {data.map((et) => (
              <EventTypeCard
                key={et.id}
                eventType={et}
                onClick={() => navigate(`/book/${et.id}`)}
              />
            ))}
          </SimpleGrid>
        )}
      </Stack>
    </Container>
  );
}
