package com.apitesting.dsl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class JsonMatcherResult {
  private final boolean success;
  private final String message;

  public JsonMatcherResult(boolean success, String message) {
    this.success = success;
    this.message = message;
  }

  public static JsonMatcherResult success() {
    return new JsonMatcherResult(true, "Match successful");
  }

  public static JsonMatcherResult fail(String msg) {
    log.info(msg);
    return new JsonMatcherResult(false, msg);
  }

  public static JsonMatcherResult pass(String msg) {
    log.info(msg);
    return new JsonMatcherResult(true, msg);
  }

}


