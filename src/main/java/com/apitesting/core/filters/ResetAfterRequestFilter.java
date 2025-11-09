package com.apitesting.core.filters;

import com.apitesting.dsl.ScenarioContext;
import io.restassured.RestAssured;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import java.util.HashSet;
import java.util.Map;

public class ResetAfterRequestFilter implements Filter {

  @Override
  public Response filter(FilterableRequestSpecification req,
      FilterableResponseSpecification res,
      FilterContext ctx) {

    // Execute the actual request first
    // Clear request-specific data only (no global reset)


    Response response = ctx.next(req, res);

    clearRequestState(req);
    return response;
  }

  private void clearRequestState(FilterableRequestSpecification req) {
      String method = req.getMethod() != null ? req.getMethod().toUpperCase() : "";
      String uri = req.getURI() != null ? req.getURI() : "";

      // 1. Clear body for methods that don't use it
      if (method.equals("GET") || method.equals("DELETE")) {
        if (req.getBody() != null) {
          req.body(""); // safely remove body
        }
      }

    // 2. Clear headers
    new HashSet<>(req.getHeaders().asList()).forEach(h -> req.removeHeader(h.getName()));

    // 3. Clear cookies
  //  new HashSet<>(req.getCookies().keySet()).forEach(req::removeCookie);

    // 4. Clear query params
    new HashSet<>(req.getQueryParams().keySet()).forEach(req::removeQueryParam);

    // 5. Clear path params for ALL requests
    new HashSet<>(req.getPathParams().keySet()).forEach(req::removePathParam);
    }
}

