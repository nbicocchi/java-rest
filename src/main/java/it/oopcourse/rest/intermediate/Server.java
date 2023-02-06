package it.oopcourse.rest.intermediate;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

import static spark.Spark.get;
import static spark.Spark.port;

public class Server {
    ObjectMapper mapper;
    Map<String, String> map = new HashMap<>(Map.of(
            "rome", "Europe/Rome",
            "delhi", "India/Delhi",
            "new_york", "America/New_York",
            "montevideo", "America/Montevideo",
            "perth", "Australia/Perth"
    ));

    public void run() {
        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        // Start embedded server at this port
        port(8080);

        // Configure resources
        get("/timezone/:id", (request, response) -> {
            String id = request.params(":id");
            String timezone = map.get(id);
            return mapper.writeValueAsString(getTimeZone(timezone));
        });
    }

    LocalTime getTimeZone(String name) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone(name));
        return new LocalTime(name, c.get(Calendar.HOUR), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
    }

    public static void main(String[] args) {
        new Server().run();
    }
}