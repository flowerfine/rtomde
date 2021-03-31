package cn.sliew.rtomde.platform.mybatis.datasource.hikaricp;

import cn.sliew.rtomde.platform.mybatis.config.DatasourceOptions;
import cn.sliew.rtomde.platform.mybatis.datasource.DataSourceFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class HikaricpDataSourceFactory implements DataSourceFactory {

    private Config config;

    private final ConcurrentMap<DatasourceOptions, DataSource> dataSourceRegistry = new ConcurrentHashMap<>(2);

    public HikaricpDataSourceFactory() {
        this.config = ConfigFactory.load("cn/sliew/rtomde/platform/mybatis/datasource/hikaricp/hikaricp.properties");
    }

    @Override
    public DataSource getDataSource(DatasourceOptions options) {
        if (dataSourceRegistry.containsKey(options)) {
            return dataSourceRegistry.get(options);
        }
        //no arguments constructor is lazy init datasource until first getConnection
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(options.getDriverClassName());
        dataSource.setJdbcUrl(options.getJdbcUrl());
        dataSource.setUsername(options.getUsername());
        dataSource.setPassword(options.getPassword());

        dataSource.setIdleTimeout(this.config.getLong("idleTimeout"));
        dataSource.setMinimumIdle(this.config.getInt("minimumIdle"));
        dataSource.setMaximumPoolSize(this.config.getInt("maximumPoolSize"));

        Config datasourceConfig = this.config.getConfig("dataSourceProperties");
        for (Map.Entry<String, ConfigValue> entry : datasourceConfig.entrySet()) {
            dataSource.addDataSourceProperty(entry.getKey(), entry.getValue().unwrapped());
        }
        if (options.getProfileSQL() != null) {
            dataSource.addDataSourceProperty("profileSQL", options.getProfileSQL());
        }
        dataSourceRegistry.put(options, dataSource);
        return dataSource;
    }
}
