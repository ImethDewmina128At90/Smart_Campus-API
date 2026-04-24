### UOW_ID=w2153254 , IIT_ID=20240076 
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
- JAX-RS 2.x (Jersey 2.41 implementation) — `javax.ws.rs.*` namespace
- Embedded Apache Tomcat 9.0.83 (`tomcat-embed-core`)
- Jackson for JSON serialization
- Maven for build management
- In-memory storage using `ConcurrentHashMap`

> ⚠️ **Note**: This project uses **Jersey 2.41 + Tomcat 9.x** to maintain compatibility with the `javax.ws.rs.*` namespace as specified in the coursework. Do not upgrade to Jersey 3.x or Tomcat 10.x without updating all imports to `jakarta.*`.

---

## 1. API Design Overview

### High-Level Architecture

This RESTful service follows a layered architecture built on top of an **embedded Apache Tomcat 9.0.83** server. The application bootstraps a Tomcat instance programmatically in `Main.java`, registers a Jersey `ServletContainer` with the server, and delegates all HTTP request handling to JAX-RS resource classes. No external application server installation or WAR deployment is required — the entire service runs as a standalone executable JAR.

### Base Path

All API endpoints are served under the base path:

```
/api/v1
```

This versioned base path is configured as the Tomcat context path in `Main.java`, ensuring all resource routes are mounted beneath it. The discovery endpoint at `GET /api/v1` provides HATEOAS-style links to all available resources.

### Core Resources

| Resource | Endpoint | Description |
|----------|----------|-------------|
| **Rooms** | `/api/v1/rooms` | CRUD operations for physical campus rooms (lecture halls, labs, libraries) |
| **Sensors** | `/api/v1/sensors` | CRUD operations for IoT devices (CO2, Temperature, Occupancy sensors) with query-based filtering by type |
| **Sensor Readings** | `/api/v1/sensors/{id}/readings` | Sub-resource for managing historical measurement logs per sensor (uses the Sub-Resource Locator pattern) |
| **Discovery** | `/api/v1` | Entry-point endpoint returning API metadata and HATEOAS navigation links |

### Data Storage Mechanism (In-Memory)

This project **does not use any external database**. All data is stored entirely in-memory using Java's built-in concurrent collections, managed by the `DataStore` class (`com.smartcampus.store.DataStore`):

| Data | Collection Type | Structure |
|------|----------------|-----------|
| Rooms | `ConcurrentHashMap<String, Room>` | Maps room ID → Room object |
| Sensors | `ConcurrentHashMap<String, Sensor>` | Maps sensor ID → Sensor object |
| Sensor Readings | `ConcurrentHashMap<String, List<SensorReading>>` | Maps sensor ID → `ArrayList` of SensorReading objects |

- **`ConcurrentHashMap`** is used instead of a plain `HashMap` to ensure thread-safe read/write access when multiple HTTP requests are processed simultaneously by Tomcat's thread pool.
- **`ArrayList`** is used within the readings map to store an ordered, chronological list of sensor measurements per sensor.
- All data is held in **static fields**, making it accessible across the per-request lifecycle of JAX-RS resource instances without requiring dependency injection.
- Since storage is in-memory, **all data is lost when the server stops** — this is by design and satisfies the coursework constraint of no external database dependencies.

### Embedded Server — Apache Tomcat 9.0.83

The project uses **Embedded Apache Tomcat 9.0.83** (`tomcat-embed-core`) as its lightweight servlet container. This satisfies the coursework requirement for a lightweight container in the following ways:

1. **No external server installation** — Tomcat is embedded as a Maven dependency (`org.apache.tomcat.embed:tomcat-embed-core:9.0.83`). The application starts a Tomcat instance programmatically in `Main.java` and runs as a self-contained JAR.
2. **Minimal footprint** — Only the `tomcat-embed-core` module is included (no JSP compiler, no WebSocket, no clustering), keeping the deployment artefact small and focused.
3. **Programmatic configuration** — The server port, context path (`/api/v1`), and servlet mappings are all configured in code, requiring no XML deployment descriptors (`web.xml`, `server.xml`).
4. **Servlet compatibility** — Tomcat 9.x implements the Servlet 4.0 specification under the `javax.servlet.*` namespace, which is fully compatible with Jersey 2.41 and the `javax.ws.rs.*` JAX-RS API used throughout the project.
5. **Production-grade threading** — Tomcat's NIO connector provides a robust thread pool for handling concurrent requests, making the service performant even under load.

---

## 2. Build & Launch Instructions

### Prerequisites
- **Java 11** or higher installed (`java -version` to verify)
- **Maven 3.6+** installed (`mvn -version` to verify)

> ⚠️ **Assessor Note**: No IDE is required. All commands below run natively in any terminal (Bash, PowerShell, Command Prompt). Do not use IDE-specific run configurations.

### Step 1 — Clone the Repository
```bash
git clone https://github.com/ImethDewmina128At90/Smart_Campus-API.git
cd Smart_Campus-API
```

### Step 2 — Build the Project
```bash
mvn clean package
```
This compiles the source code, runs any tests, and produces a **fat JAR** (uber-JAR) using the Maven Shade Plugin. The output artifact is located at:
```
target/smart-campus-api-1.0-SNAPSHOT.jar
```

### Step 3 — Launch the Server
```bash
java -jar target/smart-campus-api-1.0-SNAPSHOT.jar
```

On successful startup, you will see:
```
Smart Campus API started on Apache Tomcat.
Discovery endpoint: http://localhost:8080/api/v1
Press ENTER to stop the server...
```

### Step 4 — Verify the API is Running
```bash
curl -X GET http://localhost:8080/api/v1
```

