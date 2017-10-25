package com.yixun.sdk.idhttp;

import java.util.Map;

/**
 * Created by yaolei on 17-3-23.
 */

abstract class IdRequest {
    private String url;
    private boolean useCache;
    private boolean redirect = true;
    private String method;
    private Map<String, String> urlParams;
    private Map<String, String> headers;
    private IdProgressListener listener;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isUseCache() {
        return useCache;
    }

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    public boolean isRedirect() {
        return redirect;
    }

    public void setRedirect(boolean redirect) {
        this.redirect = redirect;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getUrlParams() {
        return urlParams;
    }

    public void setUrlParams(Map<String, String> urlParams) {
        this.urlParams = urlParams;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public IdProgressListener getListener() {
        return listener;
    }

    public void setListener(IdProgressListener listener) {
        this.listener = listener;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("url:").append(url).append(";");
        builder.append("method:").append(method).append(";");
        builder.append("useCache:").append(useCache).append(";");
        builder.append("isRedirect:").append(redirect).append(";");
        builder.append("urlParams size:").append(urlParams != null ? urlParams.size() : "null").append(";");
        builder.append("headers size:").append(headers != null ? headers.size() : "null").append(";");
        builder.append("have listener:").append(listener != null).append(".");
        return builder.toString();
    }
}
