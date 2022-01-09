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
package cn.rtomde.template.executor.resultset;

import cn.rtomde.template.io.Resources;
import cn.rtomde.template.mapping.ResultMap;
import cn.rtomde.template.mapping.ResultMapping;
import cn.rtomde.template.type.*;
import cn.sliew.milky.common.util.StringUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class ResultSetWrapper {

    private final ResultSet resultSet;
    private final TypeHandlerRegistry typeHandlerRegistry;

    private final List<String> columnNames = new ArrayList<>();
    private final Map<String, String> classNameMap = new HashMap<>();
    private final Map<String, JdbcType> jdbcTypeMap = new HashMap<>();

    private final Map<String, Map<Class<?>, TypeHandler<?>>> typeHandlerMap = new HashMap<>();

    private final Map<String, List<String>> mappedColumnNamesMap = new HashMap<>();
    private final Map<String, List<String>> unMappedColumnNamesMap = new HashMap<>();

    public ResultSetWrapper(ResultSet rs, TypeHandlerRegistry registry) throws SQLException {
        this.resultSet = rs;
        this.typeHandlerRegistry = registry;
        initColumnMetadata();
    }

    private void initColumnMetadata() throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            String labelOrName = StringUtils.isNotBlank(metaData.getColumnLabel(i)) ? metaData.getColumnLabel(i) : metaData.getColumnName(i);
            columnNames.add(labelOrName);
            classNameMap.put(labelOrName, metaData.getColumnClassName(i));
            jdbcTypeMap.put(labelOrName, JdbcType.forCode(metaData.getColumnType(i)));
        }
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    private JdbcType getJdbcType(String columnName) {
        for (Map.Entry<String, JdbcType> entry : jdbcTypeMap.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(columnName)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Gets the type handler to use when reading the result set.
     * Tries to get from the TypeHandlerRegistry by searching for the property type.
     * If not found it gets the column JDBC type and tries to get a handler for it.
     *
     * @param propertyType the property type
     * @param columnName   the column name
     * @return the type handler
     */
    public TypeHandler<?> getTypeHandler(Class<?> propertyType, String columnName) {
        TypeHandler<?> handler = null;

        Map<Class<?>, TypeHandler<?>> columnHandlers = typeHandlerMap.get(columnName);
        if (columnHandlers == null) {
            columnHandlers = new HashMap<>();
            typeHandlerMap.put(columnName, columnHandlers);
        }
        handler = columnHandlers.get(propertyType);
        if (handler != null) {
            return handler;
        }

        if (handler == null) {
            JdbcType jdbcType = getJdbcType(columnName);
            handler = typeHandlerRegistry.getTypeHandler(propertyType, jdbcType);
            // Replicate logic of UnknownTypeHandler#resolveTypeHandler
            // See issue #59 comment 10
            if (handler == null || handler instanceof UnknownTypeHandler) {
                Class<?> javaType = resolveClass(classNameMap.get(columnName));
                if (javaType != null && jdbcType != null) {
                    handler = typeHandlerRegistry.getTypeHandler(javaType, jdbcType);
                } else if (javaType != null) {
                    handler = typeHandlerRegistry.getTypeHandler(javaType);
                } else if (jdbcType != null) {
                    handler = typeHandlerRegistry.getTypeHandler(jdbcType);
                }
            }
            if (handler == null || handler instanceof UnknownTypeHandler) {
                handler = new ObjectTypeHandler();
            }
            columnHandlers.put(propertyType, handler);
        }
        return handler;
    }

    private Class<?> resolveClass(String className) {
        try {
            // #699 className could be null
            if (className != null) {
                return Resources.classForName(className);
            }
        } catch (ClassNotFoundException e) {
            // ignore
        }
        return null;
    }

    private void loadMappedAndUnmappedColumnNames(ResultMap resultMap) throws SQLException {
        List<String> mappedColumnNames = new ArrayList<>();
        List<String> unmappedColumnNames = new ArrayList<>();
        final Set<String> mappedColumns = resultMap.getResultMappings().stream()
                .map(ResultMapping::getColumn)
                .map(column -> column.toUpperCase(Locale.ENGLISH))
                .collect(Collectors.toSet());
        for (String columnName : columnNames) {
            String upperColumnName = columnName.toUpperCase(Locale.ENGLISH);
            if (mappedColumns.contains(upperColumnName)) {
                mappedColumnNames.add(upperColumnName);
            } else {
                unmappedColumnNames.add(columnName);
            }
        }
        mappedColumnNamesMap.put(resultMap.getId(), mappedColumnNames);
        unMappedColumnNamesMap.put(resultMap.getId(), unmappedColumnNames);
    }

    public List<String> getMappedColumnNames(ResultMap resultMap) throws SQLException {
        List<String> mappedColumnNames = mappedColumnNamesMap.get(resultMap.getId());
        if (mappedColumnNames == null) {
            loadMappedAndUnmappedColumnNames(resultMap);
            mappedColumnNames = mappedColumnNamesMap.get(resultMap.getId());
        }
        return mappedColumnNames;
    }
}
