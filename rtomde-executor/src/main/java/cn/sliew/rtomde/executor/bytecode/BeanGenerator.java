package cn.sliew.rtomde.executor.bytecode;

import cn.sliew.rtomde.common.utils.ClassUtils;
import javassist.*;

import java.security.ProtectionDomain;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class BeanGenerator {

    private static final AtomicLong CLASS_NAME_COUNTER = new AtomicLong(0);

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
        return this;
    }


    public BeanGenerator getter(String property, Class<?> clazz) {
        return this;
    }

    public BeanGenerator setter(String property, Class<?> clazz) {
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
        long id = CLASS_NAME_COUNTER.getAndIncrement();
        try {
            mCtc = mPool.makeClass(className);
            if (superClass != null) {
                mCtc.setSuperclass(mPool.get(superClass));
            }
            if (getters != null) {
                for (PropertyDescriptor getter : getters) {
                    mCtc.addInterface(mPool.get(cl));
                }
            }
            if (setters != null) {
                for (PropertyDescriptor setter : setters) {
                    mCtc.addField(CtField.make(code, mCtc));
                }
            }
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
