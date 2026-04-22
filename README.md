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

4. The API will be available at:http://localhost:8080/api/v1

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

*(Further question answers will be added as each Part is completed)*
