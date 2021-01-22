package cn.sliew.rtomde.datasource.type;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ReadFunction {

    Class<?> getJavaType();

    default boolean isNull(ResultSet resultSet, int columnIndex) throws SQLException {
        // JDBC is kind of dumb: we need to read the field and then ask
        // if it was null, which means we are wasting effort here.
        // We could save the result of the field access if it matters.
        resultSet.getObject(columnIndex);
        return resultSet.wasNull();
    }
}
