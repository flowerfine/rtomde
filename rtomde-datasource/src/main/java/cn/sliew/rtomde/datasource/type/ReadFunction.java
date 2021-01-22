package cn.sliew.rtomde.datasource.type;

import cn.sliew.rtomde.datasource.DataSourceException;
import cn.sliew.rtomde.datasource.record.RecordCursor;

public interface ReadFunction {

    Class<?> getJavaType();

    default boolean isNull(RecordCursor cursor, int columnIndex) throws DataSourceException {
        return cursor.isNull(columnIndex);
    }
}
