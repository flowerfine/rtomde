package cn.sliew.rtomde.platform.mybatis.datasource;

import cn.sliew.rtomde.platform.mybatis.config.DatasourceOptions;

import javax.sql.DataSource;
import java.util.Properties;

public interface DataSourceFactory {

    DataSource getDataSource(DatasourceOptions options);

}
