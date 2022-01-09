package cn.rtomde.template.executor.statement;

import cn.rtomde.template.mapping.BoundSql;
import cn.rtomde.template.mapping.MappedStatement;
import cn.rtomde.template.mapping.ParameterMapping;
import cn.rtomde.template.mapping.ResultSetType;
import cn.sliew.milky.log.Logger;
import io.cloudevents.CloudEvent;

import java.util.List;

public class StatementContext {

    private String id;

    private BoundSql boundSql;

    private Logger statementLog;

    private ResultSetType resultSetType;
    private Integer queryTimeout;
    private Integer fetchSize;

    public StatementContext(MappedStatement ms, BoundSql boundSql) {
        this.id = ms.getId();
        this.boundSql = boundSql;
        this.statementLog = ms.getStatementLog();
        this.resultSetType = ms.getResultSetType();
        this.queryTimeout = ms.getTimeout();
        this.fetchSize = ms.getFetchSize();
    }

    public String getId() {
        return id;
    }

    public String getSql() {
        return boundSql.getSql();
    }

    public List<ParameterMapping> getParameterMappings() {
        return boundSql.getParameterMappings();
    }

    public CloudEvent getEvent() {
        return boundSql.getEvent();
    }

    public Logger getStatementLog() {
        return statementLog;
    }

    public ResultSetType getResultSetType() {
        return resultSetType;
    }

    public Integer getQueryTimeout() {
        return queryTimeout;
    }

    public Integer getFetchSize() {
        return fetchSize;
    }
}
