package com.smartcampus;
//imports
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

//Main class to start the server
public class Main {

    public static final String BASE_URI = "http://localhost:8080/";

    public static HttpServer startServer() {
        final ResourceConfig rc = new ResourceConfig()
                .packages("com.smartcampus.resources");

        return GrizzlyHttpServerFactory.createHttpServer(
                URI.create(BASE_URI), rc
        );
    }
    
    public static void main(String[] args) throws IOException {
        HttpServer server = startServer();
        System.out.println("Server started at: " + BASE_URI + "api/v1");
        System.out.println("Press Enter to stop...");
        System.in.read();
        server.shutdownNow();
    }
}