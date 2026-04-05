namespace CalendarApi;

model EventType {
  id: int64;
  name: string;
  description: string;
  durationMinutes: int32;
}

model CreateEventTypeRequest {
  name: string;
  description: string;
  durationMinutes: int32;
}

model Booking {
  id: int64;
  eventTypeId: int64;
  eventTypeName: string;
  guestName: string;
  guestEmail: string;
  comment?: string;
  startTime: utcDateTime;
  endTime: utcDateTime;
}

model CreateBookingRequest {
  guestName: string;
  guestEmail: string;
  comment?: string;
  startTime: utcDateTime;
}

model UpdateBookingRequest {
  guestName?: string;
  comment?: string;
}

model AvailableSlot {
  startTime: utcDateTime;
  endTime: utcDateTime;
  available: boolean;
}

model ErrorResponse {
  message: string;
}
