package org.devdynam0507.proxy;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.common.QueryParams;
import com.linecorp.armeria.common.RequestHeaders;
import com.linecorp.armeria.server.AbstractHttpService;
import com.linecorp.armeria.server.ServiceRequestContext;

public class ProxyService extends AbstractHttpService {

    private static final Logger logger = LoggerFactory.getLogger(ProxyService.class);
    private static final String PROXY_HOST_HEADER = "X-Proxy-Host";
    private static final String RESPONSE_PROXY_HEADER = "X-Proxy-Provider";
    private static final String RESPONSE_PROXY_HEADER_VALUE = "Engagemoment Armeria Proxy";
    private static final String PROXY_HEADER_KEYS = "X-Custom-Header-Keys";

    private final ProxyWebClientFactory proxyWebClientFactory;

    public ProxyService() {
        this(new ProxyWebClientFactory());
    }

    public ProxyService(ProxyWebClientFactory proxyWebClientFactory) {
        this.proxyWebClientFactory = proxyWebClientFactory;
    }

    private static HttpRequest createProxyRequest(HttpRequest req) {
        RequestHeaders requestHeaders = req.headers().toBuilder()
            .removeAndThen(PROXY_HOST_HEADER)
            .build();

        return HttpRequest.of(HttpMethod.GET, req.path())
            .withHeaders(requestHeaders);
    }

    private static CustomHeaders peekRequiredProxyHeaders(HttpRequest req) {
        RequestHeaders headers = req.headers();
        String headerNameString = headers.get(PROXY_HEADER_KEYS);
        CustomHeaders customHeaders = CustomHeaders.of();

        if (headerNameString != null) {
            List<String> headerNames = Arrays.asList(headerNameString.split(","));
            headerNames.forEach(headerName -> {
                String headerValue = headers.get(headerName);
                if (headerValue != null) {
                    customHeaders.addHeader(headerName, headerValue);
                }
            });
        }

        return customHeaders;
    }

    private static String getProxyTargetHost(HttpRequest req) {
        return req.headers().get(PROXY_HOST_HEADER);
    }
    
    private HttpResponse requestWithCacheableClient(String proxyUrl, String path, QueryParams queryParams) {
        return proxyWebClientFactory.of(proxyUrl)
            .get(path, queryParams);
    }
    
    private HttpResponse request(String proxyUrl, String path, QueryParams queryParams, CustomHeaders customHeaders) {
        return proxyWebClientFactory.of(proxyUrl, customHeaders)
            .get(path, queryParams);
    }

    private HttpResponse request(String proxyUrl, HttpRequest req) {
        CustomHeaders customHeaders = peekRequiredProxyHeaders(req);
        String path = req.path();
        QueryParams queryParams = null;
        if (path.contains("?")) {
            queryParams = QueryParams.fromQueryString(path.substring(path.indexOf("?") + 1));
            path = path.substring(0, path.indexOf("?"));
        }
        HttpResponse res;
        if (customHeaders.size() == 0) {
            res = requestWithCacheableClient(proxyUrl, path, queryParams == null ? QueryParams.of() : queryParams);
        }
        else {
            res = request(proxyUrl, path, queryParams, customHeaders);
        }
        
        return res.recover(throwable -> {
            logger.error("Failed to proxy request", throwable);
            return HttpResponse.of(400);
        });
    }

    HttpResponse checkPingRequest(HttpRequest req) {
        if (req.path().equals("/ping")) {
            return HttpResponse.of(HttpStatus.OK, MediaType.PLAIN_TEXT_UTF_8, "pong");
        }

        return null;
    }

    @Override
    protected HttpResponse doGet(ServiceRequestContext ctx, HttpRequest req) throws Exception {
        HttpResponse pongOrNull = checkPingRequest(req);
        if (pongOrNull != null) {
            return pongOrNull;
        }

        String proxyUrl = getProxyTargetHost(req);
        if (proxyUrl == null) {
            return HttpResponse.of(
                HttpStatus.valueOf(400),
                MediaType.PLAIN_TEXT_UTF_8,
                "Missing required header: " + PROXY_HOST_HEADER
            );
        }

        req = createProxyRequest(req);
        ctx.updateRequest(req);
        logger.info("request: {}", req);

        HttpResponse res = request(proxyUrl, req);

        return res.mapHeaders(headers -> headers.toBuilder()
            .add(RESPONSE_PROXY_HEADER, RESPONSE_PROXY_HEADER_VALUE)
            .build()
        );
    }
}
