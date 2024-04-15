package org.devdynam0507.proxy;

public class ProxyErrorSentrySender implements ErrorMiddleware {

    @Override
    public void catchServerError(Throwable cause) {
        // TODO: send to sentry
    }
}
