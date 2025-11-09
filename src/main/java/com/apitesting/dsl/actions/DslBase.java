package com.apitesting.dsl.actions;

import com.apitesting.core.base.CustomRequestSpec;
import com.apitesting.dsl.ScenarioContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matcher;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;

import io.restassured.response.ValidatableResponse;
import com.apitesting.core.filters.RequestDelayFilter;
import com.apitesting.dsl.*;
import java.io.File;
import lombok.SneakyThrows;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertTrue;
/**
 *
 */
@Slf4j
public abstract class DslBase {

  final ScenarioContext context;
  protected final ObjectMapper mapper = new ObjectMapper();

  public DslBase(ScenarioContext context) {
    this.context = context;

  }

  public void url(String expression) {
    this.context.getSpec().setBaseUri(ScenarioContext.resolve(expression));
  }


  public void delayRequest(int millisecond) {
    this.context.getSpec().setFilter(new RequestDelayFilter(millisecond));
  }


  public void path(String path) {
    this.context.getSpec().setBasePath(ScenarioContext.resolve(path));
    ScenarioContext.set("currentBasePath", ScenarioContext.resolve(path));
  }


  public void param(String name, String value) {
    this.context.getSpec().setQueryParam(name, ScenarioContext.resolve(value));
  }


  public void pathParam(String name, String value) {
    this.context.getSpec().setPathParam(name, ScenarioContext.resolve(value));
  }


  public void cookie(String name, String value) {
    this.context.getSpec().setCookie(name, ScenarioContext.resolve(value));
  }


  public void header(String name, String value) {
    this.context.getSpec().setHeader(name, ScenarioContext.resolve(value));
  }


  public void formField(String name, Object value) {
    this.context.getSpec().setFormParam(name, ScenarioContext.resolve((String) value));
  }


  public void request(String body) {
    this.context.getSpec().setBody(ScenarioContext.resolve(body));
  }


  public void multipartFile(String file, File filePath, String mimeType) {
    this.context.getSpec().addMultiPartFile(file, filePath, mimeType);
  }


  public void multipart(String file, String filePath, String mimeType) {
    this.context.getSpec().addMultiPart(file, filePath, mimeType);
  }


  public void method(String method, String path) {
    if (path != null) {
      this.context.getSpec().setBasePath(ScenarioContext.resolve(path));
    }
    this.context.getApi().setResponse(
        this.context.getRes().spec(this.context.getSpec().build())
            .when().request(method)
    );
  }

  public void def(String name, String expression) {
    ScenarioContext.set(name, expression);
    context.setVariable(name, expression);
  }

 
  public void defResponse(String name, String expression) {
    String value = context.getApi().getResponse().jsonPath().getString(expression);
    ScenarioContext.set(name, value);
  }


  public void status(int status) {
    context.getApi().getResponse().then().statusCode(status);
  }


  public void statusOr(int status, int or) {
    context.getApi().getResponse().then().statusCode(anyOf(is(status), is(or)));
  }

  public void assertEqual(String s1, String expression) {
    assertThat(s1, is(ScenarioContext.resolve(expression)));
  }

  public void redirectFollow(Integer b) {
    this.context.getSpec().setRedirectFollow(Boolean.valueOf(String.valueOf(b)));
  }

 
  public void redirectCircular(Integer b) {
    this.context.getSpec().setRedirectCircular(Boolean.valueOf(String.valueOf(b)));
  }

  @SneakyThrows
  public void retryDocString(String method, String path, String conditionJson) {
    awaitUntil(method, path, conditionJson);
  }

 
  public void retry(String method, String path, String conditionJson) {
    awaitUntil(method, path, conditionJson);
  }


  public void match(String expected) {
    JsonMatcherResult result = JsonMatcher.matchJson(
        this.context.getApi().getResponse().asString(),
        ScenarioContext.resolve(expected)
    );
    assertTrue(result.isSuccess(), result.getMessage());
  }


  public void matchPath(String path, String expr) {
    JsonMatcherResult result = JsonMatcher.matchJson(
        this.context.getApi().getResponse().asString(),
        path + " " + ScenarioContext.resolve(expr)
    );
    assertTrue(result.isSuccess(), result.getMessage());
  }

  public void match_each(String arrayPath, String expected) {
    JsonMatcherResult result = JsonMatcher.matchEach(
        this.context.getApi().getResponse().asString(),
        arrayPath,
        ScenarioContext.resolve(expected)
    );
    assertTrue(result.isSuccess(), result.getMessage());
  }


