package cn.rtomde.template.component.impl;

import cn.rtomde.template.component.DataSourceComponent;
import cn.sliew.milky.component.AbstractComponentRegistry;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.HashMap;
import java.util.Map;

import static cn.rtomde.template.component.impl.HikaricpDataSourceComponent.*;

public class DataSourceService extends AbstractComponentRegistry<DataSourceComponent, Config> {

    public static final String DEFAULT_NAME = "default";
    private final Config defaultConfig;

    public DataSourceService() {
        Map<String, Object> params = new HashMap<>();
        params.put(DRIVER_CLASS_NAME, "com.mysql.cj.jdbc.Driver");
        params.put(JDBC_URL, "jdbc:mysql://127.0.0.1:3306/data_service");
        params.put(USERNAME, "root");
        params.put(PASSWORD, "123");
        params.put(MINIMUM_IDLE, 1);
        params.put(MAXIMUM_POOL_SIZE, 2);
        params.put(IDLE_TIMEOUT, 30000);
        params.put(CONNECTION_TIMEOUT, 10000);
        params.put(MAX_LIFETIME, 900000);
        this.defaultConfig = ConfigFactory.parseMap(params);
        register(DEFAULT_NAME, defaultConfig);
    }

    @Override
    public Config getDefaultConfig() {
        return defaultConfig;
    }

    public DataSourceComponent register(String name, Config config) {
        if (exist(name)) {
            return find(name).get();
        }
        return computeIfAbsent(name, () -> new HikaricpDataSourceComponent(name, config));
    }
}