Expected response (HTTP 200):
```json
{
  "name": "Smart Campus Sensor & Room Management API",
  "version": "v1",
  "description": "RESTful API for managing campus rooms and IoT sensors",
  "contact": "admin@smartcampus.ac.uk",
  "resources": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

### Step 5 — Stop the Server
Press **ENTER** in the terminal where the server is running to gracefully shut down Tomcat.

---

### Running with Apache NetBeans IDE

If you prefer to use **Apache NetBeans** instead of the command line, follow the steps below.

#### Prerequisites
- **Apache NetBeans 12.x or later** (download from [https://netbeans.apache.org](https://netbeans.apache.org))
- **JDK 11** or higher configured in NetBeans (verify under *Tools → Java Platforms*)
- **Maven** bundled with NetBeans is sufficient — no separate Maven installation is required

#### Step 1 — Open the Project
1. Launch **NetBeans**.
2. Go to **File → Open Project…** (or press `Ctrl+Shift+O`).
3. Navigate to the cloned `Smart_Campus-API` folder and select it.
4. NetBeans will automatically detect the `pom.xml` and recognise it as a **Maven** project. Click **Open Project**.

> 💡 **Tip**: If NetBeans prompts you to "Trust this project", click **Trust and Open**.

#### Step 2 — Resolve Dependencies
1. In the **Projects** panel (left side), right-click on the project name **smart-campus-api**.
2. Select **Build with Dependencies** (or press `Shift+F11`).
3. NetBeans will download all Maven dependencies (Jersey, Tomcat, Jackson, etc.) from Maven Central and compile the project.
4. Wait for the **BUILD SUCCESS** message in the Output window.

#### Step 3 — Set the Main Class
1. Right-click the project → **Properties**.
2. In the left panel, select **Run**.
3. Set the **Main Class** to:
   ```
   com.smartcampus.Main
   ```
4. Click **OK**.

> ⚠️ **Note**: This step is usually auto-detected from the `pom.xml` shade plugin configuration. If `com.smartcampus.Main` already appears, no changes are needed.

#### Step 4 — Run the Project
1. Click the **Run Project** button (green ▶ icon in the toolbar) or press `F6`.
2. The **Output** window at the bottom will display:
   ```
   Smart Campus API started on Apache Tomcat.
   Discovery endpoint: http://localhost:8080/api/v1
   Press ENTER to stop the server...
   ```
3. The API is now running and accessible at `http://localhost:8080/api/v1`.

#### Step 5 — Test the API
You can test endpoints using:
- **A web browser** — navigate to `http://localhost:8080/api/v1` to see the discovery JSON.
- **Postman** or **curl** — use the sample commands from [Section 3](#3-sample-curl-commands).

#### Step 6 — Stop the Server
- Click the **Stop** button (red ■ icon) in the NetBeans Output window to terminate the server process.

---

## 3. Sample curl Commands

Below are executable `curl` commands demonstrating successful interactions across all API sections. Run these **in order** — later commands depend on data created by earlier ones.

### 3.1 Discovery Endpoint — `GET /api/v1`
```bash
# Purpose: Verify the API is running and discover available resources (HATEOAS)
# Expected: HTTP 200 with API metadata and resource links
curl -X GET http://localhost:8080/api/v1
```

### 3.2 Create a Room — `POST /api/v1/rooms`
```bash
# Purpose: Create a new campus room
# Expected: HTTP 201 Created with the room object
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"LIB-301","name":"Library Quiet Study","capacity":50}'
```

### 3.3 Register a Sensor — `POST /api/v1/sensors`
```bash
# Purpose: Register a CO2 sensor assigned to room LIB-301
# Expected: HTTP 201 Created with the sensor object
# Note: Room LIB-301 must exist first, otherwise returns HTTP 422
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"CO2-001","type":"CO2","status":"ACTIVE","currentValue":420.0,"roomId":"LIB-301"}'
```

### 3.4 Get Sensors Filtered by Type — `GET /api/v1/sensors?type=CO2`
```bash
# Purpose: Retrieve only CO2 sensors using @QueryParam filtering
# Expected: HTTP 200 with an array containing CO2-001
curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2"
```

### 3.5 Append a Sensor Reading — `POST /api/v1/sensors/{id}/readings`
```bash
# Purpose: Record a new CO2 measurement for sensor CO2-001
# Expected: HTTP 201 Created with the reading object
# Side effect: The sensor's currentValue is updated to 455.2
curl -X POST http://localhost:8080/api/v1/sensors/CO2-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":455.2}'
```

### 3.6 Delete a Room with Sensors (409 Conflict) — `DELETE /api/v1/rooms/{id}`
```bash
# Purpose: Attempt to delete a room that still has sensors assigned
# Expected: HTTP 409 Conflict — business rule prevents orphaned sensors
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

Expected error response:
```json
{
  "error": "Conflict",
  "message": "Room 'LIB-301' cannot be deleted. It still has 1 sensor(s) assigned. Please remove all sensors before deleting the room."
}
```

### 3.7 Get All Rooms — `GET /api/v1/rooms`
```bash
# Purpose: Retrieve all rooms currently stored in the system
# Expected: HTTP 200 with an array of room objects
curl -X GET http://localhost:8080/api/v1/rooms
```

---

## 4. Conceptual Report

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

---

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

---

### Video Demonstration
A video demonstration of the API functionality via Postman testing is available via the BlackBoard submission link as required by the coursework specification.

---

#### This project is submitted as part of the 5COSC022W Client-Server Architectures coursework at the University of Westminster. All rights reserved

### UOW_ID=w2153254 , IIT_ID =20240076 
