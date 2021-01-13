package cn.sliew.rtomde.service.akka.http.bootstrap;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletionStage;

@Slf4j
@Component
public class AkkaHttpBootstrap extends AllDirectives implements CommandLineRunner, ApplicationListener<ContextClosedEvent> {

    private CompletionStage<ServerBinding> serverBinding;
    private ActorSystem<Void> system;

    @Override
    public void run(String... args) throws Exception {
        // boot up server using the route as defined below
        system = ActorSystem.create(Behaviors.empty(), "routes");

        final Http http = Http.get(system);
        //In order to access all directives we need an instance where the routes are define.
        AkkaHttpBootstrap app = new AkkaHttpBootstrap();
        serverBinding = http.newServerAt("localhost", 8080).bind(app.createRoute());
        log.info("Server online at http://localhost:8080/");
    }

    private Route createRoute() {
        return concat(
                path("hello", () -> get(() -> complete("<h1>Say hello to akka-http</h1>"))));
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        log.info(contextClosedEvent.toString());
        serverBinding.thenCompose(ServerBinding::unbind) // trigger unbinding from the port
                .thenAccept(unbound -> system.terminate())// and shutdown when done
                .whenComplete((aVoid, throwable) -> {
                    if (throwable != null) {
                        log.error("Server shutdown failure!", throwable);
                    } else {
                        log.info("Server shutdown at http://localhost:8080/");
                    }
                });
    }
}