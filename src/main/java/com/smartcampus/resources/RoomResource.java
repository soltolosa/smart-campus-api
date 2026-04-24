package com.smartcampus.resources;
//imports for jax-rs
import java.util.Collection;
import java.util.ArrayList;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
//imports
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.repository.SmartCampusDataStore;


@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {
    // GET /api/v1/rooms to retrieve all rooms
    @GET
    public Collection<Room> getAllRooms() {
        return new ArrayList<>(SmartCampusDataStore.rooms.values());
    }

    // POST /api/v1/rooms for creating a new room
    @POST
    public Response createRoom(Room room) {
        SmartCampusDataStore.rooms.put(room.getId(), room);

        return Response.status(Response.Status.CREATED)
                .entity(room)
                .build();
    }

    // GET /api/v1/rooms/{roomId} to retrieve a room by its id
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = SmartCampusDataStore.rooms.get(roomId);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Room not found\"}")
                    .build();
        }

        //adding HATEOAS links to the response
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("room", room);

        java.util.Map<String, String> links = new java.util.HashMap<>();
        links.put("self", "/api/v1/rooms/" + roomId);
        links.put("collection", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");

        response.put("links", links);


        return Response.ok(response).build();

    }

    // DELETE /api/v1/rooms/{roomId} to delete a room by its id
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = SmartCampusDataStore.rooms.get(roomId);
       
        //will not delete if room does not exist
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Room not found\"}")
                    .build();
        }
        //will not delete if room still has sensors assigned to it
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                    "Room cannot be deleted because it still has sensors assigned."
            );
        }
       
        SmartCampusDataStore.rooms.remove(roomId);

        return Response.ok("{\"message\":\"Room deleted successfully\"}").build();
    }
}
