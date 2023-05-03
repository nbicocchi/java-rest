package com.nbicocchi.rest.intermediate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.Clock;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

import static spark.Spark.get;
import static spark.Spark.port;

public class Server {
    public static Map<String, String> map = new HashMap<>(Map.of(
            "rome", "Europe/Rome",
            "new_york", "America/New_York",
            "montevideo", "America/Montevideo",
            "perth", "Australia/Perth"
    ));

    public void run() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        // Start embedded server at this port
        port(8080);

        // Configure resources
        get("/timezone/:id", (request, response) -> {
            String zoneId = request.params(":id");
            LocalTime time = LocalTime.now(Clock.system(ZoneId.of(map.get(zoneId))));
            return mapper.writeValueAsString(time);
        });
    }

    public static void main(String[] args) {
        new Server().run();
    }
}