package cn.sliew.rtomde.service.bootstrap;

import akka.actor.typed.ActorSystem;
import akka.http.javadsl.HttpTerminated;

import java.util.function.BiConsumer;

public class HttpServerShutdownHook implements BiConsumer<HttpTerminated, Throwable> {

    private final String host;
    private final ActorSystem system;

    public HttpServerShutdownHook(String host, ActorSystem system) {
        this.host = host;
        this.system = system;
    }

    @Override
    public void accept(HttpTerminated terminated, Throwable throwable) {
        if (throwable != null) {
            system.log().error("HttpServer terminate failure!", throwable);
        } else {
            system.log().info("HttpServer terminate at {}", host);
        }
    }
}
