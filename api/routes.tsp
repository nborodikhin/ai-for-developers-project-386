using TypeSpec.Http;
using TypeSpec.Rest;

namespace CalendarApi;

@route("/api/event-types")
@tag("EventTypes")
interface EventTypes {
  @post
  @doc("Create a new event type")
  create(@body body: CreateEventTypeRequest): {
    @statusCode statusCode: 201;
    @body body: EventType;
  };

  @get
  @doc("List all event types (non-deleted)")
  list(): {
    @statusCode statusCode: 200;
    @body body: EventType[];
  };

  @get
  @doc("Get a single event type by ID")
  read(@path id: int64): {
    @statusCode statusCode: 200;
    @body body: EventType;
  } | {
    @statusCode statusCode: 404;
    @body body: ErrorResponse;
  };

  @put
  @doc("Update an event type")
  update(@path id: int64, @body body: CreateEventTypeRequest): {
    @statusCode statusCode: 200;
    @body body: EventType;
  } | {
    @statusCode statusCode: 404;
    @body body: ErrorResponse;
  };

  @delete
  @doc("Soft-delete an event type (bookings are preserved)")
  delete(@path id: int64): {
    @statusCode statusCode: 204;
  } | {
    @statusCode statusCode: 404;
    @body body: ErrorResponse;
  };
}

@route("/api/event-types/{eventTypeId}/bookings")
@tag("EventTypeBookings")
interface EventTypeBookings {
  @post
  @doc("Create a booking for an event type")
  create(@path eventTypeId: int64, @body body: CreateBookingRequest): {
    @statusCode statusCode: 201;
    @body body: Booking;
  } | {
    @statusCode statusCode: 404;
    @body body: ErrorResponse;
  } | {
    @statusCode statusCode: 409;
    @body body: ErrorResponse;
  };
}

@route("/api/event-types/{eventTypeId}/available-slots")
@tag("AvailableSlots")
interface AvailableSlots {
  @get
  @doc("Get available time slots for a given date")
  list(@path eventTypeId: int64, @query date: plainDate): {
    @statusCode statusCode: 200;
    @body body: AvailableSlot[];
  } | {
    @statusCode statusCode: 404;
    @body body: ErrorResponse;
  };
}

@route("/api/bookings")
@tag("Bookings")
interface Bookings {
  @get
  @doc("List bookings. Without email: all upcoming (owner view). With email: guest's bookings.")
  list(@query email?: string): {
    @statusCode statusCode: 200;
    @body body: Booking[];
  };

  @put
  @doc("Update a booking (only guestName and comment)")
  update(@path id: int64, @body body: UpdateBookingRequest): {
    @statusCode statusCode: 200;
    @body body: Booking;
  } | {
    @statusCode statusCode: 404;
    @body body: ErrorResponse;
  };

  @delete
  @doc("Cancel a booking")
  delete(@path id: int64): {
    @statusCode statusCode: 204;
  } | {
    @statusCode statusCode: 404;
    @body body: ErrorResponse;
  };
}
