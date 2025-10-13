package com.apitesting.core;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import io.restassured.filter.Filter;
import io.restassured.http.ContentType;
import io.restassured.config.RestAssuredConfig;
import io.restassured.specification.ProxySpecification;
import io.restassured.specification.RequestSender;
import io.restassured.specification.ResponseSpecification;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomRequestSpec {

    private RequestSpecification spec;

    public CustomRequestSpec(RequestSpecification baseSpec) {
        this.spec = RestAssured.given().spec(baseSpec);
    }

    public CustomRequestSpec reset() {
        this.spec = RestAssured.given();
        return this;
    }

    public CustomRequestSpec setHeader(String name, String value) {
        spec.header(name, value);
        return this;
    }


    public CustomRequestSpec setBody(Object body) {
        spec.body(body);
        return this;
    }

    public CustomRequestSpec setQueryParam(String name, Object value) {
        spec.queryParam(name, value);
        return this;
    }

    public CustomRequestSpec setPathParam(String name, Object value) {
        spec.pathParam(name, value);
        return this;
    }

    public CustomRequestSpec setBaseUri(String baseUri) {
        spec.baseUri(baseUri);
        return this;
    }

    public CustomRequestSpec setBasePath(String basePath) {
        spec.basePath(basePath);
        return this;
    }
    public CustomRequestSpec setFormParam(String name, Object value) {
        spec.formParam(name, value);
        return this;
    }
    public CustomRequestSpec addMultiPartFile(String controlName, File file, String mimeType) {
        this.spec.multiPart(controlName, file, mimeType);
        return this;
    }
    public CustomRequestSpec addMultiPart(String controlName, String name, String mimeType) {
        this.spec.multiPart(controlName, name, mimeType);
        return this;
    }
    public CustomRequestSpec setRedirectFollow(Boolean value) {
        spec.redirects().follow(value);
        return this;
    }
    public CustomRequestSpec setRedirectCircular(Boolean value) {
        spec.redirects().allowCircular(value);
        return this;
    }
    public CustomRequestSpec setCookie(String name, String value) {
        spec.cookie(name, value);
        return this;
    }
    public CustomRequestSpec setFilter(Filter filter) {
        spec.filter(filter);
        return this;
    }

    public RequestSpecification build() {
        return spec;
    }

    // Delegate all RequestSpecification methods to spec
    public RequestSpecification header(String name, Object value) { return spec.header(name, value); }
    public RequestSpecification headers(Map<String, ?> headers) { return spec.headers(headers); }
    public RequestSpecification headers(String firstHeaderName, Object firstHeaderValue, Object... headerNameValuePairs) { return spec.headers(firstHeaderName, firstHeaderValue, headerNameValuePairs); }
    public RequestSpecification contentType(ContentType contentType) { return spec.contentType(contentType); }
    public RequestSpecification contentType(String contentType) { return spec.contentType(contentType); }
    public RequestSpecification accept(ContentType contentType) { return spec.accept(contentType); }
    public RequestSpecification accept(String contentType) { return spec.accept(contentType); }
    public RequestSpecification body(String body) { return spec.body(body); }
    public RequestSpecification body(Object body, Object... additionalObjects) { return spec.body(body); }
    public RequestSpecification param(String name, Object value) { return spec.param(name, value); }
    public RequestSpecification param(String name, Object... values) { return spec.param(name, values); }
    public RequestSpecification queryParam(String name, Object... values) { return spec.queryParam(name, values); }
    public RequestSpecification pathParam(String name, Object value) { return spec.pathParam(name, value); }
    public RequestSpecification formParam(String name, Object value) { return spec.formParam(name, value); }
    public RequestSpecification cookie(String name, String value) { return spec.cookie(name, value); }
    public RequestSpecification cookies(Map<String, ?> cookies) { return spec.cookies(cookies); }
    public RequestSpecification sessionId(String sessionIdValue) { return spec.sessionId(sessionIdValue); }
    public RequestSpecification config(RestAssuredConfig config) { return spec.config(config); }
    public RequestSpecification proxy(String host, int port) { return spec.proxy(host, port); }
    public RequestSpecification proxy(ProxySpecification proxySpecification) { return spec.proxy(proxySpecification); }
    public RequestSpecification filters(Collection<Filter> filters) { return spec.filters((List<Filter>) filters); }
    public RequestSpecification filter(Filter filter) { return spec.filter(filter); }
    public RequestSpecification baseUri(String uri) { return spec.baseUri(uri); }
    public RequestSpecification basePath(String path) { return spec.basePath(path); }
    public RequestSpecification port(int port) { return spec.port(port); }
    public RequestSpecification urlEncodingEnabled(boolean isEnabled) { return spec.urlEncodingEnabled(isEnabled); }
    public RequestSpecification relaxedHTTPSValidation() { return spec.relaxedHTTPSValidation(); }
    public RequestSpecification relaxedHTTPSValidation(String protocol) { return spec.relaxedHTTPSValidation(protocol); }
    public RequestSpecification trustStore(String pathToTrustStore, String password) { return spec.trustStore(pathToTrustStore, password); }
    public RequestSpecification multipart(String controlName, Object object) { return spec.multiPart(controlName, object); }
    public RequestSender when() { return spec.when(); }
    public ResponseSpecification response() { return spec.response(); }

}
