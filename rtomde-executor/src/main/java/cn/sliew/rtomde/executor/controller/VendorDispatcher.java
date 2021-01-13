package cn.sliew.rtomde.executor.controller;

import cn.sliew.rtomde.executor.mapper.MapperInvoker;
import cn.sliew.rtomde.executor.mapper.MapperMethod;
import cn.sliew.rtomde.executor.mapper.PlainMapperInvoker;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@RestController
public class VendorDispatcher {

    private final ConcurrentMap<String, MapperInvoker> map = new ConcurrentHashMap<>(4);

    @Autowired
    private RequestMappingHandlerMapping mappingRegistry;
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
        for (Map.Entry<String, MapperInvoker> entry : map.entrySet()) {
            String key = entry.getKey();
            RequestMappingInfo requestMappingInfo = RequestMappingInfo.paths(key).methods(RequestMethod.GET).build();
//            mappingRegistry.registerMapping(requestMappingInfo, null, null);
        }
    }

    @GetMapping("/user/{id}")
    public Object registerHandler(@PathVariable Long id) {
        return map.get("selectByPrimaryKey").invoke(sqlSessionFactory.openSession(), "selectByPrimaryKey", new Object[]{id});
    }

}