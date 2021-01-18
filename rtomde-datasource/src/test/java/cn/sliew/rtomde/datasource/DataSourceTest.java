package cn.sliew.rtomde.datasource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DataSourceTest {

    private DataSource dataSource;

    @BeforeEach
    public void beforeEach() {
        dataSource = new MockDataSource();
    }

    @Test
    public void testGetConnection() {
        Connection connection = dataSource.openConnection();
        assertNotNull(connection);
    }
}
