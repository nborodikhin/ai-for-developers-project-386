import { DatePicker } from '@mantine/dates';
import dayjs from 'dayjs';

interface Props {
  value: Date | null;
  onChange: (date: Date) => void;
}

export function CalendarPicker({ value, onChange }: Props) {
  const today = dayjs().startOf('day').toDate();

  return (
    <DatePicker
      value={value}
      onChange={(date) => date && onChange(date)}
      minDate={today}
      size="md"
    />
  );
}
