package com.smartcampus.mapper;

import com.smartcampus.exception.LinkedResourceNotFoundException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {

        String errorResponse = String.format(
                "{\"error\":\"%s\"}",
                exception.getMessage()
        );

        return Response.status(Response.Status.UNPROCESSABLE_ENTITY)
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}