package com.nbicocchi.rest.advanced;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.nbicocchi.rest.common.UtilsDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static spark.Spark.*;

public class Server {
    HikariDataSource dataSource;
    ObjectMapper mapper;

    static final String SUCCESS = "{status: ok}";
    static final String FAILURE = "{status: failed}";
    static Logger logger = LoggerFactory.getLogger(Server.class);

    public void init() {
        Thread.currentThread().setName("REST-Server");
        initMapper();
        dbConnection();
        run();
    }

    public void initMapper() {
        mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        mapper.findAndRegisterModules();
    }

    private void dbConnection() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(UtilsDB.JDBC_Driver_MySQL);
        config.setJdbcUrl(UtilsDB.JDBC_URL_MySQL);
        config.setLeakDetectionThreshold(2000);
        dataSource = new HikariDataSource(config);
    }

    public void run() {
        // Start embedded server at this port
        port(8080);

        // POST - Add new
        // curl -X POST -d name=xyz -d length=1.0 -d wingspan=1.0 -d firstFlight=2020-01-01 -d category=Airliner
        // http://localhost:8080/plane/add
        post("/plane/add", (request, response) -> {
            try (Connection connection = dataSource.getConnection();
                    PreparedStatement statement = connection.prepareStatement("INSERT INTO planes (uuid, " +
                            "name, length, wingspan, firstFlight, category) VALUES (?, ?, ?, ?, ?, ?)")) {
                statement.setString(1, UUID.randomUUID().toString());
                statement.setString(2, request.queryParams("name"));
                statement.setDouble(3, Double.parseDouble(request.queryParams("length")));
                statement.setDouble(4, Double.parseDouble(request.queryParams("wingspan")));
                statement.setDate(5, Date.valueOf(request.queryParams("firstFlight")));
                statement.setString(6, request.queryParams("category"));
                statement.executeUpdate();
            }
            return mapper.writeValueAsString(SUCCESS);
        });

        // PUT - Update
        // curl -X PUT -d name=abc -d length=1.0 -d wingspan=1.0 -d firstFlight=2000-01-01 -d
        // category=Airliner http://localhost:8080/plane/e2956180-4cb5-4ab4...
        put("/plane/:id", (request, response) -> {
            UUID uuid = UUID.fromString(request.params(":id"));
            if (!isUUIDAvailable(uuid)) {
                response.status(404);
                return mapper.writeValueAsString(FAILURE);
            }
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("UPDATE planes SET " +
                            "name=?, length=?, wingspan=?, firstFlight=?, category=? WHERE uuid=?")) {
                statement.setString(1, request.queryParams("name"));
                statement.setDouble(2, Double.parseDouble(request.queryParams("length")));
                statement.setDouble(3, Double.parseDouble(request.queryParams("wingspan")));
                statement.setDate(4, Date.valueOf(request.queryParams("firstFlight")));
                statement.setString(5, request.queryParams("category"));
                statement.setString(6, uuid.toString());
                statement.executeUpdate();
            }
            return mapper.writeValueAsString(SUCCESS);
        });

        // DELETE - delete
        // curl -X DELETE http://localhost:8080/plane/e2956180-4cb5-4ab4...
        delete("/plane/:id", (request, response) -> {
            UUID uuid = UUID.fromString(request.params(":id"));
            if (!isUUIDAvailable(uuid)) {
                response.status(404);
                return mapper.writeValueAsString(FAILURE);
            }
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("DELETE FROM planes WHERE uuid=?")) {
                statement.setString(1, uuid.toString());
                statement.executeUpdate();
            }
            return mapper.writeValueAsString(SUCCESS);
        });

        // GET - get all
        // curl -X GET http://localhost:8080/plane
        get("/plane/all", (request, response) -> {
            List<Plane> planes = new ArrayList<>();
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT * FROM planes")) {
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        planes.add(new Plane(UUID.fromString(rs.getString("uuid")), rs.getString("name"), rs.getDouble("length"), rs.getDouble("wingspan"), UtilsDB.convertSQLDateToLocalDate(rs.getDate("firstFlight")), rs.getString("category")));
                    }
                }
            }
            return mapper.writeValueAsString(planes);
        });

        // GET - get by length
        // curl -X GET "http://localhost:8080/plane/length?min=30&max=40"
        get("/plane/length", (request, response) -> {
            List<Plane> planes = new ArrayList<>();
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT * FROM planes WHERE length BETWEEN ? AND ? ORDER BY length")) {
                statement.setString(1, request.queryParams("min"));
                statement.setString(2, request.queryParams("max"));
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        planes.add(new Plane(UUID.fromString(rs.getString("uuid")), rs.getString("name"), rs.getDouble("length"), rs.getDouble("wingspan"), UtilsDB.convertSQLDateToLocalDate(rs.getDate("firstFlight")), rs.getString("category")));
                    }
                }
            }
            return mapper.writeValueAsString(planes);
        });

        // GET - get by id
        // curl -X GET "http://localhost:8080/plane/e2956180-4cb5-4ab4..."
        get("/plane/:id", (request, response) -> {
            UUID uuid = UUID.fromString(request.params(":id"));
            if (!isUUIDAvailable(uuid)) {
                response.status(404);
                return mapper.writeValueAsString(FAILURE);
            }
            Plane plane;
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT * FROM planes WHERE uuid=?")) {
                statement.setString(1, uuid.toString());
                try (ResultSet rs = statement.executeQuery()) {
                    plane = new Plane(UUID.fromString(rs.getString("uuid")), rs.getString("name"), rs.getDouble(
                            "length"), rs.getDouble("wingspan"), UtilsDB.convertSQLDateToLocalDate(rs.getDate(
                                    "firstFlight")), rs.getString("category"));
                }
            }
            return mapper.writeValueAsString(plane);
        });
    }

    private boolean isUUIDAvailable(UUID uuid) throws SQLException {
        boolean isUUIDAvailable;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM planes WHERE uuid=?")) {
            statement.setString(1, uuid.toString());
            try (ResultSet rs = statement.executeQuery()) {
                isUUIDAvailable = rs.next();
            }
        }
        return isUUIDAvailable;
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.init();
    }
}