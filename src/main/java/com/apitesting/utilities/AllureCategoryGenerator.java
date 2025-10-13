package com.apitesting.utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AllureCategoryGenerator {
  public static void main(String[] args) throws IOException {
    Path resultsDir = Paths.get("reports");
    Files.createDirectories(resultsDir);
    String json = "[\n" +
        "  {\"name\": \"HTTP Error\", \"matchedStatuses\": [\"failed\"], \"messageRegex\": \".*(HTTP 4\\\\d{2}|HTTP 5\\\\d{2}).*\"},\n" +
        "  {\"name\": \"Assertion Failure\", \"matchedStatuses\": [\"failed\"], \"messageRegex\": \".*AssertionError.*\"},\n" +
        "  {\"name\": \"Timeout Error\", \"matchedStatuses\": [\"broken\"], \"messageRegex\": \".*(TimeoutException|ReadTimeout).*\"},\n" +
        "  {\"name\": \"Connection Error\", \"matchedStatuses\": [\"broken\"], \"messageRegex\": \".*(ConnectionError|Connection refused).*\"},\n" +
        "  {\"name\": \"Authentication Error\", \"matchedStatuses\": [\"failed\"], \"messageRegex\": \".*(401|403|Unauthorized).*\"}\n" +
        "]";

    try (FileWriter writer = new FileWriter(resultsDir.resolve("categories.json").toFile())) {
      writer.write(json);
    }

    Properties props = new Properties();
    props.setProperty("Environment", System.getProperty("env", "dev"));
    props.setProperty("OS", System.getProperty("os.name"));
    props.setProperty("Java Version", System.getProperty("java.version"));

    File envFile = new File(resultsDir.toFile(), "environment.properties");
    try (FileOutputStream fos = new FileOutputStream(envFile)) {
      props.store(fos, "Allure Environment Properties");
      log.info("Allure environment.properties created.");
    } catch (IOException e) { e.printStackTrace(); }

    log.info("✅ categories.json generated in allure-results");
  }
}
