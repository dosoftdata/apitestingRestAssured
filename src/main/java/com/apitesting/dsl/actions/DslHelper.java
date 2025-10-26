package com.apitesting.dsl.actions;

import com.apitesting.dsl.ScenarioContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.hamcrest.Matcher;
import static org.hamcrest.Matchers.*;
/**
 *
 */
public abstract class DslHelper {
  final ScenarioContext context;
  protected final ObjectMapper mapper = new ObjectMapper();
  public DslHelper(ScenarioContext context) {
    this.context = context;
  }
  /**
   * Dynamically build a Hamcrest matcher based on the keyword and expected value
   */
  protected Matcher<?> buildMatcher(String matcherName, String value) {
    switch (matcherName.toLowerCase()) {

      // --- Basic equality & negation
      case "equalto":
        return equalTo(parseValue(value));
      case "notequalto":
        return not(equalTo(parseValue(value)));
      case "isequalto":
        return is(equalTo(parseValue(value)));
      case "not":
        return not(parseValue(value));

      // --- String matchers
      case "containsstring":
        return containsString(value);
      case "containsignoringcase":
        return containsStringIgnoringCase(value);
      case "startswith":
        return startsWith(value);
      case "endswith":
        return endsWith(value);
      case "equaltoignoringcase":
        return equalToIgnoringCase(value);
      case "equaltoignoringwhitespace":
        return equalToIgnoringWhiteSpace(value);
      case "matchesregex":
      case "matchespattern":
        return matchesPattern(value);

      // --- Numeric comparisons
      case "greaterthan":
        return greaterThan(value.contains(".")  ? Double.parseDouble(value) : Integer.parseInt(value));
      case "greaterthanorequalto":
        return greaterThanOrEqualTo(value.contains(".")  ? Double.valueOf(value) : Integer.valueOf(value));
      case "lessthan":
        return lessThan(value.contains(".")  ? Double.valueOf(value) : Integer.valueOf(value));
      case "lessthanorequalto":
        return lessThanOrEqualTo(value.contains(".")  ? Double.valueOf(value) : Integer.valueOf(value));
      case "closeto":
        return closeTo(Double.parseDouble(value), 0.001);

      // --- Collections
      case "hasitem":
        return hasItem(parseValue(value));
      case "hasitems":
        return hasItems(splitValues(value));
      case "hassize":
        return hasSize(Integer.parseInt(value));
      case "isempty":
        return empty();
      case "everyitemcontains":
        return everyItem(containsString(value));

      // --- Maps
      case "haskey":
        return hasKey(value);
      case "hasvalue":
        return hasValue(parseValue(value));
      case "hasentry": {
        String[] parts = value.split("=", 2);
        if (parts.length < 2)
          throw new IllegalArgumentException("hasEntry expects format key=value");
        return hasEntry(parts[0].trim(), parseValue(parts[1].trim()));
      }

      // --- Null / Existence
      case "nullvalue":
        return nullValue();
      case "notnullvalue":
        return notNullValue();

      // --- Type checking
      case "instanceof": {
        try {
          return instanceOf(Class.forName(value));
        } catch (ClassNotFoundException e) {
          throw new IllegalArgumentException("Invalid class for instanceOf: " + value, e);
        }
      }

      // --- Boolean checkers
      case "istrue":
        return is(true);
      case "isfalse":
        return is(false);

      // --- Default fallback
      default:
        throw new IllegalArgumentException("Unsupported matcher: " + matcherName);
    }
  }
  /**
   * Converts raw string to appropriate type (boolean, number, or string)
   */
  private Object parseValue(String value) {
    if (value == null || value.equalsIgnoreCase("null")) return null;
    if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))
      return Boolean.valueOf(value);
    try {
      if (value.contains(".")) return Double.parseDouble(value);
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      return value; // treat as string
    }
  }

  /***
    * Splits comma-separated expected values into a list
     */
  private Object[] splitValues(String csv) {
    return Arrays.stream(csv.split(","))
        .map(String::trim)
        .map(this::parseValue)
        .toArray();
  }


  @SuppressWarnings("unchecked")
  private <T extends Comparable<T>> T parseComparable(String value) {
    try {
      if (value.contains(".")) {
        return (T) Double.valueOf(value);  // Double implements Comparable<Double>
      } else {
        return (T) Integer.valueOf(value); // Integer implements Comparable<Integer>
      }
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Expected numeric value for matcher, got: " + value);
    }
  }
  // ---------------- HELPER ----------------

  protected Map<String, Object> parseArgs(DataTable table, String docString) throws Exception {
    if (table != null && !table.isEmpty()) {
      Map<String, Object> args = new HashMap<>();
      table.asMaps().forEach(args::putAll);
      return args;
    }

    if (docString != null && !docString.trim().isEmpty()) {
      return mapper.readValue(docString, Map.class);
    }

    return Collections.emptyMap();
  }

}
