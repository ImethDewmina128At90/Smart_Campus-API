### UOW_ID=w2153254 , IIT_ID =20240076 
# Smart Campus Sensor & Room Management API


## Overview
This project is a RESTful API built using JAX-RS (Jersey) as part of the
5COSC022W Client-Server Architectures coursework at the University of
Westminster. The API simulates a Smart Campus infrastructure, providing
endpoints to manage Rooms and IoT Sensors deployed across campus buildings.

The system is built as a high-performance web service that allows campus
facilities managers and automated building systems to interact with campus
data through a clean, resource-based REST interface.

### Core Resources
- **Rooms** — Physical campus rooms with capacity and assigned sensors
- **Sensors** — IoT devices (CO2, Temperature, Occupancy) deployed in rooms
- **Sensor Readings** — Historical measurement logs per sensor

### Tech Stack
- Java 11
- JAX-RS 3.x (Jersey implementation)
- Grizzly embedded HTTP server
- Jackson for JSON serialization
- Maven for build management
- In-memory storage using `ConcurrentHashMap`

---

## How to Build and Run

### Prerequisites
- Java 11 or higher installed
- Maven 3.6+ installed

### Steps
1. Clone the repository:
```bash
   git clone https://github.com/YOUR_USERNAME/smart-campus-api.git
   cd smart-campus-api
```

2. Build the project:
```bash
   mvn clean package
```

3. Run the server:
```bash
   java -jar target/smart-campus-api-1.0-SNAPSHOT.jar
```

4. The API will be available at:  http://localhost:8080/api/v1

   ---

## Sample curl Commands

### 1. Discovery Endpoint
```bash
curl -X GET http://localhost:8080/api/v1
```

### 2. Create a Room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"LIB-301","name":"Library Quiet Study","capacity":50}'
```

### 3. Get All Rooms
```bash
curl -X GET http://localhost:8080/api/v1/rooms
```

### 4. Register a Sensor
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-001","type":"Temperature","status":"ACTIVE","currentValue":22.5,"roomId":"LIB-301"}'
```

