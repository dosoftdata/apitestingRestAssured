package com.apitesting.dsl;

import com.apitesting.core.AllureRequestResponseFilter;
import com.apitesting.core.Log4jRestAssuredFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import io.restassured.specification.RequestSpecification;
import lombok.*;
import com.apitesting.core.ApiBase;
import com.apitesting.core.CustomRequestSpec;

import static io.restassured.RestAssured.given;


@Getter
public class ScenarioContext {
  private static final ThreadLocal<Map<String, Object>> context = ThreadLocal.withInitial(HashMap::new);

  @Setter
  private ApiBase api;
  private RequestSpecification res;
  CustomRequestSpec spec;

  public ScenarioContext() {
    api = new ApiBase();

    res = given()
        .filter(new AllureRequestResponseFilter())
        .filter(new Log4jRestAssuredFilter());

    spec = new CustomRequestSpec( getApi().getRequestSpec());
  }
  public static void set(String key, Object value) {
    context.get().put(key, value);
  }

  public static Object get(String key) {
    return context.get().get(key);
  }

  public static void clear() {
    context.get().clear();
  }

  public static Map<String, Object> getAll() {
    return context.get();
  }

  /**
   * Resolve placeholders in a string using context variables.
   * Supports <var> and #(var) syntax.
   */
  public static String resolve(String text) {
    if (text == null) return null;

    // <var> style
    Pattern anglePattern = Pattern.compile("<(.*?)>");
    Matcher angleMatcher = anglePattern.matcher(text);
    StringBuffer sb = new StringBuffer();
    while (angleMatcher.find()) {
      String key = angleMatcher.group(1);
      Object value = get(key);
      angleMatcher.appendReplacement(sb, value != null ? value.toString() : "");
    }
    angleMatcher.appendTail(sb);
    text = sb.toString();

    // #(var) style
    Pattern hashPattern = Pattern.compile("#\\((.*?)\\)");
    Matcher hashMatcher = hashPattern.matcher(text);
    sb = new StringBuffer();
    while (hashMatcher.find()) {
      String key = hashMatcher.group(1);
      Object value = get(key);
      hashMatcher.appendReplacement(sb, value != null ? value.toString() : "");
    }
    hashMatcher.appendTail(sb);

    return sb.toString();
  }

  private final Map<String, Object> variables = new HashMap<>();

  public void setVariable(String name, Object value) {
    variables.put(name, value);
  }

  public Object getVariable(String name) {
    return variables.get(name);
  }

}
