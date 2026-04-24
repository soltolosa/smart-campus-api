package com.smartcampus.mapper;

import com.smartcampus.exception.SensorUnavailableException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
// mapper class to handle the SensorUnavailableException
@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        String errorResponse = String.format(
                "{\"error\":\"%s\"}",
                exception.getMessage()
        );
        // returns HTTP 403 status code
        return Response.status(Response.Status.FORBIDDEN)
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}