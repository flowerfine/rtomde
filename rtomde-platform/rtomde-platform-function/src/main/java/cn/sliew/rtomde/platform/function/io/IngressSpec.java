package cn.sliew.rtomde.platform.function.io;

import cn.sliew.rtomde.platform.function.IngressType;

public interface IngressSpec<T> {

    IngressIdentifier<T> id();

    IngressType type();
}
