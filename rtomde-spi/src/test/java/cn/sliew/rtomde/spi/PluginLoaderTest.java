package cn.sliew.rtomde.spi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PluginLoaderTest {

    private PluginLoader pluginLoader;

    @BeforeEach
    private void beforeEach() {
        pluginLoader = new DefaultPluginLoader();
    }

    @Test
    public void testLoadStragegy() {
        List<LoadingStrategy> loadingStrategies = pluginLoader.loadingStrategys();
        assertTrue(loadingStrategies.size() > 0);
    }
}
