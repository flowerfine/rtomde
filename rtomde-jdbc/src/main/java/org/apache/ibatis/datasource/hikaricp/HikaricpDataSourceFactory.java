package org.apache.ibatis.datasource.hikaricp;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.datasource.DataSourceFactory;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

public class HikaricpDataSourceFactory implements DataSourceFactory {

    private Properties properties;
    private Config config;

    public HikaricpDataSourceFactory() {
        this.config = ConfigFactory.load("org/apache/ibatis/datasource/hikaricp/hikaricp.properties");
    }

    @Override
    public void setProperties(Properties props) {
        this.properties = props;
    }

    @Override
    public DataSource getDataSource() {
        //no arguments constructor is lazy init datasource until first getConnection
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(this.properties.getProperty("driver"));
        dataSource.setJdbcUrl(this.properties.getProperty("url"));
        dataSource.setUsername(this.properties.getProperty("username"));
        dataSource.setPassword(this.properties.getProperty("password"));

        dataSource.setIdleTimeout(this.config.getLong("idleTimeout"));
        dataSource.setMinimumIdle(this.config.getInt("minimumIdle"));
        dataSource.setMaximumPoolSize(this.config.getInt("maximumPoolSize"));

        Config datasourceConfig = this.config.getConfig("dataSourceProperties");
        for (Map.Entry<String, ConfigValue> entry : datasourceConfig.entrySet()) {
            dataSource.addDataSourceProperty(entry.getKey(), entry.getValue().unwrapped());
        }
        String profileSQL = this.properties.getProperty("profileSQL");
        if (profileSQL != null && !profileSQL.isEmpty()) {
            dataSource.addDataSourceProperty("profileSQL", Boolean.parseBoolean(profileSQL));
        }
        return dataSource;
    }
}
