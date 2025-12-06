package com.apitesting.hooks;

import static com.github.tomakehurst.wiremock.client.WireMock.resetAllScenarios;

import com.apitesting.core.base.ApiSpecsMap;
import com.apitesting.core.base.CustomRequestSpec;
import com.apitesting.core.filters.ResetAfterRequestFilter;
import com.apitesting.core.helpers.WireMockFactory;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.cucumber.java.*;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import com.apitesting.config.ConfigLoader;
import com.apitesting.dsl.ScenarioContext;
//import plugins.externalTestData.PreprocessingFeatureSupplier;
//import com.qmetry.qaf.automation.cucumber.QAFCucumberPlugin;
//import static com.qmetry.qaf.automation.core.ConfigurationManager.getBundle;
@Slf4j
public class Hooks  {
  static {

    log.info(Hooks.class.toString());
  }
    private CustomRequestSpec customSpec =  new CustomRequestSpec(ApiSpecsMap.requestSpec);
    static WireMockServer wireMockServer;
    private static boolean beforeAllisInitialized = false;
    private static boolean afterAllisInitialized = false;

  @BeforeAll
    public static void beforeAll() {
      if (!beforeAllisInitialized) {
        log.info("===== starting scenario ====");
          wireMockServer = WireMockFactory.getServer();
          if (wireMockServer.isRunning()) {
           // resetAllScenarios();
            ScenarioContext.set("mockServerUrl", wireMockServer.baseUrl());
            ScenarioContext.set("mockServer", wireMockServer);
          }
        beforeAllisInitialized = true;
      }
    }
    @Before(order = 0)
    public void loadConfig() {
        ConfigLoader.loadConfig();
        log.info(String.valueOf(ScenarioContext.getAll()));
        RestAssured.filters( new ResetAfterRequestFilter());
        customSpec.reset(); // resets to a fresh spec

    }
    @After(order = 0)
    public void after(Scenario scenario) {
      WireMock.resetAllScenarios();
      customSpec.reset(); // resets to a fresh spec
        if (scenario.isFailed()) {
           // Allure.addAttachment("Failure Response", "application/json",
             //       new ByteArrayInputStream(this.context.getApi().getResponse().asByteArray()), ".json");
        }
    }
   @AfterAll
    public static void afterAll() {
     if (!afterAllisInitialized) {
       //  log.info("=== ".concat((String) ScenarioContext.get("mockServerUrl")));
         WireMockFactory.stopServer();
       log.info("===== end scenario ====");
       afterAllisInitialized = true;
     }

   }

}

