package cn.sliew.rtomde.datasource;

import cn.sliew.rtomde.datasource.mysql.MysqlDataSourceFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class DataSourceFactoryTest {

    @Test
    public void testCreateDataSourceFactory() {
        Config config = ConfigFactory.load("applicaton.conf");
        DataSourceFactory dataSourceFactory = new MysqlDataSourceFactory(config);
        DataSource dataSource = dataSourceFactory.getDataSource(null);
        assertNotNull(dataSource);
    }
}
