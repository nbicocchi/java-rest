package com.nbicocchi.rest.intermediate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kong.unirest.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;

public class Client implements Runnable {
    static Logger logger = LoggerFactory.getLogger(Client.class);

    @Override
    public void run() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        String[] cities = {"rome", "new_york", "perth", "montevideo"};
        String city = cities[RandomGenerator.getDefault().nextInt(cities.length)];
        String url = "http://localhost:8080/timezone/" + city;
        String json = Unirest.get(url)
                .asString()
                .getBody();

        try {
            LocalTime localTime = mapper.readValue(json, LocalTime.class);
            String output = String.format("[%s] (JSON) %s -> (OBJ) %s", city, json, localTime);
            logger.info(output);
        } catch (IOException ignored) {

        }
    }

    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new Client(), 0, 5, TimeUnit.SECONDS);
    }
}