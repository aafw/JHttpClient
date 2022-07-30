package com.github.aafw.net;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpProxy;
import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.client.ProxyConfiguration;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.*;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Fields;

import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@SuppressWarnings("unused")
public class Connection {
    private final HttpClient httpClient;
    private final boolean clientReusable;

    private final HttpRequest req;
    private final HttpFields.Mutable headers;
    private final List<ProxyConfiguration.Proxy> proxies;

    private final Fields data;

    protected Connection(HttpClient httpClient, URI uri, boolean clientReusable) {
        this.httpClient = httpClient;
        this.clientReusable = clientReusable;

        req = (HttpRequest) httpClient.newRequest(uri);
        headers = (HttpFields.Mutable) req.getHeaders();
        proxies = httpClient.getProxyConfiguration().getProxies();

        data = new Fields();
    }

    public Connection header(String name, String value) {
        if(name == null) {
            throw new NullPointerException("header's name can't be null.");
        }

        if (headers.contains(name)) {
            headers.remove(name);
        }
        if (value != null) {
            headers.add(name, value);
        }
        return this;
    }

    public Connection headers(Map<String, String> headers) {
        if(headers == null) {
            throw new NullPointerException("headers can't be null.");
        }

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            this.headers.add(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public Connection cookie(String name, String value) {
        if(name == null) {
            throw new NullPointerException("cookie's name can't be null.");
        }
        req.cookie(new HttpCookie(name, value));
        return this;
    }

    public List<HttpCookie> cookies() {
        return req.getCookies();
    }

    public Connection cookies(Map<String, String> cookies) {
        if(cookies == null) {
            throw new NullPointerException("cookies can't be null.");
        }

        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            req.cookie(new HttpCookie(entry.getKey(), entry.getValue()));
        }
        return this;
    }

    public Connection cookies(List<HttpCookie> cookies) {
        if(cookies == null) {
            throw new NullPointerException("cookies can't be null.");
        }
        for (HttpCookie cookie : cookies) {
            if(cookie == null) {
                throw new NullPointerException("cookie can't be null");
            }
            req.cookie(cookie);
        }
        return this;
    }

    public Connection proxy(ProxyConfiguration.Proxy proxy) {
        if(proxy == null) {
            throw new NullPointerException("proxy can't be null.");
        }
        proxies.add(proxy);
        return this;
    }

    public Connection proxy(String host, int port) {
        proxies.add(new HttpProxy(host, port));
        return this;
    }

    public Connection method(HttpMethod method) {
        req.method(method);
        return this;
    }

    public Connection method(String method) {
        req.method(method);
        return this;
    }

    public String method() {
        return req.getMethod();
    }

    public Connection followRedirects(boolean follow) {
        req.followRedirects(follow);
        return this;
    }

    public Connection timeout(long millis) {
        req.timeout(millis, TimeUnit.MILLISECONDS);
        return this;
    }

    public CookieStore cookieStore() {
        return httpClient.getCookieStore();
    }

    public Connection cookieStore(CookieStore cookieStore) {
        httpClient.setCookieStore(cookieStore);
        return this;
    }

    public Connection userAgent(String userAgent) {
        header(HttpHeader.USER_AGENT.asString(), userAgent);
        return this;
    }

    public Connection referrer(String referrer) {
        header("Referer", referrer);
        return this;
    }

    public Connection data(String name, String value) {
        if(name == null) {
            throw new NullPointerException("data's name can't be null.");
        }
        data.add(name, value);
        return this;
    }

    public Connection requestBody(String body) {
        req.body(new StringRequestContent(body));

        if(!data.isEmpty())
            data.clear();
        return this;
    }

    public Response execute() throws ExecutionException, InterruptedException, TimeoutException {
        ContentResponse cResp = sendRequest();
        try {
            return new Response(cResp, req.getMethod(), cResp.getRequest().getURI().toURL()); // get url after following redirections
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null; // will never happen
    }

    private ContentResponse sendRequest() throws ExecutionException, InterruptedException, TimeoutException {
        if(req.getMethod().equals(HttpMethod.POST.asString())) {
            if(!data.isEmpty())
                req.body(new FormRequestContent(data));
        }

        ContentResponse cResp = req.send();
        if (!clientReusable) {
            try {
                httpClient.stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return cResp;
    }

    public String get() throws ExecutionException, InterruptedException, TimeoutException {
        req.method(HttpMethod.GET);
        return sendRequest().getContentAsString();
    }

    public String post() throws ExecutionException, InterruptedException, TimeoutException {
        req.method(HttpMethod.POST);
        return sendRequest().getContentAsString();
    }
}
