package com.apitesting.core.testData;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

/**
 * Centralized Test Data Repository.
 * Supports CSV, Excel, and Database sources.
 * Automatically caches loaded data and resolves resources from both
 * filesystem and classpath.
 * TestDataRepository repo = TestDataRepository.getInstance();
 * Map<String, List<Map<String, String>>> data = repo.loadData("db");
 * // or "csv" / "excel"
 *
 * List<Map<String, String>> scenarioData = data.get("TC001::Add item to inventory");
 * System.out.println(scenarioData);
 * -Ddb.url=jdbc:sqlite:src/test/resources/testdata/testdata.db
 * -Ddb.table=testdata
 * -Ddb.url=jdbc:postgresql://localhost:5432/testdb
 * -Ddb.user=tester
 * -Ddb.pass=secret
 * -Ddb.table=testdata
 */
public class TestDataRepository {

  private static TestDataRepository instance;
  private Map<String, List<Map<String, String>>> cachedData;

  // Default file location (can be overridden by -Dtestdata.file=...)
  private final String filePath = System.getProperty(
      "testdata.file", "src/test/resources/testdata/testdata.csv");

  // Database system properties
  private final String dbUrl = System.getProperty("db.url","");
  private final String dbUser = System.getProperty("db.user","");
  private final String dbPass = System.getProperty("db.pass", "");
  private final String dbTable = System.getProperty("db.table", "testdata");

  private TestDataRepository() {}

  public static synchronized TestDataRepository getInstance() {
    if (instance == null) instance = new TestDataRepository();
    return instance;
  }

  /**
   * Load test data once (CSV, Excel, or DB).
   * Cached for all test runs.
   */
  public synchronized Map<String, List<Map<String, String>>> loadData(String sourceType) {
    if (cachedData != null) return cachedData;

    try {
      switch (sourceType.toLowerCase()) {
        case "csv" -> cachedData = loadCSV(resolveFile(filePath));
        case "excel", "xlsx" -> cachedData = loadExcel(resolveFile(filePath));
        case "db" -> cachedData = loadFromDatabase();
        default -> throw new IllegalArgumentException("Unsupported data source: " + sourceType);
      }

      System.out.printf("✅ [TestDataRepository] Loaded %d feature/scenario groups from %s.%n",
          cachedData.size(), sourceType.toUpperCase());
    } catch (Exception e) {
      System.err.println("❌ [TestDataRepository] Failed to load test data: " + e.getMessage());
      e.printStackTrace();
      cachedData = new HashMap<>();
    }

    return cachedData;
  }

  /**
   * Resolves files from filesystem or classpath.
   */
  private File resolveFile(String path) throws IOException {
    File file = new File(path);
    if (file.exists()) return file;

    ClassLoader cl = Thread.currentThread().getContextClassLoader();

    URL resource = cl.getResource(path);
    if (resource == null) {
      // Try only filename (without directories)
      resource = cl.getResource(new File(path).getName());
    }

    if (resource != null) {
      return new File(resource.getFile());
    }

    throw new FileNotFoundException("Test data file not found: " + path);
  }

  /**
   * Loads test data from a CSV file.
   */
  private Map<String, List<Map<String, String>>> loadCSV(File file) throws IOException {
    Map<String, List<Map<String, String>>> data = new HashMap<>();

    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

      String headerLine = br.readLine();
      if (headerLine == null) return data;

      String[] headers = headerLine.split(",");
      String line;
      while ((line = br.readLine()) != null) {
        String[] values = line.split(",", -1);
        Map<String, String> row = new LinkedHashMap<>();
        for (int i = 0; i < headers.length && i < values.length; i++) {
          row.put(headers[i].trim(), values[i].trim());
        }
        addRowToGroupedData(data, row);
      }
    }

    return data;
  }

  /**
   * Loads test data from an Excel (.xlsx) file.
   */
  private Map<String, List<Map<String, String>>> loadExcel(File file) throws IOException {
    Map<String, List<Map<String, String>>> data = new HashMap<>();

    try (FileInputStream fis = new FileInputStream(file);
        Workbook workbook = new XSSFWorkbook(fis)) {

      Sheet sheet = workbook.getSheetAt(0);
      Iterator<Row> rows = sheet.iterator();
      if (!rows.hasNext()) return data;

      Row headerRow = rows.next();
      List<String> headers = new ArrayList<>();
      headerRow.forEach(cell -> headers.add(cell.getStringCellValue().trim()));

      while (rows.hasNext()) {
        Row r = rows.next();
        Map<String, String> row = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
          Cell c = r.getCell(i);
          String value = "";
          if (c != null) {
            c.setCellType(CellType.STRING);
            value = c.getStringCellValue().trim();
          }
          row.put(headers.get(i), value);
        }
        addRowToGroupedData(data, row);
      }
    }

    return data;
  }

  /**
   * Loads data from a JDBC database (auto-registers drivers).
   */
  private Map<String, List<Map<String, String>>> loadFromDatabase() throws SQLException {
    if (dbUrl == null) {
      throw new IllegalArgumentException("Missing property: -Ddb.url");
    }

    registerJdbcDriver(dbUrl);

    Map<String, List<Map<String, String>>> data = new HashMap<>();

    try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + dbTable)) {

      ResultSetMetaData meta = rs.getMetaData();
      int colCount = meta.getColumnCount();

      while (rs.next()) {
        Map<String, String> row = new LinkedHashMap<>();
        for (int i = 1; i <= colCount; i++) {
          row.put(meta.getColumnLabel(i), rs.getString(i));
        }
        addRowToGroupedData(data, row);
      }
    }

    return data;
  }

  /**
   * Attempts to auto-register a JDBC driver if not already loaded.
   */
  private void registerJdbcDriver(String jdbcUrl) {
    try {
      if (jdbcUrl.startsWith("jdbc:sqlite")) {
        Class.forName("org.sqlite.JDBC");
        System.out.println("🧩 [JDBC] Auto-registered SQLite driver");
      } else if (jdbcUrl.startsWith("jdbc:mysql")) {
        Class.forName("com.mysql.cj.jdbc.Driver");
        System.out.println("🧩 [JDBC] Auto-registered MySQL driver");
      } else if (jdbcUrl.startsWith("jdbc:postgresql")) {
        Class.forName("org.postgresql.Driver");
        System.out.println("🧩 [JDBC] Auto-registered PostgreSQL driver");
      } else if (jdbcUrl.startsWith("jdbc:h2")) {
        Class.forName("org.h2.Driver");
        System.out.println("🧩 [JDBC] Auto-registered H2 driver");
      } else if (jdbcUrl.startsWith("jdbc:sqlserver")) {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        System.out.println("🧩 [JDBC] Auto-registered SQL Server driver");
      } else if (jdbcUrl.startsWith("jdbc:oracle")) {
        Class.forName("oracle.jdbc.OracleDriver");
        System.out.println("🧩 [JDBC] Auto-registered Oracle driver");
      }
    } catch (ClassNotFoundException ignored) {
      System.out.println("⚠️ [JDBC] No specific driver found for " + jdbcUrl);
    }
  }

  /**
   * Groups data by feature_name::scenario_name.
   */
  private void addRowToGroupedData(Map<String, List<Map<String, String>>> data, Map<String, String> row) {
    String feature = row.getOrDefault("feature_name", "").trim();
    String scenario = row.getOrDefault("scenario_name", "").trim();
    if (feature.isEmpty() || scenario.isEmpty()) return;

    String key = feature + "::" + scenario;
    data.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
  }

  /** Clears cached data (for reruns or reload). */
  public void clearCache() {
    cachedData = null;
  }
}




