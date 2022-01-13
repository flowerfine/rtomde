package cn.rtomde.template.component.impl;

import cn.rtomde.template.component.DataSourceComponent;
import cn.sliew.milky.component.AbstractComponentRegistry;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.HashMap;
import java.util.Map;

public class DataSourceService extends AbstractComponentRegistry<DataSourceComponent, Config> {

    private final Config defaultConfig;

    public DataSourceService() {
        Map<String, String> params = new HashMap<>();
        params.put("")
        this.defaultConfig = ConfigFactory.defaultApplication();
        defaultConfig.
    }

    @Override
    public Config getDefaultConfig() {
        return defaultConfig;
    }
}
