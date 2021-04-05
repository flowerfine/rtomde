package cn.sliew.rtomde.service.bootstrap;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.http.javadsl.Http;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class HttpServerBootstrap implements ApplicationRunner {

    @Value("${spring.application.name:akka-http-server}")
    private String application;

    private ActorSystem<Void> system;

    @Autowired
    private AkkaHttpDynamicRoute route;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // boot up server using the route as defined below
        system = ActorSystem.create(Behaviors.empty(), application);
        system.whenTerminated().onComplete(new ActorSystemShutdownHook(system), system.executionContext());

        final Http http = Http.get(system);
        //In order to access all directives we need an instance where the routes are define.
        http.newServerAt("localhost", 8080).bind(route.dynamicRoute())
                .whenComplete(new HttpServerBootstrapHook("http://localhost:8080/", system));
    }

}
