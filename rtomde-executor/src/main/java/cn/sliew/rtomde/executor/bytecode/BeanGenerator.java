package cn.sliew.rtomde.executor.bytecode;

import javassist.ClassPool;
import javassist.CtClass;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class BeanGenerator {

    /**
     * ClassLoader => ClassPool
     */
    private static final Map<ClassLoader, ClassPool> POOL_MAP = new ConcurrentHashMap<>();
    private ClassPool mPool;
    private List<String> mImportedPackages;
    private CtClass mCtc;
    private String mClassName;
    private String mSuperClass;
    private Set<String> mInterfaces;
    private List<String> mFields;
    private List<String> mConstructors;
    private List<String> mMethods;

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

    public BeanGenerator className(String className) {

    }


    public BeanGenerator getter(String property, Class<?> clazz) {

    }

    public BeanGenerator setter(String property, Class<?> clazz) {

    }



}
