package cn.sliew.rtomde.platform.mybatis.binding;

import cn.sliew.rtomde.platform.mybatis.session.SqlSession;

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
