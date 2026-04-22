package com.smartcampus.resources;

// importing packages
import com.smartcampus.model.SensorReading;
import com.smartcampus.repository.SmartCampusDataStore;

// imports for jax rs
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private String sensorId;

    // constructor used by sub-resource locator
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET /api/v1/sensors/{sensorId}/readings
    @GET
    public List<SensorReading> getAllReadings() {
        return SmartCampusDataStore.readingsBySensor
                .getOrDefault(sensorId, new ArrayList<>());
    }

    // POST /api/v1/sensors/{sensorId}/readings
    @POST
    public Response addReading(SensorReading reading) {

        // generate ID if not provided
        if (reading.getId() == null || reading.getId().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }

        // set timestamp if not provided
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // add reading to the correct sensor list
        SmartCampusDataStore.readingsBySensor
                .computeIfAbsent(sensorId, k -> new ArrayList<>())
                .add(reading);

        return Response.status(Response.Status.CREATED)
                .entity(reading)
                .build();
    }
}