package cn.sliew.rtomde.platform.jdbc.reflection;

public interface ClassMetadata {

    /**
     * Return the name of the underlying class.
     */
    String getClassName();

    /**
     * Return whether the underlying class represents an interface.
     */
    boolean isInterface();

    /**
     * Return whether the underlying class represents an annotation.
     */
    boolean isAnnotation();

    /**
     * Return whether the underlying class is marked as abstract.
     */
    boolean isAbstract();

    /**
     * Return whether the underlying class represents a concrete class,
     * i.e. neither an interface nor an abstract class.
     */
    default boolean isConcrete() {
        return !(isInterface() || isAbstract());
    }

    /**
     * Return whether the underlying class is marked as 'final'.
     */
    boolean isFinal();

    /**
     * Return whether the underlying class is declared within an enclosing
     * class (i.e. the underlying class is an inner/nested class or a
     * local class within a method).
     * <p>If this method returns {@code false}, then the underlying
     * class is a top-level class.
     */
    default boolean hasEnclosingClass() {
        return (getEnclosingClassName() != null);
    }

    /**
     * Return the name of the enclosing class of the underlying class,
     * or {@code null} if the underlying class is a top-level class.
     */
    String getEnclosingClassName();

    /**
     * Return whether the underlying class has a super class.
     */
    default boolean hasSuperClass() {
        return (getSuperClassName() != null);
    }

    /**
     * Return the name of the super class of the underlying class,
     * or {@code null} if there is no super class defined.
     */
    String getSuperClassName();

    /**
     * Return the names of all interfaces that the underlying class
     * implements, or an empty array if there are none.
     */
    String[] getInterfaceNames();

}
