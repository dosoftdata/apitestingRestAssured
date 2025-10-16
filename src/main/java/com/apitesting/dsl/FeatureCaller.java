package com.apitesting.dsl;
import io.cucumber.core.cli.Main;

import java.util.HashMap;
import java.util.Map;

public class FeatureCaller {
  /**
   * Executes another Cucumber feature file and captures shared variables
   * @param featurePath relative path to feature file (e.g. classpath:features/auth.feature)
   * @param args map of variables to pass into the called feature
   * @return map of outputs (shared state)
   */
  public static Map<String, Object> call(String featurePath, Map<String, Object> args) {
    // In a real-world setup you can use PicoContainer/World context to pass state
    // Here we just simulate the call
    Map<String, Object> results = new HashMap<>();

    System.setProperty("called.feature", featurePath);

    try {
      Main.run(new String[]{featurePath}, Thread.currentThread().getContextClassLoader());
    } catch (Exception e) {
      throw new RuntimeException("Failed to call feature: " + featurePath, e);
    }
    // TODO: collect state from ScenarioContext or Hooks
    results.putAll(args);
    return results;
  }
}
