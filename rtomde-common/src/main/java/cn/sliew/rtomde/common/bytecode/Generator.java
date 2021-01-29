package cn.sliew.rtomde.common.bytecode;

import cn.sliew.rtomde.common.utils.ReflectUtils;
import javassist.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
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
    protected List<String> mImportedPackages;
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

    public Generator addImportedPackages(String... importedPackages) {
        if (mImportedPackages == null) {
            mImportedPackages = new ArrayList<>();
        }
        mImportedPackages.addAll(Arrays.asList(importedPackages));
        return this;
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

    /**
     * after {@link CtClass#writeFile()}, {@link CtClass#toBytecode()}, {@link CtClass#toClass()} invoked,
     * {@link CtClass} would be frozen. for more use the future, defrost it.
     *
     * fixme Memory Leak Risk
     * for reuse {@link ClassPool}, we cache all {@link ClassLoader} and {@link ClassPool}.
     * if gc cant release {@link ClassPool}, we must destroy {@link CtClass} by ourself.
     * but this will cause subsequent access {@link CtClass} by {@link Generator#mClassName} failed,
     * so we didnt detach {@link CtClass}.
     */
    @Override
    public void close() {
        if (mCtc != null) {
            mCtc.defrost();
//            mCtc.detach();
        }
        if (mFields != null) {
            mFields.clear();
        }
    }
}
