package com.apitesting.dsl.actions;

//import io.cucumber.java.ParameterType;
import com.apitesting.core.filters.RetryFilter;
import io.cucumber.java.en.*;
//import io.restassured.response.ValidatableResponse;
import io.restassured.response.ValidatableResponse;
import com.apitesting.core.filters.RequestDelayFilter;
import com.apitesting.dsl.*;

import java.io.File;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matcher;
import org.json.JSONException;
import org.json.JSONObject;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.assertTrue;

@Slf4j
public class commonSteps extends DslHelper {
    public commonSteps(ScenarioContext context) {super(context);}

    @When("^url (.+)")
    public void url(String expression) {
        this.context.getSpec().setBaseUri(ScenarioContext.resolve(expression));
    }
    @When("^delay request (\\d+)")
    public void delayRequest(int millisecond) {
        this.context.getSpec().setFilter(new RequestDelayFilter(millisecond));
    }

    @When("^path (.+)")
    public void path(String path) {
        this.context.getSpec().setBasePath(ScenarioContext.resolve(path));
    }

    @When("^param ([^\\s]+) = (.+)")
    public void param(String name, String value) {
        this.context.getSpec().setQueryParam(name, ScenarioContext.resolve(value));
    }
  @When("^pathParam ([^\\s]+) = (.+)")
  public void pathParam(String name, String value) {
    this.context.getSpec().setPathParam(name, ScenarioContext.resolve(value));
  }

    @When("^cookie ([^\\s]+) = (.+)")
    public void cookie(String name, String value) {
        this.context.getSpec().setCookie(name, ScenarioContext.resolve(value));
    }

    @When("^header ([^\\s]+) = (.+)")
    public void header(String name, String value) {
        this.context.getSpec().setHeader(name, ScenarioContext.resolve(value));
    }

    @When("^method (GET|POST|PUT|PATCH|DELETE)(?: \"([^\"]+)\")?$")
    public void method(String method, String path) {
        if (path != null) {
            this.context.getSpec().setBasePath(ScenarioContext.resolve(path));
        }
        this.context.getApi().setResponse(
                this.context.getRes().spec(this.context.getSpec().build())
                .when().request(method));
    }

    @When("^form field ([^\\s]+) = (.+)")
    public void formField(String name, Object value) {
        this.context.getSpec().setFormParam(name, ScenarioContext.resolve((String) value));
    }

    @When("^request$")
    public void requestDocstring(String body) {
        this.context.getSpec().setBody(ScenarioContext.resolve(body));
    }

    @When("^request (.+)")
    public void request(String body) { this.context.getSpec().setBody(ScenarioContext.resolve(body));}


    @When("^def (\\w+) = (.+)$")
    public void def(String name, String expression) {
        ScenarioContext.set(name,expression);
        context.setVariable(name, expression);
    }
    @When("^def (\\w+) in response.(.+)$")
    public void defResponse(String name, String expression) {
        String value = context.getApi().getResponse().jsonPath().getString(expression);
        ScenarioContext.set(name, value);
    }

    @When("^status (\\d+)")
    public void status(int status) {
       context.getApi().getResponse().then().statusCode(status);
    }
  @When("^status (\\d+) or (\\d+)")
  public void statusOr(int status, int or) {
    context.getApi().getResponse().then().statusCode(anyOf(is(status), is( or)));
  }
    @When("^assert (.+) = (.+)")
    public void assertEqual(String s1,String expression) {
          assertThat(s1, is(ScenarioContext.resolve(expression)));
    }

  @When("^redirect follow (\\d+)")
  public void redirectFollow(Integer b) {
    this.context.getSpec().setRedirectFollow(Boolean.valueOf(String.valueOf(b)));
  }
    @When("^redirect circular (\\d+)")
    public void redirectCircular(Integer b) {
        this.context.getSpec().setRedirectCircular(Boolean.valueOf(String.valueOf(b)));
    }
    @When("^retry (GET|POST|PUT|PATCH|DELETE)(?: \"([^\"]+)\")? until (.+) in response.(.*)$")
    public void retry(String method, String path, String until, String body) {
        if (path != null) {
            this.context.getSpec().setBasePath(ScenarioContext.resolve(path));
        }
              await()
               .atMost(60, TimeUnit.SECONDS)         // Total time to wait
                .pollInterval(5, TimeUnit.SECONDS)    // Custom poll interval
                .pollDelay(2, TimeUnit.SECONDS)       // Optional initial delay
                .until(() -> {
                    this.context.getApi().setResponse(
                            this.context.getRes().spec(this.context.getSpec().build())
                                    .when().request(method));
                    return this.context.getApi().getResponse().path(body).toString().equalsIgnoreCase(until);
                });

    }

