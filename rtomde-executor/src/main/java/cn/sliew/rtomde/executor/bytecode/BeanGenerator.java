package cn.sliew.rtomde.executor.bytecode;

import cn.sliew.rtomde.common.utils.ClassUtils;
import javassist.*;

import java.security.ProtectionDomain;
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
            CtClass ctcs = mSuperClass == null ? null : mPool.get(mSuperClass);
            if (mClassName == null) {
                mClassName = (mSuperClass == null || javassist.Modifier.isPublic(ctcs.getModifiers())
                        ? ClassGenerator.class.getName() : mSuperClass + "$sc") + id;
            }
            mCtc = mPool.makeClass(mClassName);
            if (mSuperClass != null) {
                mCtc.setSuperclass(ctcs);
            }
            if (mInterfaces != null) {
                for (String cl : mInterfaces) {
                    mCtc.addInterface(mPool.get(cl));
                }
            }
            if (mFields != null) {
                for (String code : mFields) {
                    mCtc.addField(CtField.make(code, mCtc));
                }
            }
            if (mMethods != null) {
                for (String code : mMethods) {
                    if (code.charAt(0) == ':') {
                        mCtc.addMethod(CtNewMethod.copy(getCtMethod(mCopyMethods.get(code.substring(1))),
                                code.substring(1, code.indexOf('(')), mCtc, null));
                    } else {
                        mCtc.addMethod(CtNewMethod.make(code, mCtc));
                    }
                }
            }
            if (mDefaultConstructor) {
                mCtc.addConstructor(CtNewConstructor.defaultConstructor(mCtc));
            }
            if (mConstructors != null) {
                for (String code : mConstructors) {
                    if (code.charAt(0) == ':') {
                        mCtc.addConstructor(CtNewConstructor
                                .copy(getCtConstructor(mCopyConstructors.get(code.substring(1))), mCtc, null));
                    } else {
                        String[] sn = mCtc.getSimpleName().split("\\$+"); // inner class name include $.
                        mCtc.addConstructor(
                                CtNewConstructor.make(code.replaceFirst(SIMPLE_NAME_TAG, sn[sn.length - 1]), mCtc));
                    }
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
