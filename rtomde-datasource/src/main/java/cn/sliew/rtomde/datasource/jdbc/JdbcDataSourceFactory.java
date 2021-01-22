package cn.sliew.rtomde.datasource.jdbc;

import cn.sliew.rtomde.datasource.DataSource;
import cn.sliew.rtomde.datasource.DataSourceFactory;
import com.google.common.base.Preconditions;
import com.typesafe.config.Config;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class JdbcDataSourceFactory implements DataSourceFactory {

    private final Config config;
    private final AtomicBoolean inited;

    private volatile DataSource dataSource;

    public JdbcDataSourceFactory(Config config) {
        this.config = Objects.requireNonNull(config, "config is null");
        this.inited = new AtomicBoolean(false);
    }

    @Override
    public Config config() {
        return config;
    }

    @Override
    public DataSource getDataSource(Object o) {
        if (inited.get()) {
            return this.dataSource;
        }
        if (inited.compareAndSet(false, true)) {
            this.dataSource = new JdbcDataSource(config());
        }
        return this.dataSource;
    }
}
