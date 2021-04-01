package cn.sliew.rtomde.spi;

import cn.sliew.milky.test.MilkyTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PluginLoaderTest extends MilkyTestCase {

    private PluginLoader pluginLoader;

    @BeforeEach
    private void beforeEach() {
        pluginLoader = DefaultPluginLoader.getPluginLoader(LoadingStrategy.class);
    }

    @Disabled
    @Test
    public void testLoadStragegy() {
        List<LoadingStrategy> loadingStrategies = pluginLoader.loadingStrategys();
        assertTrue(loadingStrategies.size() > 0);
    }
}
