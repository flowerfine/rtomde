package cn.rtomde.template.executor;

import cn.rtomde.template.mapping.BoundSql;
import cn.rtomde.template.mapping.MappedStatement;
import cn.rtomde.template.session.RowBounds;

public class ExecuteContext {

    private final MappedStatement ms;
    private final BoundSql boundSql;
    private final RowBounds rowBounds;

    public ExecuteContext(MappedStatement ms, BoundSql boundSql, RowBounds rowBounds) {
        this.ms = ms;
        this.boundSql = boundSql;
        this.rowBounds = rowBounds;
    }

    public MappedStatement getMappedStatement() {
        return ms;
    }

    public BoundSql getBoundSql() {
        return boundSql;
    }

    public RowBounds getRowBounds() {
        return rowBounds;
    }
}
