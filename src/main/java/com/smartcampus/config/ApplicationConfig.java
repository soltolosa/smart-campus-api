package com.smartcampus.config;
//imports
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
//configuration class to se the base path for the api
@ApplicationPath("/api/v1")
public class ApplicationConfig extends Application {
}