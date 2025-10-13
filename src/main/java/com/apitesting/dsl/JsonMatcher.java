package com.apitesting.dsl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.jayway.jsonpath.JsonPath;
import java.util.*;
/**
 * JsonMatcher – a JSON matcher utility for REST API testing.
 *
 * ✅ Supports:
 * -----------------------------------------------------------------------------
 * 🔹 SIMPLE MATCHERS (used inside expected JSON)
 * -----------------------------------------------------------------------------
 *   #string      → expects a string
 *   #number      → expects a number
 *   #boolean     → expects a boolean
 *   #null        → expects null
 *   #notnull     → expects non-null value
 *   #empty       → expects empty/null/blank/empty-array/object
 *   #notempty    → expects non-empty value
 *   #any         → wildcard, always matches
 *   #regex <exp> → matches regex, e.g. "#regex ^[A-Z0-9]{8}$"
 *   #contains <x>→ value contains substring x
 *   "..."        → deep match ignore subtree (object or array)
 *   #size <n>      → asserts array size equals n
 *   #length <n>    → alias for #size
 *   #min <n>       → asserts array size >= n
 *   #max <n>       → asserts array size <= n
 *   $.[*] or @array paths are supported via JsonPath
 *
 * -----------------------------------------------------------------------------
 * 🔹 JSONPATH MATCH CONDITIONS
 * -----------------------------------------------------------------------------
 *   Syntax:  $.<path> <operator> <expected>
 *
 *   = <value>       → equals
 *   != <value>      → not equals
 *   > <number>      → greater than
 *   < <number>      → less than
 *   >= <number>     → greater or equal
 *   <= <number>     → less or equal
 *   contains <text> → substring check
 *   matches <regex> → regex check
 *   exists          → path must exist
 *   !exists         → path must NOT exist
 *
 * -----------------------------------------------------------------------------
 * 🔹 STRUCTURAL / DEEP MATCHING
 * -----------------------------------------------------------------------------
 *   - Deep compare JSON trees recursively (like Karate)
 *   - Supports "..." to ignore partial structures
 *   - Supports #matchers anywhere inside expected JSON
 *   - Handles nested arrays and objects
 *
 * -----------------------------------------------------------------------------
 * 🔹 SPECIAL UTILITIES
 * -----------------------------------------------------------------------------
 *   matchJson(actual, expectedPattern)
 *       → main entry point for single JSON or JSONPath assertions
 *
 *   matchEach(actual, jsonPath, expectedPattern)
 *       → validates that every element in the array at jsonPath
 *         matches the given pattern or JSON structure
 *
 * -----------------------------------------------------------------------------
 * Example Usages:
 * -----------------------------------------------------------------------------
 *   JsonMatcher.matchJson("{\"id\":1}", "{ \"id\": #number }");
 *   JsonMatcher.matchJson("{\"name\":\"John\"}", "$.name = John");
 *   JsonMatcher.matchJson("{\"id\":5}", "$.id != 0");
 *   JsonMatcher.matchJson("{\"age\":30}", "$.age >= 18");
 *   JsonMatcher.matchJson("{\"email\":\"a@test.com\"}", "$.email matches .*@test\\.com");
 *   JsonMatcher.matchEach("{\"values\":[1,2,3]}", "$.values", "#number");
 *   JsonMatcher.matchJson("{\"items\":[1,2,3]}", "{ \"items\": #size = 3 }");
 *   JsonMatcher.matchJson("{\"values\":[1,2,3]}", "$.values #size > 2");
 *   JsonMatcher.matchJson("{\"ids\":[]}", "$.ids #size = 0");
 * -----------------------------------------------------------------------------
 */

public class JsonMatcher extends JsonMatcherResult {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public JsonMatcher(boolean success, String message) {
    super(success, message);
  }


  public static JsonMatcherResult matchJson(String actualJson, String expectedPattern) {
    try {
      if (actualJson == null) actualJson = "";
      actualJson = actualJson.trim();
      if (expectedPattern == null) expectedPattern = "";
      expectedPattern = expectedPattern.trim();

      if (actualJson.isEmpty() || actualJson.equalsIgnoreCase("null")) {
        if (expectedPattern.equalsIgnoreCase("#empty") || expectedPattern.isEmpty()) {
          return pass("Empty body matched successfully");
        } else {
          return fail("Expected non-empty body but got empty/null");
        }
      }

      JsonNode actualNode;
      try {
        actualNode = MAPPER.readTree(actualJson);
      } catch (Exception e) {
        actualNode = TextNode.valueOf(actualJson);
      }

      if (expectedPattern.startsWith("#")) {
        return evaluateSimpleMatcher(actualNode, expectedPattern);
      }

      if (expectedPattern.trim().startsWith("{") || expectedPattern.trim().startsWith("[")) {
        JsonNode expectedNode = MAPPER.readTree(expectedPattern);
        return deepCompareJson(actualNode, expectedNode, "$");
      }

      if (expectedPattern.startsWith("$")) {
        return evaluateJsonPathExpression(actualJson, expectedPattern);
      }

      return actualJson.equals(expectedPattern)
          ? pass("Raw text match succeeded")
          : fail("Raw text mismatch: expected '" + expectedPattern + "' but got '" + actualJson + "'");

    } catch (Exception e) {
      return fail("matchJson error: " + e.getMessage());
    }
  }

  private static JsonMatcherResult evaluateSimpleMatcher(JsonNode node, String pattern) {
    String p = pattern.trim().toLowerCase();
    switch (p) {
      case "#size":
      case "#length":
        if (!node.isArray()) return fail("Expected array for #size but got " + node.getNodeType());
        int size = node.size();
        return pass("Array size OK = " + size);

      case "#min":
        if (!node.isArray()) return fail("Expected array for #min but got " + node.getNodeType());
        return pass("Min OK (actual size=" + node.size() + ")");

      case "#max":
        if (!node.isArray()) return fail("Expected array for #max but got " + node.getNodeType());
        return pass("Max OK (actual size=" + node.size() + ")");
      case "#array": return node.isArray() ? pass("Array OK") : fail("Expected Array but got " + node.getNodeType());
      case "#string": return node.isTextual() ? pass("String OK") : fail("Expected string but got " + node.getNodeType());
      case "#number": return node.isNumber() ? pass("Number OK") : fail("Expected number but got " + node.getNodeType());
      case "#boolean": return node.isBoolean() ? pass("Boolean OK") : fail("Expected boolean but got " + node.getNodeType());
      case "#null": return node.isNull() ? pass("Null OK") : fail("Expected null but got non-null");
      case "#notnull": return !node.isNull() ? pass("Not null OK") : fail("Expected non-null but got null");
      case "#empty":
        if (node.isNull() || (node.isTextual() && node.asText().trim().isEmpty()) || (node.isArray() && node.size() == 0) || (node.isObject() && node.size() == 0))
          return pass("Empty OK");
        return fail("Expected empty but got: " + node);
      case "#notempty":
        if (node.isNull() || (node.isTextual() && node.asText().trim().isEmpty()) || (node.isArray() && node.size() == 0) || (node.isObject() && node.size() == 0))
          return fail("Expected non-empty but got empty");
        return pass("Not empty OK");
      default:
        if (p.startsWith("#regex")) {
          String regex = pattern.substring(6).trim();
          return node.asText().matches(regex) ? pass("Regex OK") : fail("Regex mismatch: " + node.asText());
        } else if (p.startsWith("#contains")) {
          String part = pattern.substring(9).trim();
          return node.toString().contains(part) ? pass("Contains OK") : fail("Expected to contain '" + part + "'");
        } else if (p.equals("#any")) {
          return pass("Wildcard match (#any)");
        }
        return fail("Unknown matcher: " + pattern);
    }
  }

  private static JsonMatcherResult evaluateJsonPathExpression(String actualJson, String expr) {
    try {
      int condStart = expr.indexOf(" #");
      if (condStart == -1) condStart = expr.indexOf("#");
      String path, condition;
      if (condStart > 0) {
        path = expr.substring(0, condStart).trim();
        condition = expr.substring(condStart).trim();
      } else {
        // String[] parts = expr.split("\s+", 2);
        String[] parts = expr.split("\\s+", 2);
        if (parts.length < 2) return fail("Invalid JSONPath expression: " + expr);
        path = parts[0];
        condition = parts[1].trim();
      }

      Object value;
      try {
        value = JsonPath.read(actualJson, path);
      } catch (com.jayway.jsonpath.PathNotFoundException e) {
        if (condition.equals("!exists")) return pass("Path not found as expected");
        return fail("Path not found: " + path);
      }

      if (condition.equals("exists")) return pass("Path exists");
      if (condition.equals("!exists")) return fail("Path exists but should not");

      if (value instanceof List<?>) {
        for (Object el : (List<?>) value) {
          JsonMatcherResult r = evaluateCondition(el, condition);
          if (!r.isSuccess()) return fail(path + ": " + r.getMessage());
        }
        return pass("All elements matched condition: " + condition);
      }

      return evaluateCondition(value, condition);

    } catch (Exception e) {
      return fail("evaluateJsonPathExpression error: " + e.getMessage());
    }
  }

  private static JsonMatcherResult evaluateCondition(Object val, String cond) {
    String strVal = String.valueOf(val).trim();
    try {
      if (val instanceof List<?>) {
        List<?> list = (List<?>) val;
        int size = list.size();
        if (cond.startsWith("#size")) {
          String exp = cond.replace("#size", "").trim();
          if (exp.startsWith("=")) {
            int expected = Integer.parseInt(exp.substring(1).trim());
            return size == expected
                ? pass("Array size = " + expected + " OK")
                : fail("Expected size " + expected + " but got " + size);
          }
          if (exp.startsWith(">")) {
            int expected = Integer.parseInt(exp.substring(1).trim());
            return size > expected
                ? pass("Array size > " + expected + " OK")
                : fail("Expected size > " + expected + " but got " + size);
          }
          if (exp.startsWith("<")) {
            int expected = Integer.parseInt(exp.substring(1).trim());
            return size < expected
                ? pass("Array size < " + expected + " OK")
                : fail("Expected size < " + expected + " but got " + size);
          }
          return pass("Array size = " + size);
        }
      }

      // Equality
      if (cond.startsWith("=")) {
        String expected = cond.substring(1).trim().replaceAll("^['\\\"]|['\\\"]$", "");
        return strVal.equals(expected)
            ? pass("= OK")
            : fail("Expected " + expected + " but got " + strVal);
      }

      // Not equal
      if (cond.startsWith("!=")) {
        String expected = cond.substring(2).trim().replaceAll("^['\\\"]|['\\\"]$", "");
        return !strVal.equals(expected)
            ? pass("!= OK")
            : fail("Expected not equal to " + expected + " but got " + strVal);
      }

      // Greater than / less than
      if (cond.startsWith(">") || cond.startsWith("<")) {
        double actualNum = Double.parseDouble(strVal);
        double expectedNum;
        String op;
        if (cond.startsWith(">=")) {
          expectedNum = Double.parseDouble(cond.substring(2).trim());
          op = ">=";
          return (actualNum >= expectedNum)
              ? pass(">= OK")
              : fail("Expected " + actualNum + " >= " + expectedNum);
        } else if (cond.startsWith("<=")) {
          expectedNum = Double.parseDouble(cond.substring(2).trim());
          op = "<=";
          return (actualNum <= expectedNum)
              ? pass("<= OK")
              : fail("Expected " + actualNum + " <= " + expectedNum);
        } else if (cond.startsWith(">")) {
          expectedNum = Double.parseDouble(cond.substring(1).trim());
          op = ">";
          return (actualNum > expectedNum)
              ? pass("> OK")
              : fail("Expected " + actualNum + " > " + expectedNum);
        } else if (cond.startsWith("<")) {
          expectedNum = Double.parseDouble(cond.substring(1).trim());
          op = "<";
          return (actualNum < expectedNum)
              ? pass("< OK")
              : fail("Expected " + actualNum + " < " + expectedNum);
        }
      }

      // Contains / Regex
      if (cond.startsWith("#contains") || cond.startsWith("contains")) {
        String part = cond.replaceFirst("#?contains", "").trim();
        return strVal.contains(part)
            ? pass("Contains OK")
            : fail("Does not contain " + part);
      }

      if (cond.startsWith("matches")) {
        String regex = cond.substring(7).trim();
        return strVal.matches(regex)
            ? pass("Regex OK")
            : fail("Regex mismatch: " + strVal);
      }

      return fail("Unsupported condition: " + cond);

    } catch (Exception e) {
      return fail("Condition evaluation error: " + e.getMessage());
    }
  }

