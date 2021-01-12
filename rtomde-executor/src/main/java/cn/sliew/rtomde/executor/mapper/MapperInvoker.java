package cn.sliew.rtomde.executor.mapper;

import org.apache.ibatis.session.SqlSession;

public interface MapperInvoker {

    Object invoke(SqlSession session, String id, Object[] args);
}