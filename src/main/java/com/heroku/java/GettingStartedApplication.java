package com.heroku.java;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.sql.DataSource;

import java.security.SecureRandom;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Map;

@SpringBootApplication
@Controller
public class GettingStartedApplication {
    private final DataSource dataSource;

    private static final String AB = "abcdefghijklmnopqrstuvwxyz";
    private static SecureRandom rnd = new SecureRandom();

    String getRandomString() {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    @Autowired
    public GettingStartedApplication(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/database")
    String database(Map<String, Object> model) {
        try (Connection connection = dataSource.getConnection()) {
            final var statement = connection.createStatement();
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ticks (tick timestamp, random_string varchar(30))");
            String randomString = getRandomString();
            statement.executeUpdate(
                    "INSERT INTO ticks VALUES (now(), '" + randomString + "')");

            System.out.println(
                    "Print statement inside of the GettingStartedApplication.database() method. Max Monciardini");

            final var resultSet = statement.executeQuery("SELECT tick, random_string FROM ticks");
            final var output = new ArrayList<>();
            while (resultSet.next()) {
                output.add(
                        "Read from DB: " + resultSet.getTimestamp("tick") + " " + resultSet.getString("random_string"));
            }

            model.put("records", output);
            return "database";

        } catch (Throwable t) {
            model.put("message", t.getMessage());
            return "error";
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(GettingStartedApplication.class, args);
    }
}
