package com.apitesting.hooks;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.cucumber.java.*;
import lombok.extern.slf4j.Slf4j;
import com.apitesting.config.ConfigLoader;
import com.apitesting.core.*;
import com.apitesting.dsl.ScenarioContext;

@Slf4j
public class Hooks  {
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
            ScenarioContext.set("mockServerUrl", wireMockServer.baseUrl());
            ScenarioContext.set("mockServer", wireMockServer);
          }
        beforeAllisInitialized = true;
      }
    }
    @Before(order = 0)
    public void loadConfig() {
        ConfigLoader.loadConfig();
        customSpec.reset(); // resets to a fresh spec
    }
    @After(order = 0)
    public void after(Scenario scenario) {
        if (scenario.isFailed()) {
           // Allure.addAttachment("Failure Response", "application/json",
             //       new ByteArrayInputStream(this.context.getApi().getResponse().asByteArray()), ".json");
        }
    }
   @AfterAll
    public static void afterAll() {
     if (!afterAllisInitialized) {
         log.info("=== ".concat((String) ScenarioContext.get("mockServerUrl")));
         WireMockFactory.stopServer();
       log.info("===== end scenario ====");
       afterAllisInitialized = true;
     }

   }

}

