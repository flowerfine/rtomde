package cn.sliew.rtomde.platform.jdbc.reflection;

import java.lang.reflect.Method;

public class StandardMethodMetadata implements MethodMetadata {

    private final Method method;

    public StandardMethodMetadata(Method method) {
        this.method = method;
    }

    @Override
    public String getMethodName() {
        return null;
    }

    @Override
    public String getDeclaringClassName() {
        return null;
    }

    @Override
    public String getReturnTypeName() {
        return null;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public boolean isFinal() {
        return false;
    }

    @Override
    public boolean isOverridable() {
        return false;
    }
}
