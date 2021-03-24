/**
 *    Copyright 2009-2018 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package cn.sliew.rtomde.platform.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Clinton Begin
 */
public class DoubleTypeHandler extends BaseTypeHandler<Double> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, Double parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setDouble(i, parameter);
  }

  @Override
  public Double getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    double result = rs.getDouble(columnName);
    return result == 0 && rs.wasNull() ? null : result;
  }

  @Override
  public Double getNullableResult(ResultSet rs, int columnIndex)
      throws SQLException {
    double result = rs.getDouble(columnIndex);
    return result == 0 && rs.wasNull() ? null : result;
  }

  @Override
  public Double getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    double result = cs.getDouble(columnIndex);
    return result == 0 && cs.wasNull() ? null : result;
  }

}