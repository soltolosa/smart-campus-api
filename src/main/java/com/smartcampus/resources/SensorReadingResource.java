package com.smartcampus.resources;

// importing packages
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.repository.SmartCampusDataStore;
import com.smartcampus.exception.SensorUnavailableException;

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
        return new ArrayList<>(
                SmartCampusDataStore.readingsBySensor
                        .getOrDefault(sensorId, new ArrayList<>())
        );
    }

    // POST /api/v1/sensors/{sensorId}/readings
    @POST
    public Response addReading(SensorReading reading) {

        Sensor sensor = SmartCampusDataStore.sensors.get(sensorId);

        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Sensor not found\"}")
                    .build();
        }

        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor is currently in maintenance and cannot accept readings."
            );
        }

        // generates ID if null in the request body
        if (reading.getId() == null || reading.getId().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }

        // sets a timestamp if empty in the request body
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // adds reading to the correct sensor list
        SmartCampusDataStore.readingsBySensor
                .computeIfAbsent(sensorId, k -> java.util.Collections.synchronizedList(new ArrayList<>()))
                .add(reading);

        // updates the sensor's current value with the latest reading
            sensor.setCurrentValue(reading.getValue());


        //returns created reading
        return Response.status(Response.Status.CREATED)
                .entity(reading)
                .build();
    }
}