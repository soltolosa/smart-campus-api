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

        return Response.status(422)//Unprocessable Entity
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}