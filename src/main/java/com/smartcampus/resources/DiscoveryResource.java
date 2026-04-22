package com.smartcampus.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Discovery Endpoint — GET /api/v1
 *
 * Returns API metadata: version, contact, and available resource links.
 * This implements a basic form of HATEOAS (Hypermedia As The Engine Of
 * Application State) — clients can discover all available resources
 * dynamically from this single entry point rather than relying on
 * hard-coded URLs or external documentation.
 */
@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response discover() {

        // Resource links map (HATEOAS-style navigation)
        Map<String, String> links = new LinkedHashMap<>();
        links.put("rooms",   "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");

        // Full API metadata response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("name",        "Smart Campus Sensor & Room Management API");
        response.put("version",     "v1");
        response.put("description", "RESTful API for managing campus rooms and IoT sensors");
        response.put("contact",     "admin@smartcampus.ac.uk");
        response.put("resources",   links);

        return Response.ok(response).build();
    }
}
