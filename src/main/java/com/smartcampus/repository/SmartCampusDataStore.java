package com.smartcampus.repository;
//imports
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

// Storing data in memory instead of a database as required by the brief
public class SmartCampusDataStore {

    public static final Map<String, Room> rooms =
            Collections.synchronizedMap(new HashMap<>());

    public static final Map<String, Sensor> sensors =
            Collections.synchronizedMap(new HashMap<>());

    public static final Map<String, List<SensorReading>> readingsBySensor =
            Collections.synchronizedMap(new HashMap<>());

    private SmartCampusDataStore() {
    }
}