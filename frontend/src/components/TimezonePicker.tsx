import { Select } from '@mantine/core';

interface Props {
  value: string;
  onChange: (tz: string) => void;
  label?: string;
  disabled?: boolean;
}

function getTimezones(): string[] {
  try {
    return (Intl as unknown as { supportedValuesOf: (key: string) => string[] }).supportedValuesOf('timeZone');
  } catch {
    return [
      'UTC',
      'America/New_York',
      'America/Chicago',
      'America/Denver',
      'America/Los_Angeles',
      'America/Sao_Paulo',
      'Europe/London',
      'Europe/Paris',
      'Europe/Berlin',
      'Europe/Moscow',
      'Asia/Dubai',
      'Asia/Kolkata',
      'Asia/Bangkok',
      'Asia/Shanghai',
      'Asia/Tokyo',
      'Australia/Sydney',
      'Pacific/Auckland',
    ];
  }
}

const TIMEZONES = getTimezones();

export function TimezonePicker({ value, onChange, label = 'Часовой пояс', disabled }: Props) {
  return (
    <Select
      label={label}
      data={TIMEZONES}
      value={value}
      onChange={(v) => v && onChange(v)}
      searchable
      disabled={disabled}
      maxDropdownHeight={300}
    />
  );
}
