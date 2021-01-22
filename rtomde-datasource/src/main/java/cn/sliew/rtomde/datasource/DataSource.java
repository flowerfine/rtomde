package cn.sliew.rtomde.datasource;

/**
 * 终版，不在改动
 */
public interface DataSource {

    Connection openConnection() throws DataSourceException;
}
