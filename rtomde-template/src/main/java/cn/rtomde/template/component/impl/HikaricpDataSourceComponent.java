package cn.rtomde.template.component.impl;

import cn.rtomde.template.component.DataSourceComponent;
import cn.sliew.milky.component.AbstractComponent;
import com.github.zafarkhaja.semver.Version;
import com.typesafe.config.Config;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.io.IoBuilder;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.SQLException;

public class HikaricpDataSourceComponent extends AbstractComponent implements DataSourceComponent {

    private static final Logger log = LogManager.getLogger(HikaricpDataSourceComponent.class);

    static final String DRIVER_CLASS_NAME = "driverClassName";
    static final String JDBC_URL = "jdbcUrl";
    static final String USERNAME = "username";
    static final String PASSWORD = "password";
    static final String MINIMUM_IDLE = "minimumIdle";
    static final String MAXIMUM_POOL_SIZE = "maximumPoolSize";
    static final String IDLE_TIMEOUT = "idleTimeout";
    static final String CONNECTION_TIMEOUT = "connectionTimeout";
    static final String MAX_LIFETIME = "maxLifetime";

    private final Config config;

    private HikariDataSource dataSource;

    public HikaricpDataSourceComponent(String name, Config config) {
        super(name);
        this.config = config;
        newInstance();
    }

    private void newInstance() {
        dataSource = new HikariDataSource();
        dataSource.setDriverClassName(config.getString(DRIVER_CLASS_NAME));
        dataSource.setJdbcUrl(config.getString(JDBC_URL));
        dataSource.setUsername(config.getString(USERNAME));
        dataSource.setPassword(config.getString(PASSWORD));
        dataSource.setPoolName(getName() + "-" + getIdentifier());
        dataSource.setMinimumIdle(config.getInt(MINIMUM_IDLE));
        dataSource.setMaximumPoolSize(config.getInt(MAXIMUM_POOL_SIZE));
        dataSource.setIdleTimeout(config.getInt(IDLE_TIMEOUT));
        dataSource.setConnectionTimeout(config.getInt(CONNECTION_TIMEOUT));
        dataSource.setMaxLifetime(config.getInt(MAX_LIFETIME));
        dataSource.setConnectionTestQuery("select 1");
        dataSource.setValidationTimeout(1000L);
        try {
            PrintWriter printWriter = IoBuilder.forLogger(log).setLevel(Level.DEBUG).buildPrintWriter();
            dataSource.setLogWriter(printWriter);
        } catch (SQLException e) {
            log.error("init datasource log print writer error", e);
        }
    }

    @Override
    public DataSource getInstance() {
        return dataSource;
    }

    @Override
    public String getOrganization() {
        return null;
    }

    @Override
    public String getArchitecture() {
        return null;
    }

    @Override
    public String getModule() {
        return null;
    }

    @Override
    public String getOwner() {
        return null;
    }

    @Override
    public boolean isLiveness() {
        return false;
    }

    @Override
    public boolean isReadiness() {
        return false;
    }

    @Override
    public String getNamespace() {
        return null;
    }

    @Override
    public String getApplication() {
        return null;
    }

    @Override
    public String getEnvironment() {
        return null;
    }

    @Override
    public Version getVersion() {
        return null;
    }
}
