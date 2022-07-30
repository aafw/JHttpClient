package com.github.aafw.net;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.client.WWWAuthenticationProtocolHandler;
import org.eclipse.jetty.client.dynamic.HttpClientTransportDynamic;
import org.eclipse.jetty.client.http.HttpClientConnectionFactory;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.ClientConnectionFactoryOverHTTP2;
import org.eclipse.jetty.io.ClientConnectionFactory;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.PrintStream;

public class JettyHttpClientHelper {
    protected static HttpClient getHttpClient(boolean trustAll) throws Exception {
        ClientConnector clientConnector = new ClientConnector();

        if(trustAll) {
            // Configure the clientConnector.
            SslContextFactory.Client sslContextFactory = new SslContextFactory.Client(true);
            clientConnector.setSslContextFactory(sslContextFactory);
        }

        // Prepare the application protocols.
        ClientConnectionFactory.Info h1 = HttpClientConnectionFactory.HTTP11;
        HTTP2Client http2Client = new HTTP2Client(clientConnector);
        ClientConnectionFactory.Info h2 = new ClientConnectionFactoryOverHTTP2.HTTP2(http2Client);
//        HTTP3Client http3Client = new HTTP3Client();
//        ClientConnectionFactory.Info h3 = new ClientConnectionFactoryOverHTTP3.HTTP3(http3Client);

        // Create the HttpClientTransportDynamic, preferring h3, h2 over h1.
        HttpClientTransport transport = new HttpClientTransportDynamic(clientConnector, h2, h1);
        HttpClient httpClient = new HttpClient(transport);
        startHttpClient(httpClient, trustAll);
        httpClient.getProtocolHandlers().remove(WWWAuthenticationProtocolHandler.NAME);
        return httpClient;
    }

    private static HttpClient startHttpClient(HttpClient httpClient, boolean ignoreError) throws Exception {
        if(ignoreError) {
            PrintStream oldErr =  System.err;
            PrintStream filterOut = new PrintStream(System.err) {
                public void println(String ignore) {}
            };
            System.setErr(filterOut);
            httpClient.start(); // ignore trusting all certs warnings
            System.setErr(oldErr);
        } else {
            httpClient.start();
        }
        return httpClient;
    }
}
