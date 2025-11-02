package com.apitesting.core.filters;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import static org.awaitility.Awaitility.await;
@Slf4j
@Data
public class RetryFilter implements Filter {

  private final Duration timeout;
  private final Duration pollInterval;
  private  Predicate<Response> bodyCondition;
  private Integer responseStatus;
  // New fields for logging
  private String bodyPathForLogging;
  private Object expectedValueForLogging;

  // existing constructors --------------------------------------

  public RetryFilter(Duration timeout, Duration pollInterval) {
    this.timeout = timeout;
    this.pollInterval = pollInterval;
    this.responseStatus = 200;
  }

  public RetryFilter(Duration timeout, Duration pollInterval, Integer responseStatus) {
    this.timeout = timeout;
    this.pollInterval = pollInterval;
    this.responseStatus = responseStatus;
  }

  public RetryFilter() {
    this(Duration.ofSeconds(30), Duration.ofMillis(1000), 200);
  }

  public RetryFilter(Duration timeout,
      Duration pollInterval,
      int expectedStatus,
      Predicate<Response> bodyCondition) {
          this.timeout = timeout;
          this.pollInterval = pollInterval;
          this.responseStatus = expectedStatus;
          this.bodyCondition = bodyCondition;
  }
  // NEW convenience constructors -------------------------------
  /** Minutes + seconds, default expected=200, poll=1s */
  public RetryFilter(int minutes, int seconds) {
    this(Duration.ofMinutes(minutes).plusSeconds(seconds), Duration.ofSeconds(1), 200);
  }

  /** Minutes + seconds + poll interval in ms, default expected=200 */
  public RetryFilter(int minutes, int seconds, int pollMillis) {
    this(Duration.ofMinutes(minutes).plusSeconds(seconds), Duration.ofMillis(pollMillis), 200);
  }

  /** Full control: minutes + seconds + poll interval + expected status */
  public RetryFilter(int minutes, int seconds, int pollMillis, int expectedStatus) {
    this(Duration.ofMinutes(minutes).plusSeconds(seconds), Duration.ofMillis(pollMillis), expectedStatus);
  }
  public RetryFilter(Duration timeout, Duration pollInterval, Integer responseStatus,
      Predicate<Response> bodyCondition) {
    this.timeout = timeout;
    this.pollInterval = pollInterval;
    this.responseStatus = responseStatus;
    this.bodyCondition = bodyCondition;
  }
  public RetryFilter(Integer minutes, Integer seconds, Integer pollMillis,
      Integer expectedStatus, Predicate<Response> bodyCondition) {
    this(
        Duration.ofMinutes(minutes).plusSeconds(seconds),
        Duration.ofMillis(pollMillis),
        expectedStatus,
        bodyCondition
    );
  }

  //-------------------------------------------------------------
  // filter implementation remains the same
  //-------------------------------------------------------------
  @Override
  public Response filter(FilterableRequestSpecification req,
      FilterableResponseSpecification res,
      FilterContext ctx) {

    final RequestExecutor executor = new RequestExecutor(req, res, ctx);
    final AtomicInteger attempt = new AtomicInteger(0);
    try {
      await()
          .atMost(timeout)
          .pollInterval(pollInterval)
         .ignoreExceptions()
          .until(() -> {
            attempt.incrementAndGet();
            Response response = executor.executeOnce();

            boolean statusOk = response != null && response.statusCode() == responseStatus;
            boolean bodyOk = bodyCondition == null || bodyCondition.test(response);

            // Log attempt
            String value = "N/A";

            try {
              if (bodyCondition != null && bodyPathForLogging != null) {
                Object v = response.jsonPath().get(bodyPathForLogging);
                value = v != null ? v.toString() : "null";
              }
            } catch (Exception e) {
              log.info("Retry attempt failed: {}", e.getMessage());
              return false;
            }
            log.info("[RetryFilter] Attempt #{} | status={} | JSONPath={} | value={} | expected={} | passed={}",
                attempt.get(),
                response != null ? response.statusCode() : "null",
                bodyPathForLogging != null ? bodyPathForLogging : "N/A",
                value,
                expectedValueForLogging != null ? expectedValueForLogging : "ANY",
                statusOk && bodyOk
            );

            return statusOk && bodyOk;
          });
    } catch (org.awaitility.core.ConditionTimeoutException e) {
      log.info("Retries exhausted, condition not fulfilled, but test continues.");
      return executor.getLastResponse();
    }
    return executor.getLastResponse();
  }

  private static class RequestExecutor {
    private final FilterableRequestSpecification req;
    private final FilterableResponseSpecification res;
    private final FilterContext ctx;
    private Response lastResponse;

    RequestExecutor(FilterableRequestSpecification req,
        FilterableResponseSpecification res,
        FilterContext ctx) {
      this.req = req;
      this.res = res;
      this.ctx = ctx;
    }

    Response executeOnce() {
      try {
        lastResponse = ctx.next(req, res);
        return lastResponse;
      } catch (Exception e) {
        lastResponse = null;
        throw e;
      }
    }

    Response getLastResponse() {
      return lastResponse;
    }
  }

  // ===== JSONPATH BODY ASSERT HELPERS =====
  private static JsonPath jp(Response r) {
    return r.then().extract().jsonPath();
  }

  public static RetryFilter jsonPathEquals(String jsonPath, Object expected) {
    return new RetryFilter(Duration.ofSeconds(30), Duration.ofMillis(500), 200,
        r -> {
          Object v = jp(r).get(jsonPath);
          return Objects.equals(String.valueOf(v), String.valueOf(expected));
        });
  }
  public static RetryFilter jsonPathEquals(
      int minutes,
      int seconds,
      int pollMillis,
      int expectedStatus,
      String jsonPath,
      Object expectedValue
  ) {
    return new RetryFilter(
        Duration.ofMinutes(minutes).plusSeconds(seconds),
        Duration.ofMillis(pollMillis),
        expectedStatus,
        resp -> {
          try {
            Object value = resp.jsonPath().get(jsonPath);
            return Objects.equals(value, expectedValue);
          } catch (Exception e) {
            return false;
          }
        }
    );
  }
  public static RetryFilter jsonPathExists(String jsonPath) {
    return new RetryFilter(Duration.ofSeconds(30), Duration.ofMillis(500), 200,
        r -> jp(r).get(jsonPath) != null);
  }

  public static RetryFilter jsonPathContains(String jsonPath, String containsText) {
    return new RetryFilter(Duration.ofSeconds(30), Duration.ofMillis(500), 200,
        r -> {
          Object v = jp(r).get(jsonPath);
          return v != null && v.toString().contains(containsText);
        });
  }

  public static RetryFilter jsonPathGreaterThan(String jsonPath, Number num) {
    return new RetryFilter(Duration.ofSeconds(30), Duration.ofMillis(500), 200,
        r -> {
          Number v = jp(r).get(jsonPath);
          return v != null && v.doubleValue() > num.doubleValue();
        });
  }

  public static RetryFilter jsonPathBoolean(String jsonPath) {
    return new RetryFilter(Duration.ofSeconds(30), Duration.ofMillis(500), 200,
        r -> Boolean.TRUE.equals(jp(r).get(jsonPath)));
  }



}



