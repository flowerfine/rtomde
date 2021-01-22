package cn.sliew.rtomde.datasource;

import cn.sliew.rtomde.datasource.jdbc.JdbcDataSourceFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DataSourceFactoryTest {

    @Test
    public void testCreateDataSourceFactory() {
        Config config = ConfigFactory.load("applicaton.conf");
        DataSourceFactory dataSourceFactory = new JdbcDataSourceFactory(config);
        DataSource dataSource = dataSourceFactory.getDataSource(null);
        assertNotNull(dataSource);
    }
}
