package com.smartcampus.model;
//imports
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

//model class for room in the smart campus API
public class Room {

    private String id; // Unique identifier
    private String name; // Human-readable name
    private int capacity; // Maximum occupancy for safety regulations
    private List<String> sensorIds = Collections.synchronizedList(new ArrayList<>());
    //default constructor needed for JSON
    public Room() {}

    //constructor
    public Room(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.sensorIds = Collections.synchronizedList(new ArrayList<>());
    }

    //getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<String> getSensorIds() {
        return sensorIds;
    }

    public void setSensorIds(List<String> sensorIds) {
        this.sensorIds = Collections.synchronizedList(new ArrayList<>(sensorIds));
    }
}