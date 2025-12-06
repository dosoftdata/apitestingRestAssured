package com.apitesting.dsl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.testng.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Pattern;

public class DslUtils {
  private static final ObjectMapper mapper = new ObjectMapper();


  /**
   * Reads the contents of a file from the given path.
   *
   * @param path the path to the file
   * @return the file contents as a String
   */

  public static String readFile(String path) {
    try {
      return new String(Files.readAllBytes(new File(path).toPath()));
    } catch (IOException e) {
      throw new RuntimeException("Failed to read file: " + path, e);
    }
  }


  /**
   * Reads the contents of a file from the given path.
   *
   * @param path the path to the file
   * @return the file contents as a String
   */

  public static JsonNode readJson(String path) {
    try {
      return mapper.readTree(new File(path));
    } catch (IOException e) {
      throw new RuntimeException("Failed to parse JSON: " + path, e);
    }
  }

  /**
   * @param response the response parameter used in validateResponse method
   * @param jsonPath the jsonPath parameter used in validateResponse method
   * @param expected the expected parameter used in validateResponse method
   */
  public static void matchField(Response response, String jsonPath, String expected) {
    String actual = response.jsonPath().getString(jsonPath);
    Assert.assertEquals(actual, expected, "Mismatch at " + jsonPath);
  }

  /**
   * @param response the response parameter used in validateResponse method
   * @param jsonPath the jsonPath parameter used in validateResponse method
   * @param expected the expected parameter used in validateResponse method
   */
  public static void matchRegex(Response response, String jsonPath, String regex) {
    String actual = response.jsonPath().getString(jsonPath);
    Assert.assertTrue(Pattern.matches(regex, actual),
        "Field " + jsonPath + " with value " + actual + " did not match regex " + regex);
  }


}
