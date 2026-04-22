package com.smartcampus.resources;

// importing packages
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.repository.SmartCampusDataStore;

// imports for jax rs annotations + response handling
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    // GET sensors endpoint
    // Returns all sensors and allows filtering through the query parameter
    @GET
    public Collection<Sensor> getAllSensors(@QueryParam("type") String type) {

        if (type == null || type.trim().isEmpty()) {
            return SmartCampusDataStore.sensors.values();
        }

        return SmartCampusDataStore.sensors.values()
                .stream()
                .filter(sensor -> sensor.getType() != null
                        && sensor.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    // POST sensors endpoint
    // creates a new sensor if the linked room exists
    @POST
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
        SmartCampusDataStore.readingsBySensor.put(sensor.getId(), new java.util.ArrayList<>());

        return Response.status(Response.Status.CREATED)
                .entity(sensor)
                .build();
    }

    //sub resource locator for sensor readings
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}