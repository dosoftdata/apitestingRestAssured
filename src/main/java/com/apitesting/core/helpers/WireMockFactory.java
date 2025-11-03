package com.apitesting.core.helpers;

import static com.github.tomakehurst.wiremock.client.WireMock.resetAllScenarios;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;

public class WireMockFactory {
    private static WireMockServer wireMockServer;

    public static WireMockServer getServer() {
        if (wireMockServer == null) {
            wireMockServer = new WireMockServer(
                WireMockConfiguration
                    .options()
                    .dynamicPort()
                    .usingFilesUnderDirectory("src/test/resources/wiremock")
                    .extensions(new ResponseTemplateTransformer(true))

            );
            wireMockServer.start();
            WireMock.configureFor("localhost", wireMockServer.port());
            WireMock.resetAllScenarios();
        }
        return wireMockServer;
    }

    public static void stopServer() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    public static int getPort() {
        if (wireMockServer.isRunning()) {
            return wireMockServer.port();
        }
        return 0;
    }
}
