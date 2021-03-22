package cn.sliew.rtomde.platform.mybatis.executor.loader;

import cn.sliew.rtomde.platform.mybatis.reflection.factory.ObjectFactory;
import cn.sliew.rtomde.platform.mybatis.session.Configuration;

import java.util.List;
import java.util.Properties;

public interface ProxyFactory {

    default void setProperties(Properties properties) {
        // NOP
    }

    Object createProxy(Object target, ResultLoaderMap lazyLoader, Configuration configuration, ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs);

}
