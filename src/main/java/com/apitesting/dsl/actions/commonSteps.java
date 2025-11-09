package com.apitesting.dsl.actions;

import com.apitesting.core.base.ApiBase;
import com.apitesting.core.base.CustomRequestSpec;
import io.cucumber.java.en.*;
import io.restassured.response.ValidatableResponse;
import com.apitesting.dsl.*;
import java.io.File;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.*;
import static org.testng.Assert.assertTrue;

@Slf4j
public class commonSteps extends DslBase {
  private final CustomRequestSpec spec = ApiBase.givenDefaultSpec();
    public commonSteps(ScenarioContext context) {
      super(context);

    }
  @And("Retrieve a list of all authors")
  public void authorsList(){
    spec.baseUri("https://fakerestapi.azurewebsites.net");
    spec.basePath("/api/v1/Authors");
    spec.header("Content-Type"," application/json");
    context.getApi().setResponse(spec.when().request("GET"));
    context.getRes().then().statusCode(200);
    String value = context.getApi().getResponse().jsonPath().getString("[1].id");
    ScenarioContext.set("id", value);
    }
    @When("^url (.+)")
    public void stepUrl(String expression) {
       url(expression);
    }
    @When("^delay request (\\d+)")
    public void stepDelayRequest(int millisecond) {
        delayRequest(millisecond);
    }

    @When("^path (.+)")
    public void stepPath(String path) {
      path(path);
    }

    @When("^param ([^\\s]+) = (.+)")
    public void stepParam(String name, String value) {
          param(name,value);
    }
  @When("^pathParam ([^\\s]+) = (.+)")
  public void stepPathParam(String name, String value) {
    this.context.getSpec().setPathParam(name, ScenarioContext.resolve(value));
  }
    @When("^cookie ([^\\s]+) = (.+)")
    public void stepcookie(String name, String value) {
        this.context.getSpec().setCookie(name, ScenarioContext.resolve(value));
    }

    @When("^header ([^\\s]+) = (.+)")
    public void stepHeader(String name, String value) {
        this.context.getSpec().setHeader(name, ScenarioContext.resolve(value));
    }

    @When("^method (GET|POST|PUT|PATCH|DELETE)(?: \"([^\"]+)\")?$")
    public void stepmethod(String method, String path) {
        if (path != null) {
            this.context.getSpec().setBasePath(ScenarioContext.resolve(path));
        }
        this.context.getApi().setResponse(
                this.context.getRes().spec(this.context.getSpec().build())
                .when().request(method));
    }

    @When("^form field ([^\\s]+) = (.+)")
    public void stepformField(String name, Object value) {
        this.context.getSpec().setFormParam(name, ScenarioContext.resolve((String) value));
    }

    @When("^request$")
    public void steprequestDocstring(String body) {
        this.context.getSpec().setBody(ScenarioContext.resolve(body));
    }

    @When("^request (.+)")
    public void steprequest(String body) { this.context.getSpec().setBody(ScenarioContext.resolve(body));}


    @When("^def (\\w+) = (.+)$")
    public void stepdef(String name, String expression) {
        ScenarioContext.set(name,expression);
        context.setVariable(name, expression);
    }
    @When("^def (\\w+) in response.(.+)$")
    public void stepdefResponse(String name, String expression) {
        String value = context.getApi().getResponse().jsonPath().getString(expression);
        ScenarioContext.set(name, value);
    }

    @When("^status (\\d+)")
    public void stepstatus(int status) {
       context.getApi().getResponse().then().statusCode(status);
    }
  @When("^status (\\d+) or (\\d+)")
  public void stepstatusOr(int status, int or) {
    context.getApi().getResponse().then().statusCode(anyOf(is(status), is( or)));
  }
    @When("^assert (.+) = (.+)")
    public void stepassertEqual(String s1,String expression) {
          assertEqual(s1, ScenarioContext.resolve(expression));
    }

  @When("^redirect follow (\\d+)")
  public void stepredirectFollow(Integer b) {
    this.context.getSpec().setRedirectFollow(Boolean.valueOf(String.valueOf(b)));
  }
    @When("^redirect circular (\\d+)")
    public void stepredirectCircular(Integer b) {
        this.context.getSpec().setRedirectCircular(Boolean.valueOf(String.valueOf(b)));
    }
    @SneakyThrows
    @When("^retry (GET|POST|PUT|PATCH|DELETE)\\s+([^\\s]+)? until condition$")
    public void stepretryDocString(String method, String path, String conditionJson){
      awaitUntil(method, path, conditionJson);
    }
    @When("^retry (GET|POST|PUT|PATCH|DELETE)\\s+([^\\s]+)? until condition (.*)$")
    public void stepretry(String method, String path,  String conditionJson){
      awaitUntil(method, path, conditionJson);
    }

  @When("^multipart file (.+) path (.+) mimeType (.+)")
  public void stepmultipartFile(String file, File filePath, String mimeType) {
        this.context.getSpec().addMultiPartFile(file, filePath, mimeType);
  }
    @When("^multipart (.+), (.+), (.+)$")
    public void stepmultipart(String file, String filePath, String mimeType) {
        this.context.getSpec().addMultiPart(file, filePath, mimeType);
    }
  @Then("^match$")
  public void stepmatch(String expected) {
    JsonMatcherResult result = JsonMatcher.matchJson(this.context.getApi().getResponse().asString(), ScenarioContext.resolve(expected));
    assertTrue(result.isSuccess(), result.getMessage());
  }

  @Then("^match path (.+) should be (.+)$")
  public void stepmatchPath(String path, String expr) {
    JsonMatcherResult result = JsonMatcher.matchJson(this.context.getApi().getResponse().asString(), path + " " + ScenarioContext.resolve(expr));
    assertTrue(result.isSuccess(), result.getMessage());
  }

  @Then("^match each (.+) should match$")
  public void stepmatch_each(String arrayPath, String expected) {
    JsonMatcherResult result = JsonMatcher.matchEach(this.context.getApi().getResponse().asString(), arrayPath, ScenarioContext.resolve(expected));
    assertTrue(result.isSuccess(), result.getMessage());
  }

  @Then("^match response body path (.+) should be (.+) (.+)$")
  public void stepmatch_response_body_path(String path, String matcherName, String expected) {
    ValidatableResponse response = context.getApi().getResponse().then();
    Matcher<?> matcher = buildMatcher(matcherName, expected);

    response.body(path, matcher);


  }
}
