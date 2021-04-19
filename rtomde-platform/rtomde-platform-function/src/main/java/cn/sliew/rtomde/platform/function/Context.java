package cn.sliew.rtomde.platform.function;

import cn.sliew.rtomde.platform.function.io.EgressIdentifier;

public interface Context {

    Address self();

    Address caller();

    void send(Address to, Object message);

    <T> void send(EgressIdentifier<T> egress, T message);

    default void send(FunctionType functionType, String id, Object message) {
        send(new Address(functionType, id), message);
    }

    default void reply(Object message) {
        send(caller(), message);
    }
}
