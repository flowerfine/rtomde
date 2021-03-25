package cn.sliew.rtomde.platform.mybatis.executor.loader;

import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyCopier;
import org.apache.ibatis.reflection.property.PropertyNamer;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class AbstractEnhancedDeserializationProxy {

    protected static final String FINALIZE_METHOD = "finalize";
    protected static final String WRITE_REPLACE_METHOD = "writeReplace";
    private final Class<?> type;
    private final Map<String, ResultLoaderMap.LoadPair> unloadedProperties;
    private final ObjectFactory objectFactory;
    private final List<Class<?>> constructorArgTypes;
    private final List<Object> constructorArgs;
    private final Object reloadingPropertyLock;
    private boolean reloadingProperty;

    protected AbstractEnhancedDeserializationProxy(Class<?> type, Map<String, ResultLoaderMap.LoadPair> unloadedProperties,
                                                   ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
        this.type = type;
        this.unloadedProperties = unloadedProperties;
        this.objectFactory = objectFactory;
        this.constructorArgTypes = constructorArgTypes;
        this.constructorArgs = constructorArgs;
        this.reloadingPropertyLock = new Object();
        this.reloadingProperty = false;
    }

    public final Object invoke(Object enhanced, Method method, Object[] args) throws Throwable {
        final String methodName = method.getName();
        try {
            if (WRITE_REPLACE_METHOD.equals(methodName)) {
                final Object original;
                if (constructorArgTypes.isEmpty()) {
                    original = objectFactory.create(type);
                } else {
                    original = objectFactory.create(type, constructorArgTypes, constructorArgs);
                }

                PropertyCopier.copyBeanProperties(type, enhanced, original);
                return this.newSerialStateHolder(original, unloadedProperties, objectFactory, constructorArgTypes, constructorArgs);
            } else {
                synchronized (this.reloadingPropertyLock) {
                    if (!FINALIZE_METHOD.equals(methodName) && PropertyNamer.isProperty(methodName) && !reloadingProperty) {
                        final String property = PropertyNamer.methodToProperty(methodName);
                        final String propertyKey = property.toUpperCase(Locale.ENGLISH);
                        if (unloadedProperties.containsKey(propertyKey)) {
                            final ResultLoaderMap.LoadPair loadPair = unloadedProperties.remove(propertyKey);
                            if (loadPair != null) {
                                try {
                                    reloadingProperty = true;
                                    loadPair.load(enhanced);
                                } finally {
                                    reloadingProperty = false;
                                }
                            } else {
                                /* I'm not sure if this case can really happen or is just in tests -
                                 * we have an unread property but no loadPair to load it. */
                                throw new ExecutorException("An attempt has been made to read a not loaded lazy property '"
                                        + property + "' of a disconnected object");
                            }
                        }
                    }

                    return enhanced;
                }
            }
        } catch (Throwable t) {
            throw ExceptionUtil.unwrapThrowable(t);
        }
    }

    protected abstract AbstractSerialStateHolder newSerialStateHolder(
            Object userBean,
            Map<String, ResultLoaderMap.LoadPair> unloadedProperties,
            ObjectFactory objectFactory,
            List<Class<?>> constructorArgTypes,
            List<Object> constructorArgs);

}
