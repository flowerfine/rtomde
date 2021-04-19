package cn.sliew.rtomde.platform.function.io;

import cn.sliew.rtomde.platform.function.EgressType;

public interface EgressSpec<T> {

    EgressIdentifier<T> id();

    EgressType type();
}
