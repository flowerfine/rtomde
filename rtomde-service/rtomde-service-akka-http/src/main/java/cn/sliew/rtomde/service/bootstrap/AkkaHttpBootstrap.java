package cn.sliew.rtomde.service.bootstrap;

import akka.Done;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static akka.http.javadsl.server.PathMatchers.longSegment;

@Slf4j
@Component
public class AkkaHttpBootstrap extends AllDirectives implements CommandLineRunner, ApplicationListener<ContextClosedEvent> {

    @Value("${spring.application.name:akka-http-server}")
    private String application;

    private CompletionStage<ServerBinding> serverBinding;
    private ActorSystem<Void> system;

    @Autowired
    private AkkaHttpRouter router;

    @Override
    public void run(String... args) throws Exception {
        // boot up server using the route as defined below
        system = ActorSystem.create(Behaviors.empty(), application);

        final Http http = Http.get(system);
        //In order to access all directives we need an instance where the routes are define.
        AkkaHttpBootstrap app = new AkkaHttpBootstrap();
        serverBinding = http.newServerAt("localhost", 8080).bind(app.createRoute());
        log.info("Server online at http://localhost:8080/");
    }

    private Route createRoute() {
        return concat(
                get(() -> pathPrefix("item", () ->
                        path(longSegment(), (Long id) -> {
                            final CompletionStage<Optional<Item>> futureMaybeItem = fetchItem(id);
                            return onSuccess(futureMaybeItem, maybeItem ->
                                    maybeItem.map(item -> completeOK(item, Jackson.marshaller()))
                                            .orElseGet(() -> complete(StatusCodes.NOT_FOUND, "Not Found"))
                            );
                        }))),
                post(() -> path("create-order", () ->
                        entity(Jackson.unmarshaller(Order.class), order -> {
                            CompletionStage<Done> futureSaved = saveOrder(order);
                            return onSuccess(futureSaved, done ->
                                    complete("order created")
                            );
                        }))),
                get(() -> path("hello", () -> complete("<h1>Say hello to akka-http</h1>")))
        );
    }

    // (fake) async database query api
    private CompletionStage<Optional<Item>> fetchItem(long itemId) {
        return CompletableFuture.completedFuture(Optional.of(new Item("foo", itemId)));
    }

    // (fake) async database query api
    private CompletionStage<Done> saveOrder(final Order order) {
        return CompletableFuture.completedFuture(Done.getInstance());
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

    private static class Item {
        final String name;
        final long id;

        @JsonCreator
        Item(@JsonProperty("name") String name,
             @JsonProperty("id") long id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public long getId() {
            return id;
        }
    }

    private static class Order {
        final List<Item> items;

        @JsonCreator
        Order(@JsonProperty("items") List<Item> items) {
            this.items = items;
        }

        public List<Item> getItems() {
            return items;
        }
    }
}