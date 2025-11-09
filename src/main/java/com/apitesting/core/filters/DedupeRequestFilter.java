package com.apitesting.core.filters;

import com.apitesting.dsl.ScenarioContext;
import io.restassured.RestAssured;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import java.lang.reflect.Field;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DedupeRequestFilter implements Filter {

  @Override
  public Response filter(FilterableRequestSpecification req,
      FilterableResponseSpecification res,
      FilterContext ctx) {
            dedupeHeaders(req);
            dedupeQueryParams(req);
            dedupeFormParams(req);
            dedupePathParams(req);
            dedupeCookies(req);
            dedupeMultiparts(req);
    RestAssured.reset();
    return ctx.next(req, res);
  }

  private void dedupeHeaders(FilterableRequestSpecification req) {
    Map<String, Header> unique = new LinkedHashMap<>();
    for (Header h : req.getHeaders()) {
      unique.put(h.getName().toLowerCase(), h);
    }

    req.getHeaders().asList().forEach(h -> req.removeHeader(h.getName()));
    unique.values().forEach(req::header);
  }

  private void dedupeQueryParams(FilterableRequestSpecification req) {
    Map<String, List<String>> deduped = new LinkedHashMap<>();

    req.getQueryParams().forEach((k, v) -> {
      String value = String.valueOf(v);
      deduped.putIfAbsent(k, new ArrayList<>());
      if (!deduped.get(k).contains(value)) {
        deduped.get(k).add(value);
      }
    });

    // Remove all current query params by rebuilding spec
    // ⚠ RestAssured doesn't let us clear these directly, so we re-add only deduped
    req.getQueryParams().keySet().forEach(req::removeQueryParam);

    deduped.forEach((k, values) ->
        values.forEach(val -> req.queryParam(k, val))
    );
  }

  private void dedupeFormParams(FilterableRequestSpecification req) {
    Map<String, List<String>> deduped = new LinkedHashMap<>();

    req.getFormParams().forEach((k, v) -> {
      String value = String.valueOf(v);
      deduped.putIfAbsent(k, new ArrayList<>());
      if (!deduped.get(k).contains(value)) {
        deduped.get(k).add(value);
      }
    });

    req.getFormParams().keySet().forEach(req::removeFormParam);

    deduped.forEach((k, values) ->
        values.forEach(val -> req.formParam(k, val))
    );
  }


  private void dedupePathParams(FilterableRequestSpecification req) {
    Map<String, String> deduped = new LinkedHashMap<>();

    // Collect deduped values as Strings
    req.getPathParams().forEach((k, v) -> {
      String value = String.valueOf(v);
      deduped.putIfAbsent(k, value);
    });

    // Remove existing path params safely
    req.getPathParams().keySet().forEach(req::removePathParam);

    // Re-add only unique path params
    deduped.forEach(req::pathParam);
  }

  private void dedupeCookies(FilterableRequestSpecification req) {
    List<io.restassured.http.Cookie> cookies = new ArrayList<>(req.getCookies().asList());
    Map<String, io.restassured.http.Cookie> unique = new LinkedHashMap<>();

    for (io.restassured.http.Cookie c : cookies) {
      unique.put(c.getName().toLowerCase(), c);
    }

    cookies.forEach(c -> req.removeCookie(c.getName()));
    unique.values().forEach(c -> req.cookie(c.getName(), c.getValue()));
  }

  @SuppressWarnings("unchecked")
  private void dedupeMultiparts(FilterableRequestSpecification req) {
    try {
      Field field = req.getClass().getDeclaredField("multiPartSpecifications");
      field.setAccessible(true);

      List<Object> parts = (List<Object>) field.get(req);
      if (parts == null || parts.isEmpty()) return;

      Map<String, Object> unique = new LinkedHashMap<>();
      for (Object part : parts) {
        String name = (String) part.getClass().getMethod("getControlName").invoke(part);
        unique.put(name.toLowerCase(), part);
      }

      parts.clear();
      parts.addAll(unique.values());

    } catch (Exception ignored) {
      log.info("Multipart dedupe skipped — internal RestAssured structure may have changed");
    }
  }
}