  public void match_response_body_path(String path, String matcherName, String expected) {
    ValidatableResponse response = context.getApi().getResponse().then();
    Matcher<?> matcher = buildMatcher(matcherName, expected);
    response.body(path, matcher);
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
        return greaterThan(
            value.contains(".") ? Double.parseDouble(value) : Integer.parseInt(value));
      case "greaterthanorequalto":
        return greaterThanOrEqualTo(
            value.contains(".") ? Double.valueOf(value) : Integer.valueOf(value));
      case "lessthan":
        return lessThan(value.contains(".") ? Double.valueOf(value) : Integer.valueOf(value));
      case "lessthanorequalto":
        return lessThanOrEqualTo(
            value.contains(".") ? Double.valueOf(value) : Integer.valueOf(value));
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
    if (value == null || value.equalsIgnoreCase("null"))
      return null;
    if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))
      return Boolean.valueOf(value);
    try {
      if (value.contains("."))
        return Double.parseDouble(value);
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

  protected boolean evaluateCondition(String action, Object actualValue, Object expected) {
    switch (action.toLowerCase()) {
      case "equal":
        return Objects.equals(
            String.valueOf(actualValue),
            String.valueOf(expected)
        );

      case "exists":
        return actualValue != null;

      case "contains":
        return actualValue != null && actualValue.toString().contains(String.valueOf(expected));

      case "greater":
        if (actualValue instanceof Number && expected instanceof Number) {
          return ((Number) actualValue).doubleValue() > ((Number) expected).doubleValue();
        }
        return false;

      case "listcontains":
        return actualValue instanceof List<?> list && list.contains(expected);

      case "any":
        return true;

      default:
        throw new IllegalArgumentException("Unknown action: " + action);
    }
  }

  protected void awaitUntil(String method, String path, String conditionJson) {
    JsonPath json = new JsonPath(conditionJson);
    Object expected = json.get("hasValue"); // can be String, Number, Boolean, null
    String bodyPath = json.getString("bodyPath");
    String action   = json.getString("action") != null ? json.getString("action") : "equal";

    Integer minutes        = json.get("minutes")       != null ? json.getInt("minutes") : 5;
    Integer seconds        = json.get("seconds")       != null ? json.getInt("seconds") : 30;
    Integer pollMillis     = json.get("pollMillis")    != null ? json.getInt("pollMillis") : 5000;
    Integer expectedStatus = json.get("status")        != null ? json.getInt("status") : 200;
    Integer attempts       = json.get("attempts")      != null ? json.getInt("attempts") : null;

    CustomRequestSpec originalSpec = this.context.getSpec();
    AtomicReference<Response> lastResp = new AtomicReference<>();
    AtomicInteger attemps = new AtomicInteger(0);
    if (path != null) {
      path = ScenarioContext.resolve(path);
      this.context.getSpec().basePath(path);
    }
    String finalPath = String.valueOf(ScenarioContext.get("currentBasePath"));
    // Resolve path
    if (attempts != null && attempts > 0) {
      AtomicInteger attempt = new AtomicInteger(0);
      await()
//            .pollInterval(Duration.ofMillis(pollMillis))
//            .pollDelay(Duration.ZERO)
//            .atMost(Duration.ofMillis((long) attempts * pollMillis)) // max time = attempts * poll
//            .ignoreExceptions()
          .atMost(Duration.ofMinutes(minutes).plusSeconds(seconds))
          .pollInterval(Duration.ofMillis(pollMillis))
          .ignoreExceptions()
          .until(() -> {
            int current = attempt.incrementAndGet();

            Response resp = context.getRes()
                .spec(originalSpec.build())
                .when().request(method);

            lastResp.set(resp);

            Object value = null;
            try { value = resp.path(bodyPath); } catch (Exception ignore) {}

            boolean passed = evaluateCondition(action, value, expected)
                && resp.statusCode() == expectedStatus;

            log.info("[Retry {}/{}] {} {} | status={} | {}={} | passed={}",
                current, attempts, method,finalPath,
                resp.statusCode(), bodyPath, value, passed);

            //if (passed) return true;
            return current >= attempts; // stop if attempts exceeded
          });

      context.getApi().setResponse(lastResp.get());
      return;
    }

    await()
        .atMost(Duration.ofMinutes(minutes).plusSeconds(seconds))
        .pollInterval(Duration.ofMillis(pollMillis))
        .ignoreExceptions()
        .until(() -> {
          Response resp = context.getRes()
              .spec(originalSpec.build())
              .when().request(method);

          lastResp.set(resp);
          attemps.incrementAndGet();
          boolean statusOk = resp.statusCode() == expectedStatus;

          Object value = null;
          if (bodyPath != null) {
            try { value = resp.path(bodyPath); } catch (Exception ignored) {

            }
          }

          boolean bodyOk = (bodyPath == null) || evaluateCondition(action, value, expected);

          log.info("[Retry] {} {} | status={} | attemps={} | {}={} | passed={}",
              method, finalPath, resp.statusCode(), attemps.get(), bodyPath, value, statusOk && bodyOk);

          return statusOk && bodyOk;
        });

    this.context.getApi().setResponse(lastResp.get());
  }


}
