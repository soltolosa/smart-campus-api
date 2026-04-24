package com.smartcampus.model;

// model representing asensor reading
public class SensorReading {

    private String id; // Unique reading event ID
    private long timestamp; // Epoch time (ms) when the reading was captured
    private double value; // The actual metric value recorded by the hardware

    // default constructor needed for JSON
    public SensorReading() {
    }

    // parameterised constructor
    public SensorReading(String id, long timestamp, double value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
    }

    //getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}