package cn.sliew.rtomde.platform.mybatis.reflection.wrapper;

import cn.sliew.rtomde.platform.mybatis.reflection.MetaObject;

/**
 * @author Clinton Begin
 */
public interface ObjectWrapperFactory {

    boolean hasWrapperFor(Object object);

    ObjectWrapper getWrapperFor(MetaObject metaObject, Object object);

}