### 5. Get Sensors by Type
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=Temperature"
```

---

## Report — Question Answers

### Part 1 — Service Architecture & Setup

**Q: Explain the default lifecycle of a JAX-RS Resource class.**
By default, JAX-RS creates a new instance of each Resource class for every
incoming HTTP request (per-request scope). This means instance variables are
reset after each request and cannot be used to persist data between calls.
To safely share in-memory data structures (such as a HashMap of rooms or
sensors) across all requests, they must be declared as static fields on a
dedicated data store class. In a concurrent environment, ConcurrentHashMap
must be used instead of plain HashMap to prevent race conditions when multiple
requests read and write simultaneously.

**Q: Why is HATEOAS considered a hallmark of advanced RESTful design?**
HATEOAS (Hypermedia As The Engine Of Application State) allows clients to
navigate the entire API dynamically by following links embedded in responses,
rather than relying on hard-coded URLs or external documentation. If resource
paths change server-side, clients that follow links dynamically do not break.
This reduces tight coupling between client and server and makes the API
self-documenting at runtime.

---

### Part 2 — Room Management

**Q: When returning a list of rooms, what are the implications of returning
only IDs versus returning full room objects?**

Returning only IDs minimises bandwidth and is useful when the client only
needs to check existence or build a list of references. However, it forces
the client to make N additional requests to fetch details for each room,
causing the N+1 problem. Returning full room objects increases payload size
but reduces round trips and client-side processing. For a campus management
system where rooms have relatively small payloads, returning full objects
is the better default, with pagination added if the dataset grows large.

**Q: Is the DELETE operation idempotent in your implementation?**

DELETE is considered idempotent in REST because repeated calls produce no
additional side effects beyond the first. In this implementation, the first
DELETE removes the room and returns HTTP 200. Subsequent DELETE requests for
the same room return HTTP 404 since the room no longer exists. While the
response code differs between the first and subsequent calls, no unintended
state change occurs — the system remains in the same final state regardless
of how many times the DELETE is sent. This is consistent with the REST
definition of idempotency, which concerns state changes, not response codes.
### Part 3 — Sensor Operations & Filtering

**Q: Explain the technical consequences if a client sends data in a format
other than application/json to a POST endpoint annotated with
@Consumes(MediaType.APPLICATION_JSON).**

JAX-RS automatically handles the media type mismatch before the resource
method is even invoked. If a client sends a request with Content-Type of
text/plain or application/xml, the JAX-RS runtime returns an HTTP 415
Unsupported Media Type response immediately. The server correctly signals
that it cannot process the request body in the provided format. This
behaviour is built into the framework and requires no additional code in
the resource class.

**Q: Why is the @QueryParam approach superior to embedding the filter in
the URL path (e.g. /sensors/type/CO2) for filtering collections?**

Query parameters are semantically designed for filtering, searching, and
sorting collections. Using @QueryParam makes the filter optional — clients
can call GET /sensors to get all sensors, or GET /sensors?type=CO2 to filter.
A path-based approach like /sensors/type/CO2 implies that "type/CO2" is a
specific resource identity, which is misleading. It also makes the filter
mandatory and creates a proliferation of nested paths for multiple filter
combinations. Query parameters are composable, optional, and clearly
communicate search intent to both humans and tooling.

---

### Part 4 — Sub-Resources

**Q: Discuss the architectural benefits of the Sub-Resource Locator pattern.**

The Sub-Resource Locator pattern allows a parent resource to delegate
handling of nested paths to a dedicated child resource class. In this
implementation, SensorResource delegates all /sensors/{id}/readings requests
to SensorReadingResource. This separation of concerns keeps each class
focused on a single responsibility — SensorResource manages sensor lifecycle
while SensorReadingResource manages historical data. In large APIs with many
nested resources, defining every path in one massive controller becomes
unmanageable and hard to test. Sub-resource locators allow teams to work on
different resource classes independently, improving maintainability,
readability, and scalability of the codebase.

---

### Part 5 — Error Handling & Logging

**Q: Why is HTTP 422 more semantically accurate than 404 when a referenced
resource is missing inside a valid JSON payload?**

HTTP 404 Not Found means the requested URL endpoint does not exist on the
server. HTTP 422 Unprocessable Entity means the server understood the
request and the URL is valid, but it cannot process the instruction because
the data relationships inside the payload are invalid. When a client POSTs
a new sensor with a roomId that does not exist, the URL /api/v1/sensors is
perfectly valid — the problem is the foreign key reference inside the body.
Returning 404 would mislead the client into thinking the /sensors endpoint
itself was not found. A 422 precisely communicates that the request was
received and parsed, but the referenced resource dependency could not be
resolved.

**Q: From a cybersecurity standpoint, explain the risks of exposing internal
Java stack traces to external API consumers.**

Exposing stack traces to external clients is a serious security vulnerability.
Stack traces reveal internal class names, method signatures, and line numbers,
which allow an attacker to map the application's internal structure. They
expose the names and versions of third-party libraries in use, enabling the
attacker to look up known CVEs for those specific versions. They can reveal
server file paths and package structures. They may expose SQL queries or
connection strings in database-related exceptions. They also reveal
application logic and control flow, which can be used to craft targeted
injection, path traversal, or exploitation attacks. The GlobalExceptionMapper
in this implementation ensures all unexpected errors return a safe, generic
HTTP 500 message while logging full details server-side only, where they are
accessible to developers but never exposed to clients.

**Q: Why is it better to use JAX-RS filters for cross-cutting concerns like
logging rather than manually inserting Logger.info() in every resource method?**

Filters implement the cross-cutting concern principle — logging applies to
every endpoint equally and has nothing to do with business logic. Manually
inserting Logger.info() in every resource method violates the DRY (Don't
Repeat Yourself) principle and clutters business logic with infrastructure
concerns. If logging requirements change, every method would need to be
updated individually. Filters centralise this behaviour in one place,
apply automatically to all current and future endpoints without any changes
to resource classes, and can be enabled or disabled globally. This makes
the codebase cleaner, easier to maintain, and less error-prone.


### UOW_ID=w2153254 , IIT_ID =20240076 
