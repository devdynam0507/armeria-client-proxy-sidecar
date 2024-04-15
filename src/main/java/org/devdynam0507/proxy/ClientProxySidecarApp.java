package org.devdynam0507.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.linecorp.armeria.server.Server;

public class ClientProxySidecarApp {

    static Logger logger = LoggerFactory.getLogger(ClientProxySidecarApp.class);

    static void runServer(Server proxyServer) {
        proxyServer.closeOnJvmShutdown();
        proxyServer.start().join();

        logger.info("Server has been started. Serving dummy service at http://127.0.0.1:{}",
                    proxyServer.activeLocalPort());
    }

    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext =
            new AnnotationConfigApplicationContext(ProxyServerConfiguration.class);

        runServer(applicationContext.getBean(Server.class));
    }
}
