package cn.sliew.rtomde.service.bootstrap;

import akka.actor.typed.ActorSystem;
import akka.http.javadsl.ServerBinding;

import java.time.Duration;
import java.util.function.BiConsumer;

public class HttpServerBootstrapHook implements BiConsumer<ServerBinding, Throwable> {

    private final String host;
    private final ActorSystem system;

    public HttpServerBootstrapHook(String host, ActorSystem system) {
        this.host = host;
        this.system = system;
    }

    @Override
    public void accept(ServerBinding binding, Throwable throwable) {
        if (throwable != null) {
            system.log().error("HttpServer bootstrap failure!", throwable);
        } else {
            system.log().info("HttpServer bootstrap at {}", host);
        }
        binding.addToCoordinatedShutdown(Duration.ofMinutes(1L), system);
        binding.whenTerminated().whenComplete(new HttpServerShutdownHook(host, system));
    }
}
