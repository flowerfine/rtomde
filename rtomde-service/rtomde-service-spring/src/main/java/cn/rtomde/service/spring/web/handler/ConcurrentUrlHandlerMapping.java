package cn.rtomde.service.spring.web.handler;

import org.springframework.beans.BeansException;
import org.springframework.http.server.RequestPath;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrentUrlHandlerMapping extends AbstractUrlHandlerMapping {

    private int order = -1;

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    @Override
    protected Object lookupHandler(RequestPath path, String lookupPath, HttpServletRequest request) throws Exception {
        this.readWriteLock.readLock().lock();
        try {
            return super.lookupHandler(path, lookupPath, request);
        } finally {
            this.readWriteLock.readLock().unlock();
        }
    }

    @Override
    protected Object lookupHandler(String lookupPath, HttpServletRequest request) throws Exception {
        this.readWriteLock.readLock().lock();
        try {
            return super.lookupHandler(lookupPath, request);
        } finally {
            this.readWriteLock.readLock().unlock();
        }
    }

    @Override
    public void registerHandler(String urlPath, Object handler) throws BeansException, IllegalStateException {
        this.readWriteLock.writeLock().lock();
        try {
            super.registerHandler(urlPath, handler);
        } finally {
            this.readWriteLock.writeLock().unlock();
        }
    }

    public void unregisterHandler(String urlPath) {
        if (!existsHandler(urlPath)) {
            if (logger.isInfoEnabled()) {
                logger.info("Unmapped [" + urlPath + "], stop unregister.");
            }
            return;
        }

        this.readWriteLock.writeLock().lock();
        try {
            Field handlerMapField = AbstractUrlHandlerMapping.class.getDeclaredField("handlerMap");
            Field pathPatternHandlerMapField = AbstractUrlHandlerMapping.class.getDeclaredField("pathPatternHandlerMap");

            handlerMapField.setAccessible(true);
            pathPatternHandlerMapField.setAccessible(true);

            Map<String, Object> handlerMap = (Map<String, Object>) handlerMapField.get(this);
            Map<PathPattern, Object> pathPatternHandlerMap = (Map<PathPattern, Object>) pathPatternHandlerMapField.get(this);

            Object handler = handlerMap.remove(urlPath);
            if (urlPath.equals("/")) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Remove root mapping to " + handler.toString());
                }
                setRootHandler(null);
            } else if (urlPath.equals("/*")) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Remove default mapping to " + handler.toString());
                }
                setDefaultHandler(null);
            }

            if (getPatternParser() != null) {
                pathPatternHandlerMap.remove(getPatternParser().parse(urlPath));
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Remove mapped [" + urlPath + "] onto " + handler.toString());
            }
        } catch (NoSuchFieldException e) {
            logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        } finally {
            this.readWriteLock.writeLock().unlock();
        }
    }

    public boolean existsHandler(String urlPath) {
        this.readWriteLock.readLock().lock();
        try {
            Map<String, Object> handlerMap = super.getHandlerMap();
            return handlerMap.containsKey(urlPath);
        } finally {
            this.readWriteLock.readLock().unlock();
        }
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return order;
    }
}
