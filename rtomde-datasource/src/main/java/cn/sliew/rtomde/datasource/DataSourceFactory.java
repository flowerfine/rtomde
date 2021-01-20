package cn.sliew.rtomde.datasource;

import com.typesafe.config.Config;

public interface DataSourceFactory<Context> {

    Config config();

    DataSource getDataSource(Context context);
}
