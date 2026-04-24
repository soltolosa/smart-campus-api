package com.smartcampus.mapper;

import com.smartcampus.exception.RoomNotEmptyException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

// mapper class to handle the RoomNotEmptyException
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        String errorResponse = String.format(
                "{\"error\":\"%s\"}",
                exception.getMessage()
        );
        // returns HTTP 409 status code
        return Response.status(Response.Status.CONFLICT)
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}