package cn.rtomde.template.executor.resultset;

import cn.rtomde.template.cursor.Cursor;
import cn.rtomde.template.cursor.defaults.DefaultCursor;
import cn.rtomde.template.executor.ErrorContext;
import cn.rtomde.template.executor.result.DefaultResultContext;
import cn.rtomde.template.executor.result.DefaultResultHandler;
import cn.rtomde.template.executor.result.ResultContext;
import cn.rtomde.template.executor.result.ResultHandler;
import cn.rtomde.template.mapping.MappedStatement;
import cn.rtomde.template.mapping.ResultMap;
import cn.rtomde.template.mapping.ResultMapping;
import cn.rtomde.template.session.RowBounds;
import cn.rtomde.template.type.TypeHandler;
import cn.rtomde.template.type.TypeHandlerRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.jackson.JsonCloudEventData;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DefaultResultSetHandler implements ResultSetHandler {

    private ObjectMapper objectMapper = new ObjectMapper();

    private final TypeHandlerRegistry typeHandlerRegistry;
    private final MappedStatement ms;

    public DefaultResultSetHandler(TypeHandlerRegistry typeHandlerRegistry, MappedStatement ms) {
        this.typeHandlerRegistry = typeHandlerRegistry;
        this.ms = ms;
    }

    @Override
    public List<CloudEvent> handleResultSets(Statement stmt, RowBounds rowBounds) throws SQLException {
        ErrorContext.instance().activity("handling results").object(ms.getId());

        final List<CloudEvent> multipleResults = new ArrayList<>();
        ResultSetWrapper rsw = getFirstResultSet(stmt);
        ResultMap resultMap = ms.getResultMap();
        while (rsw != null) {
            handleResultSet(rsw, resultMap, multipleResults, rowBounds);
            rsw = getNextResultSet(stmt);
        }
        return multipleResults;
    }

    @Override
    public Cursor<CloudEvent> handleCursorResultSets(Statement stmt, RowBounds rowBounds) throws SQLException {
        ErrorContext.instance().activity("handling cursor results").object(ms.getId());

        ResultSetWrapper rsw = getFirstResultSet(stmt);
        ResultMap resultMap = ms.getResultMap();
        return new DefaultCursor(this, resultMap, rsw, rowBounds);
    }

    private ResultSetWrapper getFirstResultSet(Statement stmt) throws SQLException {
        ResultSet rs = stmt.getResultSet();
        while (rs == null) {
            // move forward to get the first resultset in case the driver
            // doesn't return the resultset as the first result (HSQLDB 2.1)
            if (stmt.getMoreResults()) {
                rs = stmt.getResultSet();
            } else {
                if (stmt.getUpdateCount() == -1) {
                    // no more results. Must be no resultset
                    break;
                }
            }
        }
        return rs != null ? new ResultSetWrapper(rs, typeHandlerRegistry) : null;
    }

    private ResultSetWrapper getNextResultSet(Statement stmt) {
        // Making this method tolerant of bad JDBC drivers
        try {
            if (stmt.getConnection().getMetaData().supportsMultipleResultSets()) {
                // Crazy Standard JDBC way of determining if there are more results
                if (!(!stmt.getMoreResults() && stmt.getUpdateCount() == -1)) {
                    ResultSet rs = stmt.getResultSet();
                    if (rs == null) {
                        return getNextResultSet(stmt);
                    } else {
                        return new ResultSetWrapper(rs, typeHandlerRegistry);
                    }
                }
            }
        } catch (Exception e) {
            // Intentionally ignored.
        }
        return null;
    }

    private void handleResultSet(ResultSetWrapper rsw, ResultMap resultMap, List<CloudEvent> multipleResults, RowBounds rowBounds) throws SQLException {
        try {
            DefaultResultHandler resultHandler = new DefaultResultHandler();
            handleRowValues(rsw, resultMap, resultHandler, rowBounds);
            multipleResults.addAll(resultHandler.getResultList());
        } finally {
            // issue #228 (close resultsets)
            closeResultSet(rsw.getResultSet());
        }
    }

    public void handleRowValues(ResultSetWrapper rsw, ResultMap resultMap, ResultHandler<?> resultHandler, RowBounds rowBounds)
            throws SQLException {
        DefaultResultContext resultContext = new DefaultResultContext();
        ResultSet resultSet = rsw.getResultSet();
        skipRows(resultSet, rowBounds);
        while (shouldProcessMoreRows(resultContext, rowBounds) && !resultSet.isClosed() && resultSet.next()) {
            ObjectNode rowValue = objectMapper.createObjectNode();
            boolean foundValue = applyResultMappings(rsw, resultMap, rowValue);
            if (foundValue) {
                CloudEvent cloudEvent = CloudEventBuilder.v03()
                        .withId(resultMap.getId())
                        .withType(resultMap.getId())
                        .withSource(URI.create("http://localhost/source"))
                        .withData("application/json", JsonCloudEventData.wrap(rowValue))
                        .build();
                resultContext.nextResultObject(cloudEvent);
                ((ResultHandler<Object>) resultHandler).handleResult(resultContext);
            }
        }
    }

    private void skipRows(ResultSet rs, RowBounds rowBounds) throws SQLException {
        if (rs.getType() != ResultSet.TYPE_FORWARD_ONLY) {
            if (rowBounds.getOffset() != RowBounds.NO_ROW_OFFSET) {
                rs.absolute(rowBounds.getOffset());
            }
        } else {
            for (int i = 0; i < rowBounds.getOffset(); i++) {
                if (!rs.next()) {
                    break;
                }
            }
        }
    }

    private boolean shouldProcessMoreRows(ResultContext<?> context, RowBounds rowBounds) {
        return !context.isStopped() && context.getResultCount() < rowBounds.getLimit();
    }

    private boolean applyResultMappings(ResultSetWrapper rsw, ResultMap resultMap, ObjectNode objectNode) throws SQLException {
        List<String> mappedColumnNames = rsw.getMappedColumnNames(resultMap);
        boolean foundValues = false;
        List<ResultMapping> resultMappings = resultMap.getResultMappings();
        for (ResultMapping resultMapping : resultMappings) {
            String column = resultMapping.getColumn();
            if (column != null && mappedColumnNames.contains(column.toUpperCase(Locale.ENGLISH))) {
                applyResultMappingValue(rsw.getResultSet(), objectNode, resultMapping);
                foundValues = true;
            }
        }
        return foundValues;
    }

    private void applyResultMappingValue(ResultSet rs, ObjectNode objectNode, ResultMapping resultMapping) throws SQLException {
        TypeHandler<?> typeHandler = resultMapping.getTypeHandler();
        Object value = typeHandler.getResult(rs, resultMapping.getColumn());
        if (value != null) {
            String property = resultMapping.getColumn();
            if (resultMapping.getProperty() != null) {
                property = resultMapping.getProperty();
            }
            objectNode.putPOJO(property, value);
        }
    }

    private void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            // ignore
        }
    }

}
