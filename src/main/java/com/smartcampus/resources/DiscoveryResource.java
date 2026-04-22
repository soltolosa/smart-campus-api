package com.smartcampus.resources;

//imports
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

//Resource class to handle the discovery endpoint
@Path("/")
public class DiscoveryResource {

    //GET method for discovery endpoint
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getDiscovery() {
        Map<String, Object> response = new HashMap<>();
        //API information
        response.put("apiName", "Smart Campus API");
        response.put("version", "v1");
        response.put("contact", "w2081584@westminster.ac.uk");
        response.put("description", "A RESTful API for managing smart campus resources.");
        //Resources in the API
        Map<String, String> resources = new HashMap<>();
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        resources.put("sensorReadings", "/api/v1/sensors/{sensorId}/readings");

        response.put("resources", resources);

        return response;
    }
}