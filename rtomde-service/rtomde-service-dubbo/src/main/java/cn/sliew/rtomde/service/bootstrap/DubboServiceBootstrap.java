package cn.sliew.rtomde.service.bootstrap;

import cn.sliew.milky.log.Logger;
import cn.sliew.milky.log.LoggerFactory;
import cn.sliew.rtomde.common.bytecode.ClassGenerator;
import cn.sliew.rtomde.common.utils.ClassUtils;
import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.config.MybatisPlatformOptions;
import cn.sliew.rtomde.platform.mybatis.mapping.MappedStatement;
import cn.sliew.rtomde.platform.mybatis.session.SqlSessionFactory;
import cn.sliew.rtomde.service.bytecode.dispatcher.NameUtil;
import javassist.ClassPool;
import javassist.CtClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Order(DubboServiceBootstrap.ORDER)
@Component
public class DubboServiceBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DubboServiceBootstrap.class);

    static final int ORDER = Integer.MAX_VALUE - 10000;

    private ClassLoader classLoader = ClassUtils.getClassLoader(DubboServiceBootstrap.class);
    private ClassPool classPool = ClassGenerator.getClassPool(classLoader);

    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private AnnotationConfigApplicationContext ac;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        MybatisPlatformOptions platform = sqlSessionFactory.getPlatform();
        platform.getAllApplicationOptions().forEach(this::makeDispatcherService);
    }

    private void makeDispatcherService(MybatisApplicationOptions applicationOptions) {
        dubboInternalClassPoolCascade();

        Map<String, List<MappedStatement>> namespaces = applicationOptions.getMappedStatements().stream()
                .collect(Collectors.groupingBy(ms -> NameUtil.namespace(ms.getId())));

        for (Map.Entry<String, List<MappedStatement>> entry : namespaces.entrySet()) {
            DubboInterfaceGenerator interfaceGenerator = new DubboInterfaceGenerator(entry.getKey(), entry.getValue());
            CtClass interfaceClazz = interfaceGenerator.getInterfaceClazz();
            DubboInterfaceImplGenerator interfaceImplGenerator = new DubboInterfaceImplGenerator(applicationOptions, interfaceClazz, entry.getKey(), entry.getValue());
            CtClass interfaceImplClazz = interfaceImplGenerator.getInterfaceImplClazz();

            DubboServiceExporter exporter = new DubboServiceExporter(entry.getKey(), interfaceClazz, interfaceImplClazz, ac);
            exporter.export();
            List<String> methods = entry.getValue().stream().map(ms -> NameUtil.mappedStatementId(ms.getId())).collect(Collectors.toList());
            log.info("export dubbo service: [{}] with methods: [{}]", entry.getKey(), methods);
        }

    }

    private void dubboInternalClassPoolCascade() {
        try {
            ClassPool pool = org.apache.dubbo.common.bytecode.ClassGenerator.getClassPool(classLoader);
            Field parent = ClassPool.class.getDeclaredField("parent");
            parent.setAccessible(true);
            parent.set(pool, classPool);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
