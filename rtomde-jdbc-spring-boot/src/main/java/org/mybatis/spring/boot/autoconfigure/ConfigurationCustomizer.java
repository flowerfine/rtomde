package org.mybatis.spring.boot.autoconfigure;

import cn.sliew.rtomde.platform.mybatis.session.Configuration;

/**
 * Callback interface that can be customized a {@link Configuration} object generated on auto-configuration.
 */
@FunctionalInterface
public interface ConfigurationCustomizer {

    /**
     * Customize the given a {@link Configuration} object.
     *
     * @param configuration the configuration object to customize
     */
    void customize(Configuration configuration);

}
