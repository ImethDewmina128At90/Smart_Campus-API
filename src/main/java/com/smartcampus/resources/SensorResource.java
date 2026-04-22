package com.smartcampus.resources;

import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Sensor Resource — manages /api/v1/sensors
 *
 * Handles sensor registration, retrieval with optional type filtering,
 * and enforces referential integrity with rooms.
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    /**
     * GET /api/v1/sensors
     * GET /api/v1/sensors?type=CO2
     *
     * Returns all sensors, or filters by type if query param is provided.
     *
     * Query param approach is preferred over path-based filtering (e.g. /sensors/type/CO2)
     * because query parameters are designed for optional filtering of collections.
     * Path parameters imply a specific resource identity, not a search criterion.
     * Using @QueryParam keeps the resource URL clean and RESTful.
     */
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> result = new ArrayList<>(DataStore.sensors.values());

        if (type != null && !type.isBlank()) {
            result = result.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }

        return Response.ok(result).build();
    }

    /**
     * POST /api/v1/sensors
     * Registers a new sensor.
     *
     * Validates that the referenced roomId exists before creating the sensor.
     * If roomId does not exist, returns 422 Unprocessable Entity.
     *
     * @Consumes(APPLICATION_JSON) means JAX-RS expects JSON input.
     * If a client sends text/plain or application/xml, JAX-RS returns
     * 415 Unsupported Media Type automatically — the method is never invoked.
     */
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null || sensor.getId().isBlank()) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Bad Request");
            error.put("message", "Sensor ID is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        if (DataStore.sensors.containsKey(sensor.getId())) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Conflict");
            error.put("message", "Sensor with ID '" + sensor.getId() + "' already exists.");
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        }

        // Validate that the referenced room exists
        if (sensor.getRoomId() == null || !DataStore.rooms.containsKey(sensor.getRoomId())) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Unprocessable Entity");
            error.put("message", "Room with ID '" + sensor.getRoomId() + "' does not exist. " +
                    "Please create the room before registering a sensor in it.");
            return Response.status(422).entity(error).build();
        }

        // Save sensor
        DataStore.sensors.put(sensor.getId(), sensor);

        // Link sensor ID to the room
        DataStore.rooms.get(sensor.getRoomId()).getSensorIds().add(sensor.getId());

        // Initialise empty readings list for this sensor
        DataStore.readings.put(sensor.getId(), new ArrayList<>());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Sensor registered successfully.");
        response.put("sensor", sensor);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    /**
     * GET /api/v1/sensors/{sensorId}
     * Returns a specific sensor by ID.
     */
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.sensors.get(sensorId);

        if (sensor == null) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Not Found");
            error.put("message", "Sensor with ID '" + sensorId + "' does not exist.");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }

        return Response.ok(sensor).build();
    }

    /**
     * Sub-resource locator for sensor readings.
     * Delegates /api/v1/sensors/{sensorId}/readings to SensorReadingResource.
     *
     * This pattern separates concerns — SensorReadingResource handles all
     * reading logic independently, keeping this class clean and focused.
     * It avoids one massive controller with dozens of methods.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
