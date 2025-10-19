package com.apitesting.core.filters;

import io.qameta.allure.Allure;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

public class AllureRequestResponseFilter implements Filter {

  @Override
  public Response filter(FilterableRequestSpecification requestSpec,
      FilterableResponseSpecification responseSpec,
      FilterContext ctx) {

    Response response = ctx.next(requestSpec, responseSpec);

    Allure.addAttachment("Request URI", requestSpec.getURI());
    Allure.addAttachment("Request Method", requestSpec.getMethod());
    Allure.addAttachment("Request Headers", requestSpec.getHeaders().toString());
    Allure.addAttachment("Request Body", requestSpec.getBody() != null ? requestSpec.getBody().toString() : "No body");

    Allure.addAttachment("Response Status", String.valueOf(response.getStatusCode()));
    Allure.addAttachment("Response Headers", response.getHeaders().toString());
    Allure.addAttachment("Response Body", response.getBody().asPrettyString());

    return response;
  }
}
