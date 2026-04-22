package com.smartcampus.exceptions;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 409 Conflict — Room still has sensors assigned.
 */
@Provider
class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException ex) {
        Map<String, String> error = new LinkedHashMap<>();
        error.put("error", "Conflict");
        error.put("status", "409");
        error.put("message", "Room '" + ex.getRoomId() + "' cannot be deleted. " +
                "It still has active sensors assigned. Remove all sensors first.");
        return Response.status(409).entity(error).build();
    }
}

/**
 * 422 Unprocessable Entity — Referenced roomId does not exist.
 *
 * HTTP 422 is more semantically accurate than 404 here because the request
 * itself is well-formed and the URL is valid — the problem is that a foreign
 * key reference inside the JSON body points to a non-existent resource.
 * A 404 implies the requested URL was not found, which is misleading.
 * A 422 signals that the server understood the request but could not process
 * it due to invalid data relationships within the payload.
 */
@Provider
class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        Map<String, String> error = new LinkedHashMap<>();
        error.put("error", "Unprocessable Entity");
        error.put("status", "422");
        error.put("message", ex.getMessage());
        return Response.status(422).entity(error).build();
    }
}

/**
 * 403 Forbidden — Sensor is in MAINTENANCE and cannot accept readings.
 */
@Provider
class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException ex) {
        Map<String, String> error = new LinkedHashMap<>();
        error.put("error", "Forbidden");
        error.put("status", "403");
        error.put("message", ex.getMessage());
        return Response.status(Response.Status.FORBIDDEN).entity(error).build();
    }
}

/**
 * 500 Internal Server Error — Global catch-all for any unexpected exception.
 *
 * Security rationale: Exposing Java stack traces to API consumers is dangerous
 * because they reveal internal class names, method signatures, library versions,
 * and code structure. An attacker can use this to identify known vulnerabilities
 * in specific library versions, understand the application's internal logic,
 * craft targeted injection or exploitation attempts, and enumerate system paths.
 * This mapper ensures all unexpected errors return a safe, generic message.
 */
@Provider
class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {
        // Log the full details server-side only — never expose to client
        LOGGER.severe("Unexpected error: " + ex.getClass().getName() + " - " + ex.getMessage());

        Map<String, String> error = new LinkedHashMap<>();
        error.put("error", "Internal Server Error");
        error.put("status", "500");
        error.put("message", "An unexpected error occurred. Please contact the administrator.");
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
    }
}
