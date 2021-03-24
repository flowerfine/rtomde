package org.apache.ibatis.session;

import org.apache.ibatis.type.TypeException;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ConfigurationRegistry {

    private static final ConcurrentMap<String, Configuration> configurations = new ConcurrentHashMap<>(4);

    public static void registerConfiguration(String application, Configuration configuration) {
        if (application == null) {
            throw new SqlSessionException("The application cannot be null");
        }
        String key = application.toLowerCase(Locale.ENGLISH);
        if (configurations.containsKey(key) && configurations.get(key) != null && !configurations.get(key).equals(configuration)) {
            throw new TypeException("The application '" + application + "' is already mapped to the value '" + configurations.get(key) + "'.");
        }
        configurations.put(key, configuration);
    }

    public static Configuration getConfiguration(String application) {
        return configurations.get(application);
    }

    /**
     * Gets the configurations.
     *
     * @return the configurations
     */
    public Map<String, Configuration> getConfigurations() {
        return Collections.unmodifiableMap(configurations);
    }

}
