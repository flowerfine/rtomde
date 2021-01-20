package cn.sliew.rtomde.datasource.mysql;

import cn.sliew.rtomde.datasource.Connection;
import cn.sliew.rtomde.datasource.DataSource;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;

public class MysqlDataSource implements DataSource {

    private HikariDataSource hikari;

    MysqlDataSource(Config config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getString("jdbcUrl"));
        hikariConfig.setUsername(config.getString("username"));
        hikariConfig.setPassword(config.getString("password"));
        Config datasourceConfig = config.getConfig("datasource");
        for (Map.Entry<String, ConfigValue> entry : datasourceConfig.entrySet()) {
            hikariConfig.addDataSourceProperty(entry.getKey(), entry.getValue().unwrapped());
        }
        this.hikari = new HikariDataSource(hikariConfig);
    }

    @Override
    public Connection openConnection() {
        return null;
    }
}
