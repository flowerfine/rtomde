package cn.sliew.rtomde.service.bytecode.config.dispatcher;

import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.config.MybatisPlatformOptions;
import cn.sliew.rtomde.platform.mybatis.mapping.MappedStatement;
import lombok.extern.slf4j.Slf4j;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class MapperDispatcher {

    private final ConcurrentMap<String, MapperInvoker> map = new ConcurrentHashMap<>(4);

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
    public Object execute(String application, String id, Object... params) {
        return map.get(id).invoke(sqlSessionFactory.openSession(application), id, params);
    }
}
