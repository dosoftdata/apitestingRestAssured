package com.apitesting.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AddAStub {
  private String scenarioName;
  private String requiredScenarioState;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private int priority;

  private Request request;
  private Response response;
  @Data
  public static class Request {
    public String method;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String url;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String urlPattern;
  }

  @Data
  public static class headers {
    @JsonProperty("Content-Type")
    private String contentType;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String GUID;
  }

  @Data
  public static class Response {
    private int status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String body;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private headers headers;
  }
}
