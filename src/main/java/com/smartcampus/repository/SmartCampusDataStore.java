package com.smartcampus.repository;
//imports
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Storing data in memory instead of a database as required by the brief
//Using HashMaps and Arraylists data structures
public class SmartCampusDataStore {

    public static final Map<String, Room> rooms = new HashMap<>();
    public static final Map<String, Sensor> sensors = new HashMap<>();
    public static final Map<String, List<SensorReading>> readingsBySensor = new HashMap<>();

    private SmartCampusDataStore() {
    }
}