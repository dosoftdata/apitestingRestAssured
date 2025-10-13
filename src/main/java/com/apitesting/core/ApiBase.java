package com.apitesting.core;

import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ApiBase{
    static {
        // Disable SSL verification globally for all requests
        io.restassured.RestAssured.useRelaxedHTTPSValidation();
        io.restassured.RestAssured.defaultParser = Parser.JSON;
    }
    @Setter
    private RequestSpecification requestSpec = ApiSpecsMap.requestSpec;
    @Setter
    private Response response;
}
