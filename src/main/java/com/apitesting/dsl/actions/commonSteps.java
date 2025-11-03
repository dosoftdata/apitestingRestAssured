package com.apitesting.dsl.actions;

import com.apitesting.core.base.CustomRequestSpec;
import io.cucumber.java.en.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import com.apitesting.core.filters.RequestDelayFilter;
import com.apitesting.dsl.*;
import java.io.File;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matcher;
import org.json.JSONException;

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
    @When("^retry (GET|POST|PUT|PATCH|DELETE)\\s+([^\\s]+)? until condition$")
    public void retry(String method, String path, String conditionJson) throws JSONException {
      // Resolve path
      if (path != null) {
        path = ScenarioContext.resolve(path);
      }
      JsonPath json = new JsonPath(conditionJson);
      Object expected = json.get("hasValue"); // can be String, Number, Boolean, null
      String bodyPath = json.getString("bodyPath");
      String action   = json.getString("action") != null ? json.getString("action") : "equal";

      Integer minutes        = json.get("minutes")       != null ? json.getInt("minutes") : 1;
      Integer seconds        = json.get("seconds")       != null ? json.getInt("seconds") : 30;
      Integer pollMillis     = json.get("pollMillis")    != null ? json.getInt("pollMillis") : 2000;
      Integer expectedStatus = json.get("status")        != null ? json.getInt("status") : 200;

      CustomRequestSpec originalSpec = this.context.getSpec();
      AtomicReference<Response> lastResp = new AtomicReference<>();

      String finalPath = path;
      await()
          .atMost(Duration.ofMinutes(minutes).plusSeconds(seconds))
          .pollInterval(Duration.ofMillis(pollMillis))
          .ignoreExceptions()
          .until(() -> {
            Response resp = context.getRes()
                .spec(originalSpec.build())
                .when().request(method, finalPath);

            lastResp.set(resp);

            boolean statusOk = resp.statusCode() == expectedStatus;

            Object value = null;
            if (bodyPath != null) {
              try { value = resp.path(bodyPath); } catch (Exception ignored) {}
            }

            boolean bodyOk = (bodyPath == null) || evaluateCondition(action, value, expected);

            log.info("[Retry] {} {} | status={} | {}={} | passed={}",
                method, finalPath, resp.statusCode(), bodyPath, value, statusOk && bodyOk);

            return statusOk && bodyOk;
          });

      this.context.getApi().setResponse(lastResp.get());
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
}