  @When("^multipart file (.+) path (.+) mimeType (.+)")
  public void multipartFile(String file, File filePath, String mimeType) {
        this.context.getSpec().addMultiPartFile(file, filePath, mimeType);
  }
    @When("^multipart (.+), (.+), (.+)$")
    public void multipart(String file, String filePath, String mimeType) {
        this.context.getSpec().addMultiPart(file, filePath, mimeType);
    }
  @Then("^match$")
  public void match(String expected) {
    JsonMatcherResult result = JsonMatcher.matchJson(this.context.getApi().getResponse().asString(), ScenarioContext.resolve(expected));
    assertTrue(result.isSuccess(), result.getMessage());
  }

  @Then("^match path (.+) should be (.+)$")
  public void matchPath(String path, String expr) {
    JsonMatcherResult result = JsonMatcher.matchJson(this.context.getApi().getResponse().asString(), path + " " + ScenarioContext.resolve(expr));
    assertTrue(result.isSuccess(), result.getMessage());
  }

  @Then("^match each (.+) should match$")
  public void match_each(String arrayPath, String expected) {
    JsonMatcherResult result = JsonMatcher.matchEach(this.context.getApi().getResponse().asString(), arrayPath, ScenarioContext.resolve(expected));
    assertTrue(result.isSuccess(), result.getMessage());
  }

  @Then("^match response body path (.+) should be (.+) (.+)$")
  public void match_response_body_path(String path, String matcherName, String expected) {
    ValidatableResponse response = context.getApi().getResponse().then();
    Matcher<?> matcher = buildMatcher(matcherName, expected);
    response.body(path, matcher);
  }

  @And("wait until JSON path {string} equals {string} with timeout {int} minutes {int} seconds polling {int} ms and status {int}")
  public void WaitUntilJsonPathEquals(
      String jsonPath,
      String expected,
      int minutes,
      int seconds,
      int pollMillis,
      int expectedStatus
  ) {
    String rawValue = expected;
    Object expectedValue;
    // Try to parse integer
    try {
      expectedValue = Integer.parseInt(rawValue);
    } catch (NumberFormatException e1) {
      // Try boolean
      if ("true".equalsIgnoreCase(rawValue)) {
        expectedValue = true;
      } else if ("false".equalsIgnoreCase(rawValue)) {
        expectedValue = false;
      } else {
        // Default: treat as string
        expectedValue = rawValue;
      }
    }
    RetryFilter filterRetry = RetryFilter.jsonPathEquals(
        minutes,
        seconds,
        pollMillis,
        expectedStatus,
        jsonPath,
        expectedValue
    );

    context.getSpec().filter(filterRetry);

  }
  @And("^retry until equal$")
  public void retryUntil(String docString) throws JSONException {
    // Parse docString as JSON
    JSONObject json = new JSONObject(docString);

    String bodyPath = json.optString("bodyPath", "$"); // default JSONPath
    Object hasValue = json.has("hasValue") ? json.get("hasValue") : null;
    int minutes = json.optInt("minutes", 0);
    int seconds = json.optInt("seconds", 30);
    int status = json.optInt("status", 200);
    int pollMillis = json.optInt("pollMillis", 500); // optional, default 500ms

    // If hasValue is empty string, treat as null
    if (hasValue instanceof String && ((String) hasValue).isEmpty()) {
      hasValue = null;
    }

    RetryFilter filter;

    if (hasValue != null) {
      // Use JSONPath + expected value
      filter = RetryFilter.jsonPathEquals(minutes, seconds, pollMillis, status, bodyPath, hasValue);
    } else {
      // Only check status
      filter = new RetryFilter(minutes, seconds, pollMillis, status);
    }
    // Attach filter to RestAssured request
    context.getSpec().filter(filter);

  }
}
