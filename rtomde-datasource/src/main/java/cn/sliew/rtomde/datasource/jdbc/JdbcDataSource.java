package cn.sliew.rtomde.datasource.jdbc;

import cn.sliew.rtomde.datasource.Connection;
import cn.sliew.rtomde.datasource.DataSource;
import cn.sliew.rtomde.datasource.DataSourceException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;

public class JdbcDataSource implements DataSource {

    private HikariDataSource hikari;

    JdbcDataSource(Config config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getString("jdbc-url"));
        hikariConfig.setUsername(config.getString("username"));
        hikariConfig.setPassword(config.getString("password"));
        if (config.hasPath("driver-class-name")) {
            hikariConfig.setDriverClassName(config.getString("driver-class-name"));
        }
        if (config.hasPath("dataSourceClassName")) {
            hikariConfig.setDataSourceClassName(config.getString("dataSource-class-name"));
        }
        if (config.hasPath("idle-timeout")) {
            hikariConfig.setIdleTimeout(config.getLong("idle-timeout"));
        }
        if (config.hasPath("minimum-idle")) {
            hikariConfig.setMinimumIdle(config.getInt("minimum-idle"));
        }
        if (config.hasPath("maximum-pool-size")) {
            hikariConfig.setMaximumPoolSize(config.getInt("maximum-pool-size"));
        }

        Config datasourceConfig = config.getConfig("datasource");
        for (Map.Entry<String, ConfigValue> entry : datasourceConfig.entrySet()) {
            hikariConfig.addDataSourceProperty(entry.getKey(), entry.getValue().unwrapped());
        }
        this.hikari = new HikariDataSource(hikariConfig);
    }

    @Override
    public Connection openConnection() throws DataSourceException {
        return null;
    }
}
