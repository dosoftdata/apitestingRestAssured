package com.apitesting.core;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.Message;

public class Log4jRestAssuredFilter implements Filter {

  private static final Logger logger = LogManager.getLogger(Log4jRestAssuredFilter.class);

  @Override
  public Response filter(FilterableRequestSpecification requestSpec,
      FilterableResponseSpecification responseSpec,
      FilterContext context) {

    logger.info("Request: {}()", requestSpec.getURI());
    logger.info("Headers: {}", requestSpec.getHeaders());
    logger.info("Body: {}", requestSpec.<Message>getBody());

    Response response = context.next(requestSpec, responseSpec);

    logger.info("Response Status Code: {}", Optional.of(response.getStatusCode()));
    logger.info("Response Headers: {}", response.getHeaders());
    logger.info("Response Body: {}", response.getBody().asPrettyString());

    return response;
  }
}
