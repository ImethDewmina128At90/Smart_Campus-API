package com.smartcampus;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application entry point.
 *
 * The @ApplicationPath annotation sets the base URI for all REST resources.
 * Every resource endpoint will be prefixed with /api/v1.
 *
 * Lifecycle note (for report question):
 * By default, JAX-RS creates a NEW instance of each Resource class per request
 * (per-request lifecycle). This means instance variables are NOT shared between
 * requests. To share in-memory data (like our HashMaps), we must use static
 * fields or a singleton data store — otherwise each request would see an empty map.
 */
@ApplicationPath("/api/v1")
public class App extends Application {
    // No overrides needed — Jersey auto-scans packages via ResourceConfig in Main.java
}
