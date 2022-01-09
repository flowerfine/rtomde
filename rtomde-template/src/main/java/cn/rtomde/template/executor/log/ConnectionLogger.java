/*
 *    Copyright 2009-2021 the original author or authors.
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
package cn.rtomde.template.executor.log;

import cn.sliew.milky.log.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * Connection proxy to add logging.
 */
public final class ConnectionLogger extends BaseJdbcLogger implements InvocationHandler {

    private final Connection connection;

    private ConnectionLogger(Logger statementLog, Connection conn) {
        super(statementLog);
        this.connection = conn;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] params)
            throws Throwable {
        try {
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, params);
            }
            if ("prepareStatement".equals(method.getName()) || "prepareCall".equals(method.getName())) {
                if (isDebugEnabled()) {
                    debug(" Preparing: " + removeExtraWhitespace((String) params[0]), true);
                }
                PreparedStatement stmt = (PreparedStatement) method.invoke(connection, params);
                stmt = PreparedStatementLogger.newInstance(stmt, statementLog);
                return stmt;
            } else if ("createStatement".equals(method.getName())) {
                Statement stmt = (Statement) method.invoke(connection, params);
                stmt = StatementLogger.newInstance(statementLog, stmt);
                return stmt;
            } else {
                return method.invoke(connection, params);
            }
        } catch (Throwable t) {
//            throw ExceptionUtil.unwrapThrowable(t);
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Creates a logging version of a connection.
     *
     * @param conn         the original connection
     * @param statementLog the statement log
     * @return the connection with logging
     */
    public static Connection newInstance(Logger statementLog, Connection conn) {
        InvocationHandler handler = new ConnectionLogger(statementLog, conn);
        ClassLoader cl = Connection.class.getClassLoader();
        return (Connection) Proxy.newProxyInstance(cl, new Class[]{Connection.class}, handler);
    }

    /**
     * return the wrapped connection.
     *
     * @return the connection
     */
    public Connection getConnection() {
        return connection;
    }

}
