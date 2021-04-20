package cn.sliew.rtomde.platform.jdbc.reflection;

import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.stream.Stream;

import static cn.sliew.milky.common.check.Ensures.checkNotNull;

public class StandardClassMetadata implements ClassMetadata {

    private final Class<?> type;

    public StandardClassMetadata(Class<?> type) {
        checkNotNull(type);
        this.type = type;
    }

    @Override
    public String getClassName() {
        return this.type.getName();
    }

    @Override
    public boolean isInterface() {
        return this.type.isInterface();
    }

    @Override
    public boolean isAnnotation() {
        return this.type.isAnnotation();
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(this.type.getModifiers());
    }

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(this.type.getModifiers());
    }

    @Override
    public String getEnclosingClassName() {
        return Optional.ofNullable(this.type.getEnclosingClass())
                .map(Class::getName)
                .orElse(null);
    }

    @Override
    public String getSuperClassName() {
        return Optional.ofNullable(this.type.getSuperclass())
                .map(Class::getName)
                .orElse(null);
    }

    @Override
    public String[] getInterfaceNames() {
        return Stream.of(this.type.getInterfaces())
                .map(Class::getName)
                .toArray(size -> new String[size]);
    }

}
