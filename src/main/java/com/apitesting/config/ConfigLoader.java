package com.apitesting.config;

import com.apitesting.dsl.ScenarioContext;
import java.util.List;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.FileReader;
import java.util.Map;

public class ConfigLoader {

  private static boolean loaded = false;

  public static void loadConfig() {
    if (loaded) return;

    try {
      ScriptEngine engine = createScriptEngine();
      if (engine == null) {
        throw new IllegalStateException("No JavaScript engine available for config");
      }

      // Load config.js
      engine.eval(new FileReader("src/test/resources/config.js"));
      Invocable invocable = (Invocable) engine;

      // Call the "fn()" method inside JS
      Object result = invocable.invokeFunction("fn");
//      if (result instanceof Map) {
//        ((Map<?, ?>) result).forEach((k, v) -> ScenarioContext.set(k.toString(), v));
//      }
      // Handle nested structures
      storeNested(result, "");
      loaded = true;
    } catch (Exception e) {
      throw new RuntimeException("Failed to load config.js", e);
    }
  }

  private static ScriptEngine createScriptEngine() {
    String version = System.getProperty("java.version");
    boolean isLegacy = version.startsWith("1.8") || version.startsWith("9") || version.startsWith("10") ||
            version.startsWith("11") || version.startsWith("12") || version.startsWith("13");

    if (isLegacy) {
      // Java 8–13 → use built-in Nashorn
      return new ScriptEngineManager().getEngineByName("nashorn");
    } else {
      // Java 15+ → use external nashorn-core
      return new NashornScriptEngineFactory()
              .getScriptEngine("--language=es6");
    }
  }
  /**
   * Recursively store nested objects and arrays into ScenarioContext.
   * Keys use dot notation for objects and [index] for arrays.
   */
  @SuppressWarnings("unchecked")
  private static void storeNested(Object value, String path) {
    if (value instanceof Map<?, ?> map) {
      // Handle JS object
      map.forEach((k, v) -> {
        String newPath = path.isEmpty() ? k.toString() : path + "." + k;
        storeNested(v, newPath);
      });
    } else if (value instanceof List<?> list) {
      // Handle JS array
      for (int i = 0; i < list.size(); i++) {
        String newPath = path + "[" + i + "]";
        storeNested(list.get(i), newPath);
      }
    } else {
      // Primitive value: store it
      ScenarioContext.set(path, value);
    }
  }
}
