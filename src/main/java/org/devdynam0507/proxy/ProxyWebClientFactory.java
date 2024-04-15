package org.devdynam0507.proxy;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.armeria.client.ClientFactory;
import com.linecorp.armeria.client.WebClient;
import com.linecorp.armeria.client.logging.LoggingClient;

public class ProxyWebClientFactory {

    private final static ClientFactory clientFactory = ClientFactory.builder()
        .preferHttp1(true)
        .useHttp1Pipelining(true)
        .build();

    private final static Logger logger = LoggerFactory.getLogger(ProxyWebClientFactory.class);

    private final Map<String, WebClient> webClientCache = new HashMap<>();

    public WebClient of(String url) {
        if (webClientCache.containsKey(url)) {
            logger.info("Returning cached WebClient for url: {}", url);
            return webClientCache.get(url);
        }
        WebClient webClient = WebClient.builder(url)
            .decorator(LoggingClient.newDecorator())
            .factory(clientFactory)
            .build();
        webClientCache.put(url, webClient);

        return webClient;
    }

    public WebClient of(String url, CustomHeaders headers) {

        return WebClient.builder(url)
            .decorator(LoggingClient.newDecorator())
            .factory(clientFactory)
            .addHeaders(headers)
            .build();
    }
}
