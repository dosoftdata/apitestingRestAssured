package plugins.externalTestData;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.*;

public class DynamicDataPreProcessor {

  private final TestDataRepository dataRepo = TestDataRepository.getInstance();

  public void run() {
    File featureDir = new File("src/test/resources/features");
    List<File> features = findFeatureFiles(featureDir);

    Map<String, List<Map<String, String>>> allData = dataRepo.loadData("csv");
    for (File f : features) {
      try {
        String source = Files.readString(f.toPath());
        String featureName = extractFeatureName(source);
        String tag = extractDataSourceTag(source);
        if (tag == null) continue;

        System.out.println("🔧 [PreProcessor] Updating feature: " + featureName);
        updateFeatureFile(f, featureName, allData);
      } catch (Exception e) {
        System.err.println("❌ [PreProcessor] Failed updating " + f.getName() + ": " + e.getMessage());
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

  private void updateFeatureFile(File file, String featureName, Map<String, List<Map<String, String>>> allData) throws IOException {
    String content = Files.readString(file.toPath());
    List<String> scenarios = extractScenarioOutlines(content);

    for (String scenario : scenarios) {
      List<Map<String, String>> rows = allData.getOrDefault(featureName + "::" + scenario, new ArrayList<>());
      if (!rows.isEmpty()) {
        content = replaceExamples(content, scenario, rows);
      }
    }

    Files.writeString(file.toPath(), content);
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

  private String replaceExamples(String content, String scenario, List<Map<String, String>> rows) {
    Pattern pattern = Pattern.compile("(?s)(Scenario Outline:\\s*" + Pattern.quote(scenario) + ".*?)(?=\\n\\s*Scenario|\\Z)");
    Matcher matcher = pattern.matcher(content);
    StringBuffer sb = new StringBuffer();

    while (matcher.find()) {
      String block = matcher.group(1);
      block = block.replaceAll("(?s)Examples:.*?(?=\\n\\s*Scenario|\\Z)", "").trim();
      String examples = buildExamplesTable(rows);
      matcher.appendReplacement(sb, Matcher.quoteReplacement(block + "\n\n    Examples:\n" + examples + "\n"));
    }
    matcher.appendTail(sb);
    return sb.toString();
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
}

