package com.smartcampus.store;

import com.smartcampus.model.Room;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton in-memory data store.
 *
 * Because JAX-RS creates a new Resource instance per request,
 * all data must be stored in static fields to persist across requests.
 * ConcurrentHashMap is used to prevent race conditions under concurrent access.
 */
public class DataStore {

    // Room storage — key: roomId, value: Room object
    public static final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();

}
