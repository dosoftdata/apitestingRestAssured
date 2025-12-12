
package com.apitesting.core.helpers;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class WireMockFactoryOnce {

  private static final AtomicReference<WireMockServer> GLOBAL = new AtomicReference<>();
  private static final AtomicInteger ACTIVE_USERS = new AtomicInteger(0);

  private WireMockFactoryOnce() {}

  public static WireMockServer getServer() {
    WireMockServer s = GLOBAL.get();
    if (s == null || !s.isRunning()) {
      synchronized (WireMockFactoryOnce.class) {
        s = GLOBAL.get();
        if (s == null || !s.isRunning()) {
          s = new WireMockServer(
              WireMockConfiguration.options()
                  .dynamicPort()
                  .usingFilesUnderDirectory("src/test/resources/wiremock")
                  .extensions(new ResponseTemplateTransformer(true))
          );
          s.start();
          WireMock.configureFor("localhost", s.port());
//          WireMock.resetAllScenarios();
          GLOBAL.set(s);
          log.info("WireMock (singleton) started on port {}", s.port());
        }
      }
    }
    return s;
  }

  /** increments usage count; call in @BeforeAll */
  public static void retain() {
    int n = ACTIVE_USERS.incrementAndGet();
    log.debug("WireMock retain -> users={}", n);
  }

  /** decrements usage count and stops server when zero; call in @AfterAll */
  public static void release() {
    int n = ACTIVE_USERS.decrementAndGet();
    log.debug("WireMock release -> users={}", n);
    if (n <= 0) {
      synchronized (WireMockFactoryOnce.class) {
        WireMockServer s = GLOBAL.getAndSet(null);
        if (s != null && s.isRunning()) {
          s.stop();
          log.info("WireMock (singleton) stopped");
        }
        ACTIVE_USERS.set(0);
      }
    }
  }

  public static int getPort() {
    WireMockServer s = GLOBAL.get();
    return (s != null && s.isRunning()) ? s.port() : 0;
  }
}

