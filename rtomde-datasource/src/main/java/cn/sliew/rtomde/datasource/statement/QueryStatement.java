package cn.sliew.rtomde.datasource.statement;

import cn.sliew.rtomde.datasource.DataSourceException;
import cn.sliew.rtomde.datasource.record.RecordSet;

public interface QueryStatement {

    RecordSet executeQuery() throws DataSourceException;

    void setParamter(int paramterIndex, Object paramter) throws DataSourceException;

    int getQueryTimeout() throws DataSourceException;

    void setQueryTimeout(int seconds) throws DataSourceException;

    void cancel() throws DataSourceException;
}
