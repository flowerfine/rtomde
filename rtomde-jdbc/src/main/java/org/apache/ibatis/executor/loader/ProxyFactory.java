package org.apache.ibatis.executor.loader;

import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.Configuration;

import java.util.List;
import java.util.Properties;

public interface ProxyFactory {

    default void setProperties(Properties properties) {
        // NOP
    }

    Object createProxy(Object target, ResultLoaderMap lazyLoader, Configuration configuration, ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs);

}
