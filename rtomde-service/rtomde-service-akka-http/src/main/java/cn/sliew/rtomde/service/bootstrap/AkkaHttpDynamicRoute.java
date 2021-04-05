package cn.sliew.rtomde.service.bootstrap;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.mapping.MappedStatement;
import cn.sliew.rtomde.platform.mybatis.session.SqlSessionFactory;
import cn.sliew.rtomde.service.bytecode.dispatcher.MapperDispatcher;
import cn.sliew.rtomde.service.bytecode.dispatcher.NameUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static akka.http.javadsl.server.PathMatchers.segment;

@Component
public class AkkaHttpDynamicRoute extends AllDirectives implements InitializingBean {

    /**
     * application -> (id -> paramClass)
     */
    private final Map<String, Map<String, Class<?>>> applicationIdInvokers = new ConcurrentHashMap<>();

    @Autowired
    private MapperDispatcher mapperDispatcher;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    Route dynamicRoute() {
        return get(() -> applicationIdInvokers.entrySet().stream().map(entry -> application(entry.getKey(), entry.getValue()))
                .reduce(reject(), Route::orElse));
    }

    private Route application(String application, Map<String, Class<?>> invokers) {
        return invokers.entrySet().stream().map(invoker ->
                path(segment(application).slash(segment(NameUtil.mappedStatementId(invoker.getKey()))), () ->
                        entity(Jackson.unmarshaller(invoker.getValue()), param ->
                                complete(StatusCodes.OK, mapperDispatcher.execute(invoker.getKey(), application, param), Jackson.marshaller())
                        )
                )
        ).reduce(reject(), Route::orElse);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Collection<MybatisApplicationOptions> allApplicationOptions = sqlSessionFactory.getPlatform().getAllApplicationOptions();
        for (MybatisApplicationOptions applicationOptions : allApplicationOptions) {
            applicationIdInvokers.put(applicationOptions.getId(), idParamClassMap(applicationOptions));
        }
    }

    private Map<String, Class<?>> idParamClassMap(MybatisApplicationOptions applicationOptions) {
        Collection<MappedStatement> mappedStatements = applicationOptions.getMappedStatements();
        Map<String, Class<?>> idParamClassMap = new HashMap<>(mappedStatements.size());
        for (MappedStatement ms : mappedStatements) {
            idParamClassMap.put(ms.getId(), ms.getParameterMap().getType());
        }
        return idParamClassMap;
    }
}
