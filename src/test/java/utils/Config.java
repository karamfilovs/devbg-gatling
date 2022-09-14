package utils;

import java.time.Duration;

public class Config {
    public static final Duration DURATION = Duration.ofSeconds(Long.parseLong(System.getProperty("duration", "1")));
    public static final int USERS = Integer.parseInt(System.getProperty("users", "1"));
    public static final String USERNAME = System.getProperty("username", "admin");
    public static final String PASSWORD = System.getProperty("password", "admin");
    public static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:9966");
}
