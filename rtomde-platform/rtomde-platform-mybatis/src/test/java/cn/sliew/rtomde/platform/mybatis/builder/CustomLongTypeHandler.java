package cn.sliew.rtomde.platform.mybatis.builder;

import cn.sliew.rtomde.platform.mybatis.type.JdbcType;
import cn.sliew.rtomde.platform.mybatis.type.MappedTypes;
import cn.sliew.rtomde.platform.mybatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(Long.class)
public class CustomLongTypeHandler implements TypeHandler<Long> {

    @Override
    public void setParameter(PreparedStatement ps, int i, Long parameter, JdbcType jdbcType) throws SQLException {
        ps.setLong(i, parameter);
    }

    @Override
    public Long getResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getLong(columnName);
    }

    @Override
    public Long getResult(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getLong(columnIndex);
    }

    @Override
    public Long getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return cs.getLong(columnIndex);
    }

}
