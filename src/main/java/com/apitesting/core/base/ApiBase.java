package com.apitesting.core.base;

import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import lombok.Setter;


public class ApiBase{
    static {
        RestAssured.reset();
        RestAssured.requestSpecification= null;
        RestAssured.urlEncodingEnabled = true;
        RestAssured.responseSpecification =null;
        RestAssured.useRelaxedHTTPSValidation();
        io.restassured.RestAssured.defaultParser = Parser.JSON;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Getter
    private static RequestSpecification requestSpec = ApiSpecsMap.requestSpec;

    @Getter
    @Setter
    private Response response;

    private static final AtomicReference<CustomRequestSpec> specRef = new AtomicReference<>();

    public static CustomRequestSpec spec(){
      specRef.set(new CustomRequestSpec(requestSpec));
      return specRef.get();
    }

}
