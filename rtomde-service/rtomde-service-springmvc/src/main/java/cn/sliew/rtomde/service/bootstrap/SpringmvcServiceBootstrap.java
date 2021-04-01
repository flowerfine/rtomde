package cn.sliew.rtomde.service.bootstrap;

import cn.sliew.rtomde.common.utils.ClassUtils;
import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.config.MybatisPlatformOptions;
import cn.sliew.rtomde.platform.mybatis.mapping.MappedStatement;
import cn.sliew.rtomde.platform.mybatis.session.SqlSessionFactory;
import javassist.CannotCompileException;
import javassist.CtClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SpringmvcServiceBootstrap implements ApplicationRunner {

    private ClassLoader classLoader = ClassUtils.getClassLoader(SpringmvcServiceBootstrap.class);

    @Autowired
    private GenericWebApplicationContext ac;
    @Autowired
    private RequestMappingHandlerMapping mappingRegistry;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        MybatisPlatformOptions platform = sqlSessionFactory.getPlatform();
        platform.getAllApplicationOptions().forEach(this::makeDispatcherController);
    }

    private void makeDispatcherController(MybatisApplicationOptions applicationOptions) {
        Map<String, List<MappedStatement>> namespaces = applicationOptions.getMappedStatements().stream().collect(Collectors.groupingBy(ms ->
                ms.getId().lastIndexOf(".") != -1 ?
                        ms.getId().substring(0, ms.getId().lastIndexOf(".")) :
                        ms.getId()));

        Map<String, Class<?>> dispatcherControllers = new HashMap<>(namespaces.size());
        for (Map.Entry<String, List<MappedStatement>> entry : namespaces.entrySet()) {
            SpringmvcServiceGenerator springmvcServiceGenerator = new SpringmvcServiceGenerator(applicationOptions, entry.getKey(), entry.getValue());
            CtClass controllerClazz = springmvcServiceGenerator.getControllerClazz();
            Class<?> dispatcherController = toClass(classLoader, controllerClazz);
            dispatcherControllers.put(entry.getKey(), dispatcherController);
        }
        for (Map.Entry<String, Class<?>> entry : dispatcherControllers.entrySet()) {
            SpringmvcControllerExporter exporter = new SpringmvcControllerExporter(applicationOptions, entry.getValue(), namespaces.get(entry.getKey()), ac, mappingRegistry);
            exporter.export();
        }

    }

    private Class<?> toClass(ClassLoader classLoader, CtClass ctClass) {
        try {
            Class clazz = ctClass.toClass(classLoader, getClass().getProtectionDomain());
            ctClass.defrost();
            return clazz;
        } catch (CannotCompileException e) {
            log.error("create Service Class:[{}] failed", ctClass.getName(), e);
            throw new RuntimeException("create Service Class:" + ctClass.getName() + " failed.", e);
        }
    }

}
