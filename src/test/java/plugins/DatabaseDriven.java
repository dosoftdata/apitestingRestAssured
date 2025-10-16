package plugins;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestSourceRead;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class DatabaseDriven implements ConcurrentEventListener {

  private final ObjectMapper mapper = new ObjectMapper();
  private final String buildId = System.getenv().getOrDefault("BUILD_ID", "local");
  private final String jdbcUrl = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/testdb");
  private final String jdbcUser = System.getenv().getOrDefault("DB_USER", "testuser");
  private final String jdbcPass = System.getenv().getOrDefault("DB_PASS", "testpass");

  @Override
  public void setEventPublisher(EventPublisher publisher) {
    publisher.registerHandlerFor(TestSourceRead.class, this::onFeatureRead);
  }

  private void onFeatureRead(TestSourceRead event) {
    try {
      String originalSource = event.getSource();
      String featureKey = extractFeatureKeyFromPath(String.valueOf(event.getUri()));
      List<String> lines = new ArrayList<>(Arrays.asList(originalSource.split("\n")));

      // Load all DB examples for this feature
      Map<String, List<Map<String, String>>> dbExamples = loadAllExamplesForFeature(featureKey);
      if (dbExamples.isEmpty()) return;

      for (int i = 0; i < lines.size(); i++) {
        String line = lines.get(i).trim();
        if (line.startsWith("Scenario")) {
          String scenarioName = extractScenarioName(line);
          List<Map<String, String>> examples = dbExamples.getOrDefault(scenarioName, Collections.emptyList());
          if (examples.isEmpty()) continue;

          if (line.startsWith("Scenario Outline")) {
            // Replace original Examples with DB Examples
            int examplesStart = findExamplesStart(lines, i);
            int examplesEnd = findExamplesEnd(lines, examplesStart);
            if (examplesStart != -1 && examplesEnd != -1) {
              for (int j = examplesEnd - 1; j >= examplesStart; j--) {
                lines.remove(j);
              }
            }
            String newExamples = buildExamplesTable(examples);
            int insertPos = examplesStart != -1 ? examplesStart : i + 1;
            lines.addAll(insertPos, Arrays.asList(newExamples.split("\n")));
            System.out.printf("[DBInjector] Replaced Examples for Outline '%s' with %d DB rows%n",
                scenarioName, examples.size());
          } else {
            // Regular Scenario → replace placeholders with first DB row
            int scenarioEnd = findScenarioEnd(lines, i);
            Map<String, String> firstRow = examples.get(0);
            for (int j = i + 1; j < scenarioEnd; j++) {
              lines.set(j, replacePlaceholders(lines.get(j), firstRow));
            }
            System.out.printf("[DBInjector] Replaced placeholders for scenario '%s' using first DB row%n",
                scenarioName);
          }
        }
      }

      // Replace feature source in memory
      String modifiedSource = String.join("\n", lines);
      Field field = event.getClass().getDeclaredField("source");
      field.setAccessible(true);
      field.set(event, modifiedSource);

    } catch (Exception e) {
      System.err.println("[DBInjector] Error processing feature: " + e.getMessage());
      e.printStackTrace(System.err);
    }
  }

  // --- Helper Methods ---

  private String extractFeatureKeyFromPath(String uri) {
    String file = uri.substring(uri.lastIndexOf('/') + 1);
    return file.replace(".feature", "").toUpperCase();
  }

  private String extractScenarioName(String line) {
    return line.replaceFirst("Scenario( Outline)?[:]", "").trim();
  }

  private int findExamplesStart(List<String> lines, int scenarioStart) {
    for (int i = scenarioStart + 1; i < lines.size(); i++) {
      if (lines.get(i).trim().startsWith("Examples:")) return i;
      if (lines.get(i).trim().startsWith("Scenario")) break;
    }
    return -1;
  }

  private int findExamplesEnd(List<String> lines, int examplesStart) {
    if (examplesStart == -1) return -1;
    for (int i = examplesStart + 1; i < lines.size(); i++) {
      String trimmed = lines.get(i).trim();
      if (trimmed.startsWith("Scenario") || trimmed.startsWith("Examples:")) return i;
    }
    return lines.size();
  }

  private int findScenarioEnd(List<String> lines, int startLine) {
    for (int i = startLine + 1; i < lines.size(); i++) {
      if (lines.get(i).trim().startsWith("Scenario")) return i;
    }
    return lines.size();
  }

  private String replacePlaceholders(String line, Map<String, String> data) {
    String updated = line;
    for (Map.Entry<String, String> e : data.entrySet()) {
      updated = updated.replace("<" + e.getKey() + ">", e.getValue());
    }
    return updated;
  }

  private String buildExamplesTable(List<Map<String, String>> examples) {
    if (examples.isEmpty()) return "";
    List<String> headers = new ArrayList<>(examples.get(0).keySet());
    String headerLine = "| " + String.join(" | ", headers) + " |";
    List<String> rows = new ArrayList<>();
    for (Map<String, String> row : examples) {
      rows.add("| " + String.join(" | ", headers.stream().map(h -> row.getOrDefault(h, "")).toList()) + " |");
    }
    List<String> block = new ArrayList<>();
    block.add("    Examples:");
    block.add("      " + headerLine);
    for (String r : rows) block.add("      " + r);
    return String.join("\n", block);
  }

  private Map<String, List<Map<String, String>>> loadAllExamplesForFeature(String featureKey) {
    String sql = """
            SELECT sc.name AS scenario_name, e.example_data
            FROM scenario_examples e
            JOIN scenario_example_sets s ON e.example_set_id = s.id
            JOIN scenarios sc ON s.scenario_id = sc.id
            JOIN features f ON sc.feature_id = f.id
            WHERE f.feature_key = ?
              AND (s.build_id = ? OR s.build_id IS NULL)
            ORDER BY sc.name, e.row_index
        """;

    Map<String, List<Map<String, String>>> result = new LinkedHashMap<>();
    try (Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass);
        PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, featureKey);
      ps.setString(2, buildId);
      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
        String scenarioName = rs.getString("scenario_name");
        Map<String, String> row = mapper.readValue(rs.getString("example_data"),
            new TypeReference<>() {});
        result.computeIfAbsent(scenarioName, k -> new ArrayList<>()).add(row);
      }

    } catch (Exception ex) {
      System.err.println("[DBInjector] Failed to load DB examples: " + ex.getMessage());
    }

    return result;
  }
}


