package cn.sliew.rtomde.datasource.type;

import java.util.Locale;
import java.util.Objects;

import static java.lang.String.format;

public final class TypeSignature {

    private final String name;

    /**
     * 保证hashcode不等于0
     */
    private int hashCode;

    public TypeSignature(String name) {
        checkArgument(name != null && !name.isEmpty(), "base is null or empty");
        checkArgument(validateName(name), "Bad characters in base type: %s", name);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    private static void checkArgument(boolean argument, String format, Object... args) {
        if (!argument) {
            throw new IllegalArgumentException(format(format, args));
        }
    }

    private static boolean validateName(String name) {
        return name.chars().noneMatch(c -> c == '<' || c == '>' || c == ',');
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TypeSignature other = (TypeSignature) o;

        return Objects.equals(this.name.toLowerCase(Locale.ENGLISH), other.name.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public int hashCode() {
        int hash = hashCode;
        if (hash == 0) {
            hash = Objects.hash(name.toLowerCase(Locale.ENGLISH));
            if (hash == 0) {
                hash = 1;
            }
            hashCode = hash;
        }

        return hash;
    }
}