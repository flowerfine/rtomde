package cn.sliew.rtomde.platform.mybatis.log;

import cn.sliew.milky.common.log.Logger;
import cn.sliew.rtomde.platform.mybatis.reflection.ExceptionUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Statement proxy to add logging.
 *
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
public final class StatementLogger extends BaseJdbcLogger implements InvocationHandler {

    private final Statement statement;

    private StatementLogger(Statement stmt, Logger statementLog, int queryStack) {
        super(statementLog, queryStack);
        this.statement = stmt;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        try {
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, params);
            }
            if (EXECUTE_METHODS.contains(method.getName())) {
                if (isDebugEnabled()) {
                    debug(" Executing: " + removeBreakingWhitespace((String) params[0]), true);
                }
                if ("executeQuery".equals(method.getName())) {
                    ResultSet rs = (ResultSet) method.invoke(statement, params);
                    return rs == null ? null : ResultSetLogger.newInstance(rs, statementLog, queryStack);
                } else {
                    return method.invoke(statement, params);
                }
            } else if ("getResultSet".equals(method.getName())) {
                ResultSet rs = (ResultSet) method.invoke(statement, params);
                return rs == null ? null : ResultSetLogger.newInstance(rs, statementLog, queryStack);
            } else {
                return method.invoke(statement, params);
            }
        } catch (Throwable t) {
            throw ExceptionUtil.unwrapThrowable(t);
        }
    }

    /**
     * Creates a logging version of a Statement.
     *
     * @param stmt - the statement
     * @return - the proxy
     */
    public static Statement newInstance(Statement stmt, Logger statementLog, int queryStack) {
        InvocationHandler handler = new StatementLogger(stmt, statementLog, queryStack);
        ClassLoader cl = Statement.class.getClassLoader();
        return (Statement) Proxy.newProxyInstance(cl, new Class[]{Statement.class}, handler);
    }

    /**
     * return the wrapped statement.
     *
     * @return the statement
     */
    public Statement getStatement() {
        return statement;
    }

}
