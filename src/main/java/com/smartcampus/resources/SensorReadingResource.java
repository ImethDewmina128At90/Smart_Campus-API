package com.smartcampus.resources;

import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SensorReadingResource — sub-resource for /api/v1/sensors/{sensorId}/readings
 *
 * Handles historical reading log for a specific sensor.
 * This class is instantiated by SensorResource via the sub-resource locator pattern.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings
     * Returns all historical readings for this sensor.
     */
    @GET
    public Response getReadings() {
        if (!DataStore.sensors.containsKey(sensorId)) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Not Found");
            error.put("message", "Sensor with ID '" + sensorId + "' does not exist.");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }

        List<SensorReading> sensorReadings = DataStore.readings.getOrDefault(sensorId, new ArrayList<>());
        return Response.ok(sensorReadings).build();
    }

    /**
     * POST /api/v1/sensors/{sensorId}/readings
     * Appends a new reading for this sensor.
     *
     * Side effect: Updates the currentValue on the parent Sensor object
     * to keep data consistent across the API.
     *
     * Returns 403 if the sensor status is MAINTENANCE — it cannot accept readings.
     */
    @POST
    public Response addReading(SensorReading reading) {
        if (!DataStore.sensors.containsKey(sensorId)) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Not Found");
            error.put("message", "Sensor with ID '" + sensorId + "' does not exist.");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }

        // Check sensor is not in MAINTENANCE
        String status = DataStore.sensors.get(sensorId).getStatus();
        if ("MAINTENANCE".equalsIgnoreCase(status)) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Forbidden");
            error.put("message", "Sensor '" + sensorId + "' is currently under MAINTENANCE " +
                    "and cannot accept new readings.");
            return Response.status(Response.Status.FORBIDDEN).entity(error).build();
        }

        // Auto-generate ID and timestamp if not provided
        if (reading.getId() == null) {
            reading.setId(java.util.UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Save reading
        DataStore.readings.get(sensorId).add(reading);

        // Side effect: update currentValue on parent sensor
        DataStore.sensors.get(sensorId).setCurrentValue(reading.getValue());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Reading recorded successfully.");
        response.put("reading", reading);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }
}
