package cn.sliew.rtomde.datasource.type;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface WriteFunction {

    Class<?> getJavaType();

    default String getBindExpression() {
        return "?";
    }

    default void setNull(PreparedStatement statement, int index) throws SQLException {
        statement.setObject(index, null);
    }
}
