package cn.sliew.rtomde.service.bytecode.config.dispatcher;

import lombok.extern.slf4j.Slf4j;
import cn.sliew.rtomde.platform.mybatis.binding.MapperInvoker;
import cn.sliew.rtomde.platform.mybatis.binding.MapperMethod;
import cn.sliew.rtomde.platform.mybatis.binding.PlainMapperInvoker;
import cn.sliew.rtomde.platform.mybatis.session.Configuration;
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
        Configuration configuration = sqlSessionFactory.getConfiguration();
        Collection<String> mappedStatementNames = configuration.getMappedStatementNames();
        for (String mappedStatementName : mappedStatementNames) {
            MapperMethod mapperMethod = new MapperMethod(configuration, mappedStatementName);
            map.putIfAbsent(mappedStatementName, new PlainMapperInvoker(mapperMethod));
        }
    }

    public Map<String, MapperInvoker> getMapperInvokers() {
        return Collections.unmodifiableMap(map);
    }

    public Object execute(String id, Object... params) {
        return map.get(id).invoke(sqlSessionFactory.openSession(), id, params);
    }
}
