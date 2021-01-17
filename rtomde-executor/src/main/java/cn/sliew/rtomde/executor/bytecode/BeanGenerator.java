package cn.sliew.rtomde.executor.bytecode;

import cn.sliew.rtomde.common.utils.ClassUtils;
import javassist.*;

import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BeanGenerator {

    /**
     * ClassLoader => ClassPool
     */
    private static final Map<ClassLoader, ClassPool> POOL_MAP = new ConcurrentHashMap<>();
    private ClassPool mPool;
    private CtClass mCtc;
    private String className;
    private String superClass;
    private List<PropertyDescriptor> getters;
    private List<PropertyDescriptor> setters;

    private BeanGenerator() {
        throw new IllegalStateException("can't do this!");
    }

    private BeanGenerator(ClassPool pool) {
        mPool = pool;
    }

    public static BeanGenerator newInstance() {
        return new BeanGenerator(getClassPool(Thread.currentThread().getContextClassLoader()));
    }

    public static BeanGenerator newInstance(ClassLoader loader) {
        return new BeanGenerator(getClassPool(loader));
    }

    public static ClassPool getClassPool(ClassLoader loader) {
        if (loader == null) {
            return ClassPool.getDefault();
        }

        ClassPool pool = POOL_MAP.get(loader);
        if (pool == null) {
            pool = new ClassPool(true);
            pool.appendClassPath(new CustomizedLoaderClassPath(loader));
            POOL_MAP.put(loader, pool);
        }
        return pool;
    }

    public BeanGenerator superClass(String cn) {
        superClass = cn;
        return this;
    }

    public BeanGenerator superClass(Class<?> cl) {
        superClass = cl.getName();
        return this;
    }

    public BeanGenerator className(String className) {
        className = className;
        return this;
    }

    public BeanGenerator setgetter(String property, Class<?> clazz) {
        if (getters == null) {
            getters = new ArrayList<>();
        }
        if (setters == null) {
            setters = new ArrayList<>();
        }
        PropertyDescriptor propertyDescriptor = new PropertyDescriptor(property, clazz);
        getters.add(propertyDescriptor);
        setters.add(propertyDescriptor);
        return this;
    }


    public BeanGenerator getter(String property, Class<?> clazz) {
        if (getters == null) {
            getters = new ArrayList<>();
        }
        getters.add(new PropertyDescriptor(property, clazz));
        return this;
    }

    public BeanGenerator setter(String property, Class<?> clazz) {
        if (setters == null) {
            setters = new ArrayList<>();
        }
        setters.add(new PropertyDescriptor(property, clazz));
        return this;
    }

    public Class<?> toClass() {
        return toClass(ClassUtils.getClassLoader(ClassGenerator.class),
                getClass().getProtectionDomain());
    }

    public Class<?> toClass(ClassLoader loader, ProtectionDomain pd) {
        if (mCtc != null) {
            mCtc.detach();
        }
        try {
            mCtc = mPool.makeClass(className);
            if (superClass != null) {
                mCtc.setSuperclass(mPool.get(superClass));
            }
            if (getters != null) {
                for (PropertyDescriptor getter : getters) {
                    CtNewMethod.getter(getter.getProperty(),
                            CtField.make("private " + getter.getJavaType().getName() + getter.getProperty(), mCtc));
                }
            }
            if (setters != null) {
                for (PropertyDescriptor setter : setters) {
                    CtNewMethod.setter(setter.getProperty(),
                            CtField.make("private " + setter.getJavaType().getName() + setter.getProperty(), mCtc));
                }
            }
            mCtc.writeFile();
            return mCtc.toClass(loader, pd);
        } catch (RuntimeException e) {
            throw e;
        } catch (NotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (CannotCompileException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
