package cn.sliew.rtomde.common.reflect;

public interface ReflectorFactory {

  boolean isClassCacheEnabled();

  void setClassCacheEnabled(boolean classCacheEnabled);

  Reflector findForClass(Class<?> type);
}