package cn.sliew.rtomde.platform.jdbc.parameter;

import cn.sliew.rtomde.platform.jdbc.script.ScriptBoundResult;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ParameterHandler {

    void bindParameter(ScriptBoundResult boundScript, PreparedStatement statement) throws SQLException;
}
