package cn.sliew.rtomde.spi;

import cn.sliew.milky.common.log.Logger;
import cn.sliew.milky.common.log.LoggerFactory;
import cn.sliew.rtomde.common.lang.Prioritized;

import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkArgument;

public class DefaultPluginLoader<T> implements PluginLoader<T> {

    private static final Logger log = LoggerFactory.getLogger(DefaultPluginLoader.class);

    private static final ConcurrentMap<Class<?>, PluginLoader<?>> PLUGIN_LOADERS = new ConcurrentHashMap<>(64);
    private static final ConcurrentMap<Class<?>, Object> PLUGIN_INSTANCES = new ConcurrentHashMap<>(64);

    private final Class<?> type;

    private final ConcurrentMap<Class<?>, String> cachedNames = new ConcurrentHashMap<>();

    private static volatile LoadingStrategy[] strategies = loadLoadingStrategies();

    private DefaultPluginLoader(Class<?> type) {
        this.type = type;
    }


    @Override
    public List<LoadingStrategy> loadingStrategys() {
        return Arrays.asList(strategies);
    }

    /**
     * Load all {@link Prioritized prioritized} {@link LoadingStrategy Loading Strategies} via {@link ServiceLoader}
     *
     * @return non-null
     */
    private static LoadingStrategy[] loadLoadingStrategies() {
        return StreamSupport.stream(ServiceLoader.load(LoadingStrategy.class).spliterator(), false)
                .sorted()
                .toArray(LoadingStrategy[]::new);
    }

    public static <T> DefaultPluginLoader<T> getPluginLoader(Class<T> type) {
        checkArgument(type != null, "Extension type == null");
        checkArgument(!type.isInterface(), "Extension type (" + type + ") is not an interface!");

        DefaultPluginLoader<T> loader = (DefaultPluginLoader<T>) PLUGIN_LOADERS.get(type);
        if (loader == null) {
            loader = (DefaultPluginLoader<T>) PLUGIN_LOADERS.computeIfAbsent(type, (clazz) -> new DefaultPluginLoader<T>(clazz));
        }
        return loader;
    }


}