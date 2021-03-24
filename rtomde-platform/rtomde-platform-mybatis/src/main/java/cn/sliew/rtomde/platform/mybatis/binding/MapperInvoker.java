package cn.sliew.rtomde.platform.mybatis.binding;

import cn.sliew.rtomde.platform.mybatis.session.SqlSession;

public interface MapperInvoker {

    Object invoke(SqlSession session, String id, Object[] args);
}