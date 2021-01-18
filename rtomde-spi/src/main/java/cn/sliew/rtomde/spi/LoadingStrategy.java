package cn.sliew.rtomde.spi;

import cn.sliew.rtomde.common.lang.Prioritized;

public interface LoadingStrategy extends Prioritized {

    String directory();

    default String[] excludedPackages() {
        return null;
    }

    /**
     * Indicates current {@link LoadingStrategy} supports overriding other lower prioritized instances or not.
     *
     * @return if supports, return <code>true</code>, or <code>false</code>
     */
    default boolean overridden() {
        return false;
    }
}
