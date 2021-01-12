package cn.sliew.rtomde.executor.mapper;

import org.apache.ibatis.session.SqlSession;

public interface MapperInvoker {

    Object invoke(String id, Object[] args, SqlSession session);
}