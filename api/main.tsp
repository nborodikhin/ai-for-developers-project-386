import "@typespec/http";
import "@typespec/rest";
import "@typespec/openapi";

import "./models.tsp";
import "./routes.tsp";

using TypeSpec.Http;
using TypeSpec.OpenAPI;

@service
@info(#{ title: "Calendar Booking API", version: "1.0.0" })
@server("http://localhost:8080", "Local development server")
namespace CalendarApi;
