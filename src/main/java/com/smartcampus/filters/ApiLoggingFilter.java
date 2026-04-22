package com.smartcampus.filters;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import java.util.logging.Logger;

/**
 * API Logging Filter — logs every incoming request and outgoing response.
 *
 * Implements both ContainerRequestFilter and ContainerResponseFilter
 * to intercept the full request/response lifecycle.
 *
 * Why use filters for logging instead of Logger.info() in every method?
 * Filters are a cross-cutting concern — they apply to ALL endpoints
 * automatically without modifying any resource class. Adding Logger.info()
 * to every method would violate the DRY principle, clutter business logic,
 * and make it easy to miss new endpoints. Filters centralise this concern
 * in one place, making it easier to maintain, modify, or disable logging
 * without touching resource code.
 */
@Provider
public class ApiLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(ApiLoggingFilter.class.getName());

    /**
     * Logs the HTTP method and URI of every incoming request.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) {
        LOGGER.info(String.format("INCOMING REQUEST  → Method: [%s]  URI: [%s]",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri()));
    }

    /**
     * Logs the HTTP status code of every outgoing response.
     */
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {
        LOGGER.info(String.format("OUTGOING RESPONSE → Method: [%s]  URI: [%s]  Status: [%d]",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri(),
                responseContext.getStatus()));
    }
}
