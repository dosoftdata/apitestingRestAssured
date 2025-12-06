package com.apitesting.core.testData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class initGenerator {
  public static void main(String[] args) throws IOException {
    Path resultsDir = Paths.get("reports");
    Files.createDirectories(resultsDir);
    Properties props = new Properties();
    props.setProperty("Environment", System.getProperty("env", "dev"));
    props.setProperty("OS", System.getProperty("os.name"));
    props.setProperty("Java Version", System.getProperty("java.version"));

    File envFile = new File(resultsDir.toFile(), "environment.properties");
    try (FileOutputStream fos = new FileOutputStream(envFile)) {
      props.store(fos, "Allure Environment Properties");
      log.info("Allure environment.properties created.");
    } catch (IOException e) { e.printStackTrace(); }

    try {
      log.info("✅ Staring process testdata.");
      DynamicDataPreProcessor td = new DynamicDataPreProcessor();
      td.run();
      log.info("✅ end process testdata.");
    }catch ( RuntimeException e){
      log.info(e.getMessage());
    }

  }
}
