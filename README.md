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

4. The API will be available at:
