package cn.sliew.rtomde.executor.bytecode;

import cn.sliew.rtomde.common.utils.ReflectUtils;
import javassist.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

abstract class Generator implements AutoCloseable {
    /**
     * ClassLoader => ClassPool
     */
    static final Map<ClassLoader, ClassPool> POOL_MAP = new ConcurrentHashMap<>();
    protected ClassPool mPool;
    protected CtClass mCtc;
    protected String mClassName;
    protected String mSuperClass;
    protected Set<String> mFields;

    protected Generator(ClassPool pool) {
        mPool = pool;
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

    protected CtClass getCtClass(Class<?> c) throws NotFoundException {
        return mPool.get(c.getName());
    }

    protected CtMethod getCtMethod(Method m) throws NotFoundException {
        return getCtClass(m.getDeclaringClass())
                .getMethod(m.getName(), ReflectUtils.getDescWithoutMethodName(m));
    }

    protected CtConstructor getCtConstructor(Constructor<?> c) throws NotFoundException {
        return getCtClass(c.getDeclaringClass()).getConstructor(ReflectUtils.getDesc(c));
    }

    @Override
    public void close() {
        if (mCtc != null) {
            mCtc.detach();
        }
        if (mFields != null) {
            mFields.clear();
        }
    }
}
