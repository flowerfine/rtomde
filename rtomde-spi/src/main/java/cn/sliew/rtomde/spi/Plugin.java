package cn.sliew.rtomde.spi;

import java.util.Properties;

import static cn.sliew.milky.common.check.Ensures.checkNotNull;

public interface Plugin {

    /**
     * Helper method to get the class loader used to load the plugin. This may be needed for some plugins that use
     * dynamic class loading afterwards the plugin was loaded.
     *
     * @return the class loader used to load the plugin.
     */
    default ClassLoader getClassLoader() {
        return checkNotNull(this.getClass().getClassLoader(),
                () -> String.format("%s plugin with null class loader", this.getClass().getName()));
    }

    /**
     * Optional method for plugins to pick up settings from the configuration.
     *
     * @param prop The configuration to apply to the plugin.
     */
    default void configure(Properties prop) {
    }
}
