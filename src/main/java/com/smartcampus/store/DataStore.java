package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    // Room storage
    public static final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();

    // Sensor storage
    public static final ConcurrentHashMap<String, Sensor> sensors = new ConcurrentHashMap<>();

    // Sensor readings storage
    public static final ConcurrentHashMap<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

}