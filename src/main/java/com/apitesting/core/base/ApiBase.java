package com.apitesting.core.base;

import static io.restassured.RestAssured.given;

import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import lombok.Setter;


public class ApiBase{
    static {
        // Disable SSL verification globally for all requests
        RestAssured.requestSpecification= null;
        //RestAssured.filters( new ResetAfterRequestFilter());
        RestAssured.urlEncodingEnabled = true;
        RestAssured.responseSpecification =null;
        RestAssured.useRelaxedHTTPSValidation();
        io.restassured.RestAssured.defaultParser = Parser.JSON;
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
    public static CustomRequestSpec givenDefaultSpec(){
      specRef.set(spec());
      specRef.get().given().spec(requestSpec);
      return specRef.get();
    }
}
