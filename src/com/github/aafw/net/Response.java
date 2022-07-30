package com.github.aafw.net;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpVersion;

import java.net.URL;

/**
 * Response From HTTP Connection
 */
@SuppressWarnings("unused")
public class Response {
    private final ContentResponse cResp;
    private final String method;
    private final URL url;
    private final int code;
    private final HttpFields headers;

    protected Response(ContentResponse cResp, String method, URL url) {
        this.cResp = cResp;
        this.method = method;
        this.url = url;

        code = cResp.getStatus();
        headers = cResp.getHeaders();
    }


    public URL url() {
        return url;
    }

    public String method() {
        return method;
    }

    /**
     * @return 返回的头
     */
    public HttpFields headers() {
        return headers;
    }

    public String header(String name) {
        return headers.get(name);
    }

    public int statusCode() {
        return code;
    }

    public String statusMessage() {
        return cResp.getReason();
    }


    public HttpVersion httpVersion() {
        return cResp.getVersion();
    }
    public int httpVersionAsInt() {
        return cResp.getVersion().getVersion();
    }

    public String body() {
        return cResp.getContentAsString();
    }

    public byte[] bodyAsBytes() {
        return cResp.getContent();
    }

    @Override
    public String toString() {
        int code = statusCode();
        return "Response{" +
                "method=" + method + ' ' + code + ' ' + statusMessage() +
                ((code > 300 && code < 400)?(" Redirect:" + header("location")):"")+
                ", url=" + url +
                ", contentType=" + contentType() +
                '}';
    }

    public String charset() {
        return cResp.getEncoding();
    }

    public String contentType() {
        return cResp.getMediaType();
    }
}
