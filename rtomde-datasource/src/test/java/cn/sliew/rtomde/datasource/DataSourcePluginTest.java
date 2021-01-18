package cn.sliew.rtomde.datasource;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class DataSourcePluginTest {

    @Test
    public void testLoadDataSource() {
        DataSourcePlugin provider = new DataSourcePlugin() {
            @Override
            public List<DataSource> loadDataSources() {
                return Arrays.asList(new MockDataSource());
            }
        };
        List<DataSource> dataSources = provider.loadDataSources();
        assertThat(dataSources, hasSize(greaterThan(0)));
    }
}