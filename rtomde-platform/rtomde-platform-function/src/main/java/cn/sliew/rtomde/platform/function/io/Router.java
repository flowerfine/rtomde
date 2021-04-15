package cn.sliew.rtomde.platform.function.io;

import cn.sliew.rtomde.platform.function.Address;
import cn.sliew.rtomde.platform.function.FunctionType;

public interface Router<InT> {

    void route(InT message, Downstream<InT> downstream);

    interface Downstream<T> {

        void forward(Address to, T message);

        default void forward(FunctionType functionType, String id, T message) {
            forward(new Address(functionType, id), message);
        }
    }
}