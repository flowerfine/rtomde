package org.apache.ibatis.binding;

import org.apache.ibatis.session.SqlSession;

public interface MapperInvoker {

    Object invoke(SqlSession session, String id, Object[] args);
}