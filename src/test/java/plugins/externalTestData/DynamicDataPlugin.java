package plugins.externalTestData;

import com.apitesting.core.helpers.DBManager;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestRunStarted;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DynamicDataPlugin implements ConcurrentEventListener {

  private final TestDataRepository dataRepo = TestDataRepository.getInstance();
  private final DBManager dbManager = DBManager.getInstance();

  @Override
  public void setEventPublisher(EventPublisher publisher) {
    // Hook at the earliest moment, before feature parsing
    publisher.registerHandlerFor(TestRunStarted.class, event -> updateAllFeatureFiles());
  }

  /** Scan all .feature files and replace Examples before parsing */
  private void updateAllFeatureFiles() {
    File featureDir = new File("src/test/resources/features");
    List<File> features = findFeatureFiles(featureDir);

    for (File f : features) {
      try {
        String source = new String(java.nio.file.Files.readAllBytes(f.toPath()));
        if (source.contains("@DataSource:")) {
          updateFeature(f.toURI(), source);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private List<File> findFeatureFiles(File dir) {
    List<File> list = new ArrayList<>();
    if (dir.exists()) {
      for (File f : Objects.requireNonNull(dir.listFiles())) {
        if (f.isDirectory()) list.addAll(findFeatureFiles(f));
        else if (f.getName().endsWith(".feature")) list.add(f);
      }
    }
    return list;
  }

  private void updateFeature(URI uri, String source) {
    String featureName = extractFeatureName(source);
    String dataSourceTag = extractDataSourceTag(source);

    if (dataSourceTag == null) return;

    String sourceType = dataSourceTag.replace("@DataSource:", "").trim().toLowerCase();
    System.out.println("📂 [DynamicDataPlugin] Preparing test data for '" + featureName + "' (" + sourceType + ")");

    Map<String, List<Map<String, String>>> allData = dataRepo.loadData(sourceType);

    for (String scenarioName : extractScenarioOutlines(source)) {
      List<Map<String, String>> rows = allData.getOrDefault(featureName + "::" + scenarioName, new ArrayList<>());
      if (!rows.isEmpty()) {
        try {
          replaceExamplesInFeature(uri.toString(), scenarioName, rows);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  private String extractFeatureName(String source) {
    for (String line : source.split("\\R")) {
      if (line.trim().startsWith("Feature:")) {
        return line.replace("Feature:", "").trim();
      }
    }
    return "Unnamed Feature";
  }

  private String extractDataSourceTag(String source) {
    for (String line : source.split("\\R")) {
      if (line.trim().startsWith("@DataSource:")) {
        return line.trim();
      }
    }
    return null;
  }

  private List<String> extractScenarioOutlines(String source) {
    List<String> list = new ArrayList<>();
    for (String line : source.split("\\R")) {
      if (line.trim().startsWith("Scenario Outline:")) {
        list.add(line.replace("Scenario Outline:", "").trim());
      }
    }
    return list;
  }

  private void replaceExamplesInFeature(String uri, String scenarioName, List<Map<String, String>> rows) throws IOException {
    File file;
    try {
      file = Paths.get(URI.create(uri)).toFile();
    } catch (IllegalArgumentException e) {
      file = new File(uri);
    }

    String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
    Pattern pattern = Pattern.compile("(?s)(Scenario Outline:\\s*" + Pattern.quote(scenarioName) + ".*?)(?=\\n\\s*Scenario|\\Z)");
    Matcher matcher = pattern.matcher(content);

    StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      String block = matcher.group(1);
      block = block.replaceAll("(?s)Examples:.*?(?=\\n\\s*Scenario|\\Z)", "").trim();
      String examples = buildExamplesTable(rows);
      matcher.appendReplacement(sb, Matcher.quoteReplacement(block + "\n\n    Examples:\n" + examples + "\n"));
    }
    matcher.appendTail(sb);

    writeFeatureFile(file, sb.toString());
    System.out.println("✅ [DynamicDataPlugin] Updated " + file.getName() + " (Scenario: " + scenarioName + ")");
  }

  private String buildExamplesTable(List<Map<String, String>> rows) {
    if (rows.isEmpty()) return "";

    Set<String> headers = new LinkedHashSet<>(rows.get(0).keySet());
    headers.remove("feature_name");
    headers.remove("scenario_name");

    StringBuilder sb = new StringBuilder();
    sb.append("      | ").append(String.join(" | ", headers)).append(" |\n");

    for (Map<String, String> row : rows) {
      sb.append("      | ");
      sb.append(String.join(" | ", headers.stream()
          .map(h -> row.getOrDefault(h, ""))
          .toList()));
      sb.append(" |\n");
    }
    return sb.toString();
  }

  private void writeFeatureFile(File file, String content) throws IOException {
    File parent = file.getParentFile();
    if (parent != null && !parent.exists()) parent.mkdirs();
    try (FileWriter fw = new FileWriter(file, false)) {
      fw.write(content);
    }
  }
}



