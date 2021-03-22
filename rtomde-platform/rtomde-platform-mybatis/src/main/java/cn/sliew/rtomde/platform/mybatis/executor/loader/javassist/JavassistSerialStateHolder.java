package cn.sliew.rtomde.platform.mybatis.executor.loader.javassist;

import cn.sliew.rtomde.platform.mybatis.executor.loader.AbstractSerialStateHolder;
import cn.sliew.rtomde.platform.mybatis.executor.loader.ResultLoaderMap;
import cn.sliew.rtomde.platform.mybatis.reflection.factory.ObjectFactory;

import java.util.List;
import java.util.Map;

class JavassistSerialStateHolder extends AbstractSerialStateHolder {

    private static final long serialVersionUID = 8940388717901644661L;

    public JavassistSerialStateHolder() {
    }

    public JavassistSerialStateHolder(
            final Object userBean,
            final Map<String, ResultLoaderMap.LoadPair> unloadedProperties,
            final ObjectFactory objectFactory,
            List<Class<?>> constructorArgTypes,
            List<Object> constructorArgs) {
        super(userBean, unloadedProperties, objectFactory, constructorArgTypes, constructorArgs);
    }

    @Override
    protected Object createDeserializationProxy(Object target, Map<String, ResultLoaderMap.LoadPair> unloadedProperties, ObjectFactory objectFactory,
                                                List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
        return new JavassistProxyFactory().createDeserializationProxy(target, unloadedProperties, objectFactory, constructorArgTypes, constructorArgs);
    }
}
