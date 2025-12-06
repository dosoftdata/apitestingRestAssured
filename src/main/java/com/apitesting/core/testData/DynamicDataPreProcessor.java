package com.apitesting.core.testData;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/**
 * Fully automatic test data preprocessor.
 * - Walks all feature files recursively under src/test/resources/features
 * - Detects @DataSource tags (@DataSource:csv, @DataSource:excel, @DataSource:db)
 * - Loads data for all features/scenarios from unified repository
 * - Rewrites each feature file with updated Example tables
 */
@Slf4j
public class DynamicDataPreProcessor {

  private final TestDataRepository dataRepo = TestDataRepository.getInstance();

  /**
   * Entry point — run this before executing Cucumber
   */
  public void run() {
    File featureDir = new File("src/test/resources/Suites");
    List<File> features = findFeatureFiles(featureDir);

    // Load all test data once (supports CSV, Excel, DB — implementation in TestDataRepository)
    Map<String, List<Map<String, String>>> allData = dataRepo.loadData("csv");

    for (File f : features) {
      try {
        String source = Files.readString(f.toPath());
        String featureName = extractFeatureName(source);
        String tag = extractDataSourceTag(source);
        if (tag == null) continue; // skip if no @DataSource tag

        System.out.println("🔧 [PreProcessor] Updating feature: " + featureName);
        updateFeatureFile(f, featureName, allData);
      } catch (Exception e) {
        System.err.println("❌ [PreProcessor] Failed updating " + f.getName() + ": " + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  /**
   * Recursively find all .feature files under a directory
   */
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

  /**
   * Extract the "Feature:" line
   */
  private String extractFeatureName(String source) {
    for (String line : source.split("\\R")) {
      if (line.trim().startsWith("Feature:")) {
        return line.replace("Feature:", "").trim();
      }
    }
    return "Unnamed Feature";
  }

  /**
   * Detect @DataSource:csv, @DataSource:excel, or @DataSource:db
   */
  private String extractDataSourceTag(String source) {
    for (String line : source.split("\\R")) {
      if (line.trim().startsWith("@DataSource:")) {
        return line.trim();
      }
    }
    return null;
  }

  /**
   * Update Examples sections of all Scenario Outlines in the feature
   */
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

  /**
   * Find all Scenario Outline titles
   */
  private List<String> extractScenarioOutlines(String source) {
    List<String> list = new ArrayList<>();
    for (String line : source.split("\\R")) {
      if (line.trim().startsWith("Scenario Outline:")) {
        list.add(line.replace("Scenario Outline:", "").trim());
      }
    }
    return list;
  }

  /**
   * Replace or insert Examples table for a given scenario
   */
  private String replaceExamples(String content, String scenario, List<Map<String, String>> rows) {
    Pattern pattern = Pattern.compile("(?s)(Scenario Outline:\\s*" + Pattern.quote(scenario) + ".*?)(?=\\n\\s*Scenario|\\Z)");

    Matcher matcher = pattern.matcher(content);
    StringBuffer sb = new StringBuffer();

    while (matcher.find()) {
      String block = matcher.group(1);

      // Extract placeholders actually used in this scenario
      Set<String> usedPlaceholders = extractUsedPlaceholders(block);

      // Remove any old Examples table
      block = block.replaceAll("(?s)Examples:.*?(?=\\n\\s*Scenario|\\Z)", "").trim();

      // Filter test data columns to match placeholders
      List<Map<String, String>> filteredRows = filterColumns(rows, usedPlaceholders);

      // Build new Examples section
      String examples = buildExamplesTable(filteredRows, usedPlaceholders);

      matcher.appendReplacement(sb, Matcher.quoteReplacement(block + "\n\n    Examples:\n" + examples + "\n"));
    }

    matcher.appendTail(sb);
    return sb.toString()
        .replaceAll("(?m)^[ \\t]*\r?\n{3,}", "\n\n")
        .trim() + "\n";
  }

  /**
   * Extract all <placeholders> used in a Scenario Outline
   */
  private Set<String> extractUsedPlaceholders(String scenarioBlock) {
    Set<String> placeholders = new LinkedHashSet<>();
    Matcher m = Pattern.compile("<(.*?)>").matcher(scenarioBlock);
    while (m.find()) {
      placeholders.add(m.group(1).trim());
    }
    return placeholders;
  }

  /**
   * Keep only the data columns used in this scenario
   */
  private List<Map<String, String>> filterColumns(List<Map<String, String>> rows, Set<String> usedCols) {
    List<Map<String, String>> filtered = new ArrayList<>();
    for (Map<String, String> row : rows) {
      Map<String, String> newRow = new LinkedHashMap<>();
      for (String key : usedCols) {
        if (row.containsKey(key)) {
          newRow.put(key, row.get(key));
        }
      }
      filtered.add(newRow);
    }
    return filtered;
  }

  /**
   * Build the Examples table dynamically
   */
  private String buildExamplesTable(List<Map<String, String>> rows, Set<String> usedCols) {
    if (rows.isEmpty() || usedCols.isEmpty()) return "";

    StringBuilder sb = new StringBuilder();
    sb.append("      | ").append(String.join(" | ", usedCols)).append(" |\n");

    for (Map<String, String> row : rows) {
      sb.append("      | ");
      sb.append(String.join(" | ", usedCols.stream()
          .map(h -> Optional.ofNullable(row.get(h)).orElse("").trim())
          .toList()));
      sb.append(" |\n");
    }

    return sb.toString();
  }
}


