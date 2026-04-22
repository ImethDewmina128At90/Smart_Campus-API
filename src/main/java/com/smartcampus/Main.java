package com.smartcampus;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jackson.JacksonFeature;

import java.io.File;

public class Main {

    public static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(PORT);

        // Required for Tomcat 9.x — activate the default connector
        tomcat.getConnector();

        // Create a minimal web-app context (no actual webapp directory needed)
        String docBase = new File(System.getProperty("java.io.tmpdir")).getAbsolutePath();
        Context context = tomcat.addContext("/api/v1", docBase);

        // Build the Jersey ResourceConfig
        ResourceConfig config = new ResourceConfig()
                .packages(
                    "com.smartcampus.resources",
                    "com.smartcampus.exceptions",
                    "com.smartcampus.filters"
                )
                .register(JacksonFeature.class);

        // Register the Jersey ServletContainer with Tomcat
        Tomcat.addServlet(context, "jersey-servlet",
                new ServletContainer(config));
        context.addServletMappingDecoded("/*", "jersey-servlet");

        tomcat.start();

        System.out.println("Smart Campus API started on Apache Tomcat.");
        System.out.println("Discovery endpoint: http://localhost:" + PORT + "/api/v1");
        System.out.println("Press ENTER to stop the server...");
        System.in.read();

        tomcat.stop();
        tomcat.destroy();
    }
}