package com.apitesting.core;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequestDelayFilter implements Filter {
  private final long delayMillis;
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

  public RequestDelayFilter(long delayMillis) {
    this.delayMillis = delayMillis;
  }
  @Override
  public Response filter(FilterableRequestSpecification requestSpec,
      FilterableResponseSpecification responseSpec,
      FilterContext ctx) {
    try {
      // Schedule task and wait for completion without Thread.sleep()
      return scheduler.schedule(() -> ctx.next(requestSpec, responseSpec),
          delayMillis,
          TimeUnit.MILLISECONDS).get();
    } catch (Exception e) {
      log.error("Error applying delay filter", e);
    }
    return null;
  }
}
