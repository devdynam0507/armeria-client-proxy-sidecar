package org.devdynam0507.proxy;

@FunctionalInterface
public interface ErrorMiddleware {

    void catchServerError(Throwable cause);
}
