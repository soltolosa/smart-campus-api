package com.smartcampus.resources;

// importing packages
import java.util.Collection;
import java.util.stream.Collectors;
//imports for jax-rs
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.repository.SmartCampusDataStore;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    // GET /api/v1/sensors to retrieve all the sensors
    // returns all sensors and allows filtering using query parameter
    @GET
    public Collection<Sensor> getAllSensors(@QueryParam("type") String type) {

        if (type == null || type.trim().isEmpty()) {
        return new java.util.ArrayList<>(SmartCampusDataStore.sensors.values());
    }
       // allows filering by type using query params
        return SmartCampusDataStore.sensors.values()
                .stream()
                .filter(sensor -> sensor.getType() != null
                        && sensor.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    // POST /api/v1/sensors tocreate a new sensor
    // creates a new sensor if the linked room exists
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        Room room = SmartCampusDataStore.rooms.get(sensor.getRoomId());

        if (room == null) {
            throw new LinkedResourceNotFoundException("The specified room does not exist.");
        }

        // stores the sensor in memory
        SmartCampusDataStore.sensors.put(sensor.getId(), sensor);

        // adds sensor id to the room's list of sensors
        room.getSensorIds().add(sensor.getId());

        // creates an empty list of readings for the new sensor
        SmartCampusDataStore.readingsBySensor.put(
                sensor.getId(),
                java.util.Collections.synchronizedList(new java.util.ArrayList<>())
        );
        return Response.status(Response.Status.CREATED)
                .entity(sensor)
                .build();
    }

    //sub resource locator for sensor readings
    //returns an instance of SensorReadingResource class
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}