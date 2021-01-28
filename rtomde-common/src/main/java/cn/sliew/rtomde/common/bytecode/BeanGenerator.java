package cn.sliew.rtomde.common.bytecode;

import cn.sliew.rtomde.common.utils.ClassUtils;
import javassist.*;

import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public final class BeanGenerator extends Generator implements AutoCloseable {

    private List<PropertyDescriptor> getters;
    private List<PropertyDescriptor> setters;

    private BeanGenerator(ClassPool pool) {
        super(pool);
    }

    public static BeanGenerator newInstance() {
        return new BeanGenerator(getClassPool(Thread.currentThread().getContextClassLoader()));
    }

    public static BeanGenerator newInstance(ClassLoader loader) {
        return new BeanGenerator(getClassPool(loader));
    }

    public BeanGenerator superClass(String cn) {
        this.mSuperClass = cn;
        return this;
    }

    public BeanGenerator superClass(Class<?> cl) {
        this.mSuperClass = cl.getName();
        return this;
    }

    public BeanGenerator className(String className) {
        this.mClassName = className;
        return this;
    }

    public BeanGenerator setgetter(String property, Class<?> clazz) {
        if (getters == null) {
            getters = new ArrayList<>();
        }
        if (setters == null) {
            setters = new ArrayList<>();
        }
        PropertyDescriptor propertyDescriptor = formatProperty(property, clazz);
        getters.add(propertyDescriptor);
        setters.add(propertyDescriptor);
        return this;
    }


    public BeanGenerator getter(String property, Class<?> clazz) {
        if (getters == null) {
            getters = new ArrayList<>();
        }
        getters.add(formatProperty(property, clazz));
        return this;
    }

    public BeanGenerator setter(String property, Class<?> clazz) {
        if (setters == null) {
            setters = new ArrayList<>();
        }
        setters.add(formatProperty(property, clazz));
        return this;
    }

    private PropertyDescriptor formatProperty(String property, Class<?> clazz) {
        if ((clazz == Boolean.class || clazz == Boolean.TYPE) && property.startsWith("is")) {
            return new PropertyDescriptor(property.substring(2, 3).toLowerCase() + property.substring(3), clazz);
        }
        return new PropertyDescriptor(property, clazz);
    }

    public Class<?> toClass() {
        return toClass(ClassUtils.getClassLoader(BeanGenerator.class),
                getClass().getProtectionDomain());
    }

    /**
     * fixme 后面把get()换成makeClass
     *
     * @param loader
     * @param pd
     * @return
     */
    public Class<?> toClass(ClassLoader loader, ProtectionDomain pd) {
        if (mCtc != null) {
            mCtc.detach();
        }
        try {
            mCtc = mPool.makeClass(mClassName);
            if (mImportedPackages != null) {
                mImportedPackages.forEach(importedPackage -> mPool.importPackage(importedPackage));
            }
            if (mSuperClass != null) {
                mCtc.setSuperclass(mPool.get(mSuperClass));
            }
            if (getters != null) {
                for (PropertyDescriptor getter : getters) {
                    CtMethod ctMethod = CtNewMethod.getter(getterMethod(getter), getField(getter));
                    mCtc.addMethod(ctMethod);
                }
            }
            if (setters != null) {
                for (PropertyDescriptor setter : setters) {
                    CtMethod ctMethod = CtNewMethod.setter(setterMethod(setter), getField(setter));
                    mCtc.addMethod(ctMethod);
                }
            }
            if (mFields != null) {
                for (String code : mFields) {
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
        }
    }

    private String getterMethod(PropertyDescriptor getter) {
        String property = getter.getProperty().substring(0, 1).toUpperCase() + getter.getProperty().substring(1);
        if (getter.getJavaType() == Boolean.class || getter.getJavaType() == Boolean.TYPE) {
            return String.format("is%s", property);
        }
        return String.format("get%s", property);
    }

    private String setterMethod(PropertyDescriptor setter) {
        String property = setter.getProperty().substring(0, 1).toUpperCase() + setter.getProperty().substring(1);
        return String.format("set%s", property);
    }

    @Override
    public void close() {
        super.close();
        if (getters != null) {
            getters.clear();
        }
        if (setters != null) {
            setters.clear();
        }
    }

    private CtField getField(PropertyDescriptor field) {
        String fieldStr = String.format("private %s %s;", field.getJavaType().getName(), field.getProperty());
        CtField ctField = null;
        try {
            ctField = mCtc.getField(fieldStr);
        } catch (NotFoundException e) {
            try {
                ctField = CtField.make(fieldStr, mCtc);
                if (this.mFields == null) {

                    this.mFields = new HashSet<>();
                }
                this.mFields.add(fieldStr);
            } catch (CannotCompileException cannotCompileException) {
                throw new RuntimeException(cannotCompileException.getMessage(), cannotCompileException);
            }
        }
        return ctField;
    }
}
