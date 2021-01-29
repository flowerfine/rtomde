package org.apache.ibatis.datasource.hikaricp;

import org.apache.ibatis.datasource.DataSourceFactory;

import javax.sql.DataSource;
import java.util.Properties;

public class HikaricpDataSourceFactory implements DataSourceFactory {

    private Properties properties;

    @Override
    public void setProperties(Properties props) {
        System.out.println(props);
        this.properties = props;
    }

    @Override
    public DataSource getDataSource() {
        return null;
    }
}
