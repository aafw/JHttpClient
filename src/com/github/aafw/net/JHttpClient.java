package com.github.aafw.net;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.WWWAuthenticationProtocolHandler;
import org.eclipse.jetty.client.http.HttpClientTransportOverHTTP;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;

import java.net.URI;

@SuppressWarnings("unused")
public class JHttpClient {
    private static HttpClient staticHttpClient = null;
    private static HttpField defaultUserAgent = new HttpField(HttpHeader.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36");

    public JHttpClient() {
    }

    /**
     * Set to null to disable the user-agent header
     * @param userAgent
     */
    public static void setDefaultUserAgent(String userAgent) {
        defaultUserAgent = (userAgent == null) ? null : new HttpField(HttpHeader.USER_AGENT, userAgent);
        staticHttpClient.setUserAgentField(defaultUserAgent);
    }

    public static Connection connect(String uri) {
        return connect(uri, false);
    }

    public static Connection connect(String uri, boolean trustAll) {
        try {
            URI uri2 = URI.create(uri);
            if (staticHttpClient == null) {
                HttpClientTransportOverHTTP transport = new HttpClientTransportOverHTTP();
                staticHttpClient = new HttpClient(transport);
                transport.setHttpClient(staticHttpClient);
                staticHttpClient.start();
                staticHttpClient.getProtocolHandlers().remove(WWWAuthenticationProtocolHandler.NAME);
                staticHttpClient.setUserAgentField(defaultUserAgent);
            }

            // https
            if (uri2.getScheme().toLowerCase().endsWith("s")) {
                HttpClient httpClient = JettyHttpClientHelper.getHttpClient(trustAll);
                httpClient.setUserAgentField(defaultUserAgent);
                return new Connection(httpClient, uri2, false);
            } else { // http
                return new Connection(staticHttpClient, uri2, true);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getCause());
        }
    }
}
