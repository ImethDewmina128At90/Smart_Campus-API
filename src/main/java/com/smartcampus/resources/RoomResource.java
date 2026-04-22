package com.smartcampus.resources;

import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Room Resource — manages /api/v1/rooms
 *
 * Handles creation, retrieval, and deletion of campus rooms.
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    /**
     * GET /api/v1/rooms
     * Returns all rooms in the system.
     */
    @GET
    public Response getAllRooms() {
        return Response.ok(DataStore.rooms.values()).build();
    }

    /**
     * POST /api/v1/rooms
     * Creates a new room.
     * Returns 409 if a room with the same ID already exists.
     */
    @POST
    public Response createRoom(Room room) {
        if (room == null || room.getId() == null || room.getId().isBlank()) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Bad Request");
            error.put("message", "Room ID is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        if (DataStore.rooms.containsKey(room.getId())) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Conflict");
            error.put("message", "Room with ID '" + room.getId() + "' already exists.");
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        }

        DataStore.rooms.put(room.getId(), room);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Room created successfully.");
        response.put("room", room);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    /**
     * GET /api/v1/rooms/{roomId}
     * Returns a specific room by ID.
     * Returns 404 if the room does not exist.
     */
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);

        if (room == null) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Not Found");
            error.put("message", "Room with ID '" + roomId + "' does not exist.");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }

        return Response.ok(room).build();
    }

    /**
     * DELETE /api/v1/rooms/{roomId}
     * Deletes a room by ID.
     *
     * Business Logic: A room cannot be deleted if it still has sensors assigned.
     * This prevents orphaned sensor data.
     *
     * Idempotency: DELETE is idempotent in REST. However, in this implementation,
     * the first DELETE removes the room and returns 200. Subsequent DELETE requests
     * for the same ID return 404, since the room no longer exists. This is
     * technically not fully idempotent in terms of response, but is safe and
     * predictable — no unintended side effects occur on repeated calls.
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);

        if (room == null) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Not Found");
            error.put("message", "Room with ID '" + roomId + "' does not exist.");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }

        // Business rule: cannot delete a room that still has sensors
        if (!room.getSensorIds().isEmpty()) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Conflict");
            error.put("message", "Room '" + roomId + "' cannot be deleted. " +
                    "It still has " + room.getSensorIds().size() + " sensor(s) assigned. " +
                    "Please remove all sensors before deleting the room.");
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        }

        DataStore.rooms.remove(roomId);

        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "Room '" + roomId + "' deleted successfully.");
        return Response.ok(response).build();
    }
}
