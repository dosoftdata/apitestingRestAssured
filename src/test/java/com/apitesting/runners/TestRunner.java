package com.apitesting.runners;


import com.github.tomakehurst.wiremock.WireMockServer;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import io.qameta.allure.testng.AllureTestNg;
import lombok.extern.slf4j.Slf4j;
import com.apitesting.core.helpers.WireMockFactory;
import com.apitesting.dsl.ScenarioContext;
import org.testng.annotations.*;

@Slf4j
@Listeners({AllureTestNg.class})
@CucumberOptions(
    plugin = {
        "pretty",
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
    },
    features = "classpath:Suites",
    glue = {
        "com.apitesting.dsl.actions",
        "com.apitesting.hooks"
    },
    tags = "@regression"
)
public class TestRunner extends AbstractTestNGCucumberTests {
  private static WireMockServer wireMockServer;
  @Override
  @DataProvider(parallel = false)
  public Object[][] scenarios() {
    return super.scenarios();
  }

  @BeforeSuite(alwaysRun = true)
  public void beforeSuite() {
      wireMockServer = WireMockFactory.getServer();
      ScenarioContext.set("mockServer", wireMockServer);
      ScenarioContext.set("mockServerUrl", wireMockServer.baseUrl());
      log.info("WireMock server started: {}", ScenarioContext.get("mockServerUrl"));
  }

  @BeforeClass(alwaysRun = true)
  public void beforeClass() {
    String tag = System.getProperty("cucumber.filter.tags", "");
    log.info("Running tests with tag: {}", tag != null ? tag : "No tag filter applied");
  }
  @AfterClass(alwaysRun = true)
  public void afterClass() {
  // ScenarioContext.clear();
  }

  @AfterSuite(alwaysRun = true)
  public void afterSuite() {
    if((boolean) ScenarioContext.get("wiremock")){
      WireMockFactory.stopServer();
      ScenarioContext.clear();
      log.info("WireMock server stopped.");
    }
  }
}