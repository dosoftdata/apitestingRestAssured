package com.apitesting.core.base;

import com.apitesting.core.filters.DedupeRequestFilter;
import com.apitesting.core.filters.RequestDelayFilter;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import lombok.Data;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;

import static org.apache.http.client.params.ClientPNames.COOKIE_POLICY;
import static org.apache.http.client.params.CookiePolicy.BROWSER_COMPATIBILITY;

@Data
@SuppressWarnings("deprecation")
public class ApiSpecsMap {
    private static final CookieStore cookieStoreGBL = new BasicCookieStore();
    public static RequestSpecification requestSpec = new RequestSpecBuilder()
            .addFilter(new RequestDelayFilter(2000))
            .addFilter(new DedupeRequestFilter())
            .addFilter(new RequestLoggingFilter(LogDetail.ALL))
            .addFilter(new ResponseLoggingFilter(LogDetail.ALL)).build()
            .config(RestAssuredConfig.newConfig()
                    .httpClient(HttpClientConfig.httpClientConfig()
                    .httpClientFactory(() -> {
                        DefaultHttpClient httpClient = new DefaultHttpClient();
                        httpClient.setCookieStore(cookieStoreGBL);
                        return httpClient;
                    })
                    .setParam(COOKIE_POLICY, BROWSER_COMPATIBILITY)));
    private ApiSpecsMap (){}
}
