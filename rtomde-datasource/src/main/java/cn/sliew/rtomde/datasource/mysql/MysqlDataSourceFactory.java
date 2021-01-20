package cn.sliew.rtomde.datasource.mysql;

import cn.sliew.rtomde.datasource.DataSource;
import cn.sliew.rtomde.datasource.DataSourceFactory;
import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
import com.zaxxer.hikari.HikariConfig;

import java.util.concurrent.atomic.AtomicBoolean;

public final class MysqlDataSourceFactory implements DataSourceFactory {

    private final Config config;
    private final AtomicBoolean inited;

    private volatile DataSource dataSource;

    public MysqlDataSourceFactory(Config config) {
        Preconditions.checkArgument(config != null, "config null");
        this.config = config;
        this.inited = new AtomicBoolean(false);
    }

    @Override
    public Config config() {
        return this.config;
    }

    @Override
    public DataSource getDataSource(Object o) {
        if (inited.compareAndSet(false, true)) {
            this.dataSource = new MysqlDataSource(config());
        }
        return this.dataSource;
    }
}
