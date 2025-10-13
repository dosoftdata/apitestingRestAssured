package com.apitesting.config;

import com.apitesting.dsl.ScenarioContext;
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
      if (result instanceof Map) {
        ((Map<?, ?>) result).forEach((k, v) -> ScenarioContext.set(k.toString(), v));
      }

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
              .getScriptEngine("--language=es6", "--no-deprecation-warning",
                      "--strict=false", "--allow-all-access");
    }
  }
}