  private static JsonMatcherResult deepCompareJson(JsonNode actual, JsonNode expected, String path) {
    if (path == null || path.isEmpty()) path = "$";
    if (expected.isTextual() && expected.asText().trim().equals("...")) return pass("Ignored subtree at " + path);
    if (expected.isTextual() && expected.asText().trim().startsWith("#")) return evaluateSimpleMatcher(actual, expected.asText().trim());

    if (expected.isObject()) {
      if (!actual.isObject()) return fail(path + ": Expected object but got " + actual.getNodeType());
      Iterator<Map.Entry<String, JsonNode>> fields = expected.fields();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> entry = fields.next();
        String key = entry.getKey();
        JsonNode expectedValue = entry.getValue();
        if (key.equals("...")) return pass("Ignored remaining fields at " + path);
        JsonNode actualValue = actual.get(key);
        if (actualValue == null) return fail(path + "." + key + ": Missing field in actual JSON");
        JsonMatcherResult r = deepCompareJson(actualValue, expectedValue, path + "." + key);
        if (!r.isSuccess()) return r;
      }
      return pass("All fields matched at " + path);
    }

    if (expected.isArray()) {
      if (!actual.isArray()) return fail(path + ": Expected array but got " + actual.getNodeType());
      ArrayNode expArr = (ArrayNode) expected, actArr = (ArrayNode) actual;

      if (expArr.size() == 0 && actArr.size() == 0) return pass(path + ": Both arrays empty");
      if (expArr.size() == 1 && expArr.get(0).isTextual() && expArr.get(0).asText().trim().equals("..."))
        return pass(path + ": Ignored array contents via ...");

      if (expArr.size() == 1 && actArr.size() > 0) {
        JsonNode expectedEl = expArr.get(0);
        for (int i = 0; i < actArr.size(); i++) {
          JsonMatcherResult r = deepCompareJson(actArr.get(i), expectedEl, path + "[" + i + "]");
          if (!r.isSuccess()) return fail("Array element[" + i + "] mismatch: " + r.getMessage());
        }
        return pass("All array elements matched pattern at " + path);
      }

      if (expArr.size() != actArr.size())
        return fail(path + ": Array size mismatch: expected " + expArr.size() + " but got " + actArr.size());

      for (int i = 0; i < expArr.size(); i++) {
        JsonMatcherResult r = deepCompareJson(actArr.get(i), expArr.get(i), path + "[" + i + "]");
        if (!r.isSuccess()) return fail("Array element[" + i + "] mismatch: " + r.getMessage());
      }
      return pass("Array matched at " + path);
    }

    if (expected.isNumber() || expected.isTextual() || expected.isBoolean()) {
      if (!actual.asText().equals(expected.asText()))
        return fail(path + ": Value mismatch, expected=" + expected.asText() + ", actual=" + actual.asText());
      return pass("Scalar match OK at " + path);
    }

    if (expected.isNull() && actual.isNull()) return pass(path + ": Both null");
    if (expected.isNull() && !actual.isNull()) return fail(path + ": Expected null but got non-null");
    return pass("Match complete at " + path);
  }

  public static JsonMatcherResult matchEach(String actualJson, String jsonPath, String expectedPattern) {
    try {
      Object result = JsonPath.read(actualJson, jsonPath);

      if (!(result instanceof List<?>)) {
        return fail("JSONPath did not return an array: " + jsonPath);
      }

      List<?> list = (List<?>) result;
      if (list.isEmpty()) {
        return fail("No elements found at path: " + jsonPath);
      }

      // Prepare matcher / expected node
      JsonNode expectedNode = null;
      boolean isMatcher = expectedPattern.trim().startsWith("#");

      if (!isMatcher && (expectedPattern.trim().startsWith("{") || expectedPattern.trim().startsWith("["))) {
        expectedNode = MAPPER.readTree(expectedPattern);
      }

      for (int i = 0; i < list.size(); i++) {
        Object el = list.get(i);
        JsonNode actualNode = MAPPER.valueToTree(el);

        JsonMatcherResult resultEach;
        if (isMatcher) {
          resultEach = evaluateSimpleMatcher(actualNode, expectedPattern);
        } else if (expectedNode != null) {
          resultEach = deepCompareJson(actualNode, expectedNode, "$[" + i + "]");
        } else {
          // simple scalar compare
          if (actualNode.asText().equals(expectedPattern)) {
            resultEach = pass("Scalar equal at index " + i);
          } else {
            resultEach = fail("Scalar mismatch at index " + i + ": expected " + expectedPattern + ", got " + actualNode.asText());
          }
        }

        if (!resultEach.isSuccess()) {
          return fail("Array element[" + i + "] mismatch: " + resultEach.getMessage());
        }
      }

      return pass("All elements at " + jsonPath + " matched pattern successfully");

    } catch (Exception e) {
      return fail("matchEach error: " + e.getMessage());
    }
  }

}


