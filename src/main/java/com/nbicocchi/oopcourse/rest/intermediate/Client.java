package com.nbicocchi.oopcourse.rest.intermediate;

import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;

public class Client implements Runnable {
    static Logger logger = LoggerFactory.getLogger(Client.class);
    ObjectMapper mapper = new ObjectMapper();
    RandomGenerator rnd = new Random();

    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new Client(), 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        String[] cities = {"rome", "delhi", "singapore", "perth", "montevideo"};
        String url = "http://localhost:8080/timezone/" + cities[rnd.nextInt(cities.length)];
        String json = Unirest.get(url)
                .asString()
                .getBody();

        try {
            logger.info(json);
            LocalTime localTime = mapper.readValue(json, LocalTime.class);
            logger.info(localTime.toString());
        } catch (IOException ignored) {}
    }
}