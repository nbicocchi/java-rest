package com.nbicocchi.oopcourse.rest.advanced;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.Unirest;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UIController {
    List<Plane> planes = new ArrayList<>();
    int selected = 0;

    public UIController() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        String jsonInput  = Unirest.get("http://localhost:8080/plane/all")
                .asString()
                .getBody();
        planes = mapper.readValue(jsonInput, new TypeReference<List<Plane>>(){});
    }

    public Plane getSelected() {
        return planes.get(selected);
    }

    public void first() {
        selected = 0;
    }

    public void last() {
        selected = planes.size() - 1;

    }

    public void next() {
        selected = Math.min(planes.size() - 1, selected + 1);
    }

    public void previous() {
        selected = Math.max(0, selected - 1);
    }

    public void insert(Plane plane) {
        String jsonResponse =  Unirest.post("http://localhost:8080/plane/add")
                .fields(Map.of("name", plane.getName(),
                                "length", plane.getLength(),
                                "wingspan", plane.getWingspan(),
                                "firstFlight", plane.getFirstFlight(),
                                "category", plane.getCategory()))
                .asString()
                .getBody();
        planes.add(plane);
   }

    public void remove() {
        String jsonResponse = Unirest.delete("http://localhost:8080/plane/{id}")
                .routeParam("id", getSelected().getUUID().toString())
                .asString()
                .getBody();
        planes.remove(getSelected());
    }

    public void update(Plane plane) {
        String jsonResponse = Unirest.put("http://localhost:8080/plane/{id}")
                .routeParam("id", getSelected().getUUID().toString())
                .field("name", plane.getName())
                .field("length", String.valueOf(plane.getLength()))
                .field("wingspan", String.valueOf(plane.getWingspan()))
                .field("firstFlight", plane.getFirstFlight().toString())
                .field("category", plane.getCategory())
                .asString()
                .getBody();
    }

    public void setName(String name) {
        getSelected().setName(name);
        update(getSelected());
    }

    public void setLength(double length) {
        getSelected().setLength(length);
        update(getSelected());
    }

    public void setWingspan(double wingspan) {
        getSelected().setWingspan(wingspan);
        update(getSelected());
    }

    public void setFirstFlight(LocalDate firstFlight) {
        getSelected().setFirstFlight(firstFlight);
        update(getSelected());

    }

    public void setCategory(String category) {
        getSelected().setCategory(category);
        update(getSelected());
    }
}
