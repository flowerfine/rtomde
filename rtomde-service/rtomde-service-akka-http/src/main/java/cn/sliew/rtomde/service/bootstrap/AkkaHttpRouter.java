package cn.sliew.rtomde.service.bootstrap;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.config.MybatisPlatformOptions;
import cn.sliew.rtomde.platform.mybatis.mapping.MappedStatement;
import cn.sliew.rtomde.platform.mybatis.session.SqlSessionFactory;
import cn.sliew.rtomde.service.bytecode.dispatcher.MapperDispatcher;
import cn.sliew.rtomde.service.bytecode.dispatcher.NameUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AkkaHttpRouter extends AllDirectives {

    @Autowired
    private MapperDispatcher mapperDispatcher;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    public Route createRoute() {
        MybatisPlatformOptions platform = sqlSessionFactory.getPlatform();
        Collection<MybatisApplicationOptions> applications = platform.getAllApplicationOptions();
        List<Route> routes = applications.stream().map(this::dispatchApplication).collect(Collectors.toList());
        if (routes.isEmpty()) {
            return reject();
        }
        if (routes.size() == 1) {
            return routes.get(0);
        }

        MybatisApplicationOptions[] array = applications.toArray(new MybatisApplicationOptions[applications.size()]);
        Route first = dispatchApplication(array[0]);
        List<Route> alternatives = new ArrayList<>(array.length - 1);
        for (int i = 1; i < array.length; i++) {
            alternatives.add(dispatchApplication(array[i]));
        }

        return concat(first, alternatives.toArray(new Route[alternatives.size()]));
    }

    private Route dispatchApplication(MybatisApplicationOptions application) {
        return get(() -> path("/" + application.getId(), () -> dispatchNamespace(application.getMappedStatements())));
    }

    private Route dispatchNamespace(Collection<MappedStatement> mappedStatements) {
        if (mappedStatements.isEmpty()) {
            return reject();
        }
        MappedStatement[] array = mappedStatements.toArray(new MappedStatement[mappedStatements.size()]);
        if (array.length == 1) {
            return dispatchMappedStatement(array[0]);
        }

        Route first = dispatchMappedStatement(array[0]);
        List<Route> alternatives = new ArrayList<>(array.length - 1);
        for (int i = 1; i < array.length; i++) {
            Route alternative = dispatchMappedStatement(array[0]);
            alternatives.add(alternative);
        }

        return concat(first, alternatives.toArray(new Route[alternatives.size()]));
    }

    private Route dispatchMappedStatement(MappedStatement ms) {
        return get(() -> path(NameUtil.mappedStatementId(ms.getId()), () ->
                entity(Jackson.unmarshaller(ms.getParameterMap().getType()), param ->
                        complete(StatusCodes.OK, null, Jackson.marshaller()))));
    }

}
