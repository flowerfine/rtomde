package cn.sliew.rtomde.platform.mybatis.cache.decorators;

import cn.sliew.rtomde.platform.mybatis.cache.Cache;

public class SizeConstraintCache implements Cache {

    private final Cache delegate;

    public SizeConstraintCache(Cache delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public void putObject(Object key, Object value) {

    }

    @Override
    public Object getObject(Object key) {
        return delegate.getObject(key);
    }

    @Override
    public Object removeObject(Object key) {
        return delegate.removeObject(key);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }
}
