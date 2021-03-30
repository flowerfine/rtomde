package cn.sliew.rtomde.platform.mybatis.log;

import cn.sliew.milky.log.Logger;
import cn.sliew.rtomde.platform.mybatis.reflection.ArrayUtil;

import java.lang.reflect.Method;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Base class for proxies to do logging.
 *
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
public abstract class BaseJdbcLogger {

    protected static final Set<String> SET_METHODS;
    protected static final Set<String> EXECUTE_METHODS = new HashSet<>();

    private final Map<Object, Object> columnMap = new HashMap<>();

    private final List<Object> columnNames = new ArrayList<>();
    private final List<Object> columnValues = new ArrayList<>();

    protected final Logger statementLog;
    protected final int queryStack;

    /*
     * Default constructor
     */
    public BaseJdbcLogger(Logger logger, int queryStack) {
        this.statementLog = logger;
        if (queryStack == 0) {
            this.queryStack = 1;
        } else {
            this.queryStack = queryStack;
        }
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
            try {
                return ArrayUtil.toString(((Array) value).getArray());
            } catch (SQLException e) {
                return value.toString();
            }
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

    protected String removeBreakingWhitespace(String original) {
        StringTokenizer whitespaceStripper = new StringTokenizer(original);
        StringBuilder builder = new StringBuilder();
        while (whitespaceStripper.hasMoreTokens()) {
            builder.append(whitespaceStripper.nextToken());
            builder.append(" ");
        }
        return builder.toString();
    }

    protected boolean isDebugEnabled() {
        return statementLog.isDebugEnabled();
    }

    protected boolean isTraceEnabled() {
        return statementLog.isTraceEnabled();
    }

    protected void debug(String text, boolean input) {
        if (statementLog.isDebugEnabled()) {
            statementLog.debug(prefix(input) + text);
        }
    }

    protected void trace(String text, boolean input) {
        if (statementLog.isTraceEnabled()) {
            statementLog.trace(prefix(input) + text);
        }
    }

    private String prefix(boolean isInput) {
        char[] buffer = new char[queryStack * 2 + 2];
        Arrays.fill(buffer, '=');
        buffer[queryStack * 2 + 1] = ' ';
        if (isInput) {
            buffer[queryStack * 2] = '>';
        } else {
            buffer[0] = '<';
        }
        return new String(buffer);
    }

}
