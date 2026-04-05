import { Box, Grid, Title, Text, Button, Card, List, ThemeIcon } from '@mantine/core';
import { IconCheck } from '@tabler/icons-react';
import { useNavigate } from 'react-router-dom';

export function HomePage() {
  const navigate = useNavigate();

  return (
    <Box
      style={{
        minHeight: 'calc(100vh - 60px)',
        background: 'linear-gradient(135deg, #fff1eb 0%, #ace0f9 100%)',
        display: 'flex',
        alignItems: 'center',
      }}
      px="xl"
    >
      <Grid maw={1100} mx="auto" w="100%" gutter="xl" align="center">
        <Grid.Col span={{ base: 12, md: 6 }}>
          <Text size="xs" tt="uppercase" c="dimmed" mb="xs" fw={600} style={{ letterSpacing: '0.08em' }}>
            Быстрое запись на звонок
          </Text>
          <Title order={1} size="3.5rem" lh={1.1} mb="lg">
            Calendar
          </Title>
          <Text size="md" c="dimmed" mb="xl" maw={420}>
            Забронируйте встречу за минуту — выбирайте тип события и удобное время.
          </Text>
          <Button
            size="md"
            color="orange"
            radius="md"
            onClick={() => navigate('/book')}
          >
            Записаться →
          </Button>
        </Grid.Col>

        <Grid.Col span={{ base: 12, md: 6 }}>
          <Card withBorder radius="lg" p="xl" shadow="sm" bg="white">
            <Title order={3} mb="md">Возможности</Title>
            <List
              spacing="sm"
              icon={
                <ThemeIcon color="orange" size={20} radius="xl" variant="light">
                  <IconCheck size={12} />
                </ThemeIcon>
              }
            >
              <List.Item>Выбор типа события и удобного времени для встречи</List.Item>
              <List.Item>Быстрое бронирование с подтверждением и дополнительными заметками</List.Item>
              <List.Item>Управление типами встреч и просмотр предстоящих записей в админке</List.Item>
            </List>
          </Card>
        </Grid.Col>
      </Grid>
    </Box>
  );
}
