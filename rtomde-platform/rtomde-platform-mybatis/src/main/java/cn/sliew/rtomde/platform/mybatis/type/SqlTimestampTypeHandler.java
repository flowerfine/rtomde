package cn.sliew.rtomde.platform.mybatis.type;

import java.sql.*;

/**
 * @author Clinton Begin
 */
public class SqlTimestampTypeHandler extends BaseTypeHandler<Timestamp> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Timestamp parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setTimestamp(i, parameter);
    }

    @Override
    public Timestamp getNullableResult(ResultSet rs, String columnName)
            throws SQLException {
        return rs.getTimestamp(columnName);
    }

    @Override
    public Timestamp getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        return rs.getTimestamp(columnIndex);
    }

    @Override
    public Timestamp getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        return cs.getTimestamp(columnIndex);
    }
}
