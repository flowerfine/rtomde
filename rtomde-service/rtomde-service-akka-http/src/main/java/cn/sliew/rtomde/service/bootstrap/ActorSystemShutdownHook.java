package cn.sliew.rtomde.service.bootstrap;

import akka.Done;
import akka.actor.typed.ActorSystem;
import scala.Function1;
import scala.util.Try;

public class ActorSystemShutdownHook implements Function1<Try<Done>, Object> {

    private final ActorSystem system;

    public ActorSystemShutdownHook(ActorSystem system) {
        this.system = system;
    }

    @Override
    public Object apply(Try<Done> doneTry) {
        if (doneTry.isFailure()) {
            system.log().info("ActorSystem terminate failure!", doneTry.failed().get());
        } else {
            system.log().info("ActorSystem terminate success!");
        }
        return doneTry.get();
    }

    @Override
    public <A> Function1<A, Object> compose(Function1<A, Try<Done>> g) {
        return null;
    }

    @Override
    public <A> Function1<Try<Done>, A> andThen(Function1<Object, A> g) {
        return null;
    }
}
