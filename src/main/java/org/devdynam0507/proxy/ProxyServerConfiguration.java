package org.devdynam0507.proxy;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.logging.LoggingService;

@Configuration
public class ProxyServerConfiguration {

    @Bean
    Server proxyServer(
        List<ErrorMiddleware> middlewares,
        ProxyService proxyService
    ) {
        ServerBuilder sb = Server.builder();

        return sb.http(8888)
            .service("glob:/**", proxyService)
            .decorator(LoggingService.newDecorator())
            .errorHandler((ctx, cause) -> {
                middlewares.forEach(handler -> handler.catchServerError(cause));

                return HttpResponse.of(500);
            })
            .build();
    }

    @Bean
    ErrorMiddleware sentrySender() {
        return new ProxyErrorSentrySender();
    }

    @Bean
    ProxyService proxyService() {
        return new ProxyService();
    }
}
