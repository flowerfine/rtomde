package cn.sliew.rtomde.service.bootstrap;

import cn.sliew.rtomde.common.bytecode.CustomizedLoaderClassPath;
import cn.sliew.rtomde.common.utils.ClassUtils;
import cn.sliew.rtomde.service.bytecode.config.dispatcher.MapperDispatcher;
import javassist.*;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.bytecode.ClassGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Order(DubboServiceBootstrap1.ORDER)
@Component
public class DubboServiceBootstrap1 implements ApplicationRunner {

    static final int ORDER = Integer.MAX_VALUE - 10000;

    private String application;

    private static ClassPool classPool = ClassGenerator.getClassPool(DubboServiceBootstrap1.class.getClassLoader());

    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private MapperDispatcher dispatcher;

    static {
        classPool.appendClassPath(new CustomizedLoaderClassPath(Thread.currentThread().getContextClassLoader()));
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Configuration configuration = sqlSessionFactory.getConfiguration();
        this.application = configuration.getApplication();

        makeDispatcherInterface();
    }

    private Class makeDispatcherInterface() {
        CtClass anInterface = classPool.makeInterface("cn.sliew.rtomde.executor.MapperService");
        ClassFile ccFile = anInterface.getClassFile();
        ConstPool constpool = ccFile.getConstPool();

        try {
            CtMethod[] methods = makeServiceMethod(anInterface);
            for (CtMethod m : methods) {
                anInterface.addMethod(m);
            }
            anInterface.writeFile();
            return anInterface.toClass(ClassUtils.getClassLoader(DubboServiceBootstrap1.class), getClass().getProtectionDomain());
        } catch (CannotCompileException e) {
            log.error("create Service:[{}] failed", anInterface.getName(), e);
            throw new RuntimeException("create Service:" + anInterface.getName() + " failed.", e);
        } catch (IOException e) {
            log.error("create Service:[{}] failed", anInterface.getName(), e);
            throw new RuntimeException("create Service:" + anInterface.getName() + " failed.", e);
        } catch (NotFoundException e) {
            log.error("create Service:[{}] failed", anInterface.getName(), e);
            throw new RuntimeException("create Service:" + anInterface.getName() + " failed.", e);
        }
    }

    private CtMethod[] makeServiceMethod(CtClass declaring) {
        Set<String> invokers = dispatcher.getMapperInvokers().keySet();
        List<CtMethod> methods = new ArrayList<>(invokers.size());
        for (String id : invokers) {
            CtMethod method = makeServiceMethod(declaring, id);
            methods.add(method);
        }
        return methods.toArray(new CtMethod[0]);
    }

    private CtMethod makeServiceMethod(CtClass declaring, String id) {
        try {
            MappedStatement mappedStatement = sqlSessionFactory.getConfiguration().getMappedStatement(id);
            Class<?> paramType = mappedStatement.getParameterMap().getType();
            Class<?> resultType = mappedStatement.getResultMap().getType();
            return new CtMethod(classPool.get(resultType.getName()), id.replace(".", "_"), new CtClass[]{classPool.get(paramType.getName())}, declaring);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}