package cn.sliew.rtomde.executor.mapper;

import org.apache.ibatis.session.SqlSession;

public class PlainMapperInvoker implements MapperInvoker {

    private final MapperMethod mapperMethod;

    public PlainMapperInvoker(MapperMethod mapperMethod) {
        this.mapperMethod = mapperMethod;
    }

    @Override
    public Object invoke(SqlSession session, String id, Object[] args) {
        return mapperMethod.execute(session, id, args);
    }
}
