package com.apitesting.core;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

public class WireMockFactory {
    private static WireMockServer wireMockServer;

    public static WireMockServer getServer() {
        if (wireMockServer == null) {
            wireMockServer = new WireMockServer(
                WireMockConfiguration
                    .options()
                    .dynamicPort()
                    .usingFilesUnderDirectory("src/test/resources/wiremock")
                    .extensions(new com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer(true))

            );
            wireMockServer.start();
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
