package net.jr.test;

public class TestUtil {

    public static void configureLogging() {
        String logLevel = System.getenv("TESTS_LOG_LEVEL");
        logLevel = logLevel == null ? "warn" : logLevel;
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", logLevel);
    }
}
