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

import java.lang.reflect.Method;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Base class for proxies to do logging.
 */
public abstract class BaseJdbcLogger {

    protected static final Set<String> SET_METHODS;
    protected static final Set<String> EXECUTE_METHODS = new HashSet<>();

    private final Map<Object, Object> columnMap = new HashMap<>();

    private final List<Object> columnNames = new ArrayList<>();
    private final List<Object> columnValues = new ArrayList<>();

    protected final Logger statementLog;

    /*
     * Default constructor
     */
    public BaseJdbcLogger(Logger log) {
        this.statementLog = log;
    }

    static {
        SET_METHODS = Arrays.stream(PreparedStatement.class.getDeclaredMethods())
                .filter(method -> method.getName().startsWith("set"))
                .filter(method -> method.getParameterCount() > 1)
                .map(Method::getName)
                .collect(Collectors.toSet());

        EXECUTE_METHODS.add("execute");
        EXECUTE_METHODS.add("executeUpdate");
        EXECUTE_METHODS.add("executeQuery");
        EXECUTE_METHODS.add("addBatch");
    }

    protected void setColumn(Object key, Object value) {
        columnMap.put(key, value);
        columnNames.add(key);
        columnValues.add(value);
    }

    protected Object getColumn(Object key) {
        return columnMap.get(key);
    }

    protected String getParameterValueString() {
        List<Object> typeList = new ArrayList<>(columnValues.size());
        for (Object value : columnValues) {
            if (value == null) {
                typeList.add("null");
            } else {
                typeList.add(objectValueString(value) + "(" + value.getClass().getSimpleName() + ")");
            }
        }
        final String parameters = typeList.toString();
        return parameters.substring(1, parameters.length() - 1);
    }

    protected String objectValueString(Object value) {
        if (value instanceof Array) {
//            try {
//                return ArrayUtil.toString(((Array) value).getArray());
//            } catch (SQLException e) {
//                return value.toString();
//            }
        }
        return value.toString();
    }

    protected String getColumnString() {
        return columnNames.toString();
    }

    protected void clearColumnInfo() {
        columnMap.clear();
        columnNames.clear();
        columnValues.clear();
    }

    protected String removeExtraWhitespace(String original) {
//        return SqlSourceBuilder.removeExtraWhitespaces(original);
        return original;
    }

    protected boolean isDebugEnabled() {
        return statementLog.isDebugEnabled();
    }

    protected boolean isTraceEnabled() {
        return statementLog.isTraceEnabled();
    }

    protected void debug(String text, boolean input) {
        if (statementLog.isDebugEnabled()) {
            statementLog.debug(input + text);
        }
    }

    protected void trace(String text, boolean input) {
        if (statementLog.isTraceEnabled()) {
            statementLog.trace(input + text);
        }
    }
}
