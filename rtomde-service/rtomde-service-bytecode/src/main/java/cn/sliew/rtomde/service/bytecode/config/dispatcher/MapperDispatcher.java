package cn.sliew.rtomde.service.bytecode.config.dispatcher;

import cn.sliew.milky.common.exception.ThrowableCollector;
import cn.sliew.milky.log.Logger;
import cn.sliew.milky.log.LoggerFactory;
import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.config.MybatisPlatformOptions;
import cn.sliew.rtomde.platform.mybatis.binding.MapperInvoker;
import cn.sliew.rtomde.platform.mybatis.binding.MapperMethod;
import cn.sliew.rtomde.platform.mybatis.binding.PlainMapperInvoker;
import cn.sliew.rtomde.platform.mybatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

@Component
public class MapperDispatcher {

    private static final Logger log = LoggerFactory.getLogger(MapperDispatcher.class);

    private final ConcurrentMap<String, MapperInvoker> map = new ConcurrentHashMap<>(4);

    private ThrowableCollector.Factory throwableCollectorFactory = () -> new ThrowableCollector();

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @PostConstruct
    public void register() {
        MybatisPlatformOptions platform = sqlSessionFactory.getPlatform();
        platform.getAllApplicationOptions().forEach(this::dispatcherMapperMethod);
    }

    private void dispatcherMapperMethod(MybatisApplicationOptions application) {
        Collection<String> mappedStatementNames = application.getMappedStatementNames();
        for (String mappedStatementName : mappedStatementNames) {
            MapperMethod mapperMethod = new MapperMethod(application, mappedStatementName);
            map.putIfAbsent(mappedStatementName, new PlainMapperInvoker(mapperMethod));
        }
    }

    public Map<String, MapperInvoker> getMapperInvokers() {
        return Collections.unmodifiableMap(map);
    }

    /**
     * todo application
     */
    public Object execute(String id, String application, Object... params) {
        ThrowableCollector throwableCollector = throwableCollectorFactory.create();
        CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> map.get(id).invoke(sqlSessionFactory.openSession(application), id, params));
        throwableCollector.execute(() -> future.get());
        if (throwableCollector.isEmpty()) {
            try {
                return future.get();
            } catch (InterruptedException e) {
                // should never happen
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                // should never happen
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        } else {
            Throwable throwable = throwableCollector.getThrowable();
            log.error(throwable.getMessage(), throwable);
            throw new RuntimeException(throwable);
        }
    }


}
