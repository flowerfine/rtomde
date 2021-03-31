package cn.sliew.rtomde.service.bytecode.autoconfigure;

import cn.sliew.rtomde.platform.mybatis.config.MybatisPlatformOptions;

/**
 * Callback interface that can be customized a {@link MybatisPlatformOptions} object generated on auto-configuration.
 */
@FunctionalInterface
public interface MybatisPlatformCustomizer {

    /**
     * Customize the given a {@link MybatisPlatformOptions} object.
     *
     * @param platform the Mybatis Platform metadata object to customize
     */
    void customize(MybatisPlatformOptions platform);

}
