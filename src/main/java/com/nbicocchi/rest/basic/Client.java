package com.nbicocchi.rest.basic;

import kong.unirest.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;

public class Client implements Runnable {
    static Logger logger = LoggerFactory.getLogger(Client.class);

    @Override
    public void run() {
        String[] languages = {"italian", "english", "german"};
        String url = "http://localhost:8080/" + languages[RandomGenerator.getDefault().nextInt(languages.length)];
        String json = Unirest.get(url).asString().getBody();
        logger.info(json);
    }

    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new Client(), 0, 1, TimeUnit.SECONDS);
    }
}