package plugins.externalTestData;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class TestDataRepository {
  private static TestDataRepository instance;
  private Map<String, List<Map<String, String>>> cachedData;

  private final String filePath = System.getProperty(
      "testdata.file", "src/test/resources/testdata/testdata.csv");

  private TestDataRepository() {}

  public static synchronized TestDataRepository getInstance() {
    if (instance == null) instance = new TestDataRepository();
    return instance;
  }

  public synchronized Map<String, List<Map<String, String>>> loadData(String sourceType) {
    if (cachedData != null) return cachedData;

    try {
      File file = new File(filePath);
      if (filePath.endsWith(".csv")) {
        cachedData = loadCSV(file);
      } else if (filePath.endsWith(".xlsx")) {
        cachedData = loadExcel(file);
      } else {
        throw new IllegalArgumentException("Unsupported data file type: " + filePath);
      }
      System.out.println("✅ [TestDataRepository] Loaded " + cachedData.size() + " feature/scenario groups.");
    } catch (Exception e) {
      e.printStackTrace();
      cachedData = new HashMap<>();
    }
    return cachedData;
  }

  private Map<String, List<Map<String, String>>> loadCSV(File file) throws IOException {
    Map<String, List<Map<String, String>>> data = new HashMap<>();
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
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
        String key = row.get("feature_name") + "::" + row.get("scenario_name");
        data.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
      }
    }
    return data;
  }

  private Map<String, List<Map<String, String>>> loadExcel(File file) throws IOException {
    Map<String, List<Map<String, String>>> data = new HashMap<>();
    try (FileInputStream fis = new FileInputStream(file);
        Workbook workbook = new XSSFWorkbook(fis)) {
      Sheet sheet = workbook.getSheetAt(0);
      Iterator<Row> rows = sheet.iterator();

      Row headerRow = rows.next();
      List<String> headers = new ArrayList<>();
      headerRow.forEach(cell -> headers.add(cell.getStringCellValue()));

      while (rows.hasNext()) {
        Row r = rows.next();
        Map<String, String> row = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
          Cell c = r.getCell(i);
          row.put(headers.get(i), c == null ? "" : c.toString());
        }
        String key = row.get("feature_name") + "::" + row.get("scenario_name");
        data.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
      }
    }
    return data;
  }
}



