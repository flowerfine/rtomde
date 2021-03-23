package cn.sliew.rtomde.service.bootstrap;

import cn.sliew.rtomde.common.bytecode.ClassGenerator;
import cn.sliew.rtomde.common.bytecode.CustomizedLoaderClassPath;
import cn.sliew.rtomde.common.utils.ClassUtils;
import cn.sliew.rtomde.service.bytecode.config.dispatcher.MapperDispatcher;
import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.annotation.Annotation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class SpringmvcServiceBootstrap implements ApplicationRunner {

    private String application;

    private static ClassPool classPool = ClassGenerator.getClassPool(SpringmvcServiceBootstrap.class.getClassLoader());

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

        makeDispatcherController();
    }

    private Class<?> makeDispatcherController() {
        CtClass controllerClass = classPool.makeInterface("cn.sliew.rtomde.executor.MapperController");
        ClassFile ccFile = controllerClass.getClassFile();

        ConstPool constpool = ccFile.getConstPool();

        try {
            CtMethod[] methods = makeRequestMapping(controllerClass, constpool);
            for (CtMethod m : methods) {
                controllerClass.addMethod(m);
            }
            // fixme debug
            controllerClass.writeFile();

            return controllerClass.toClass(ClassUtils.getClassLoader(SpringmvcServiceBootstrap.class), getClass().getProtectionDomain());
        } catch (CannotCompileException e) {
            log.error("create Controller:[{}] failed", controllerClass.getName(), e);
            throw new RuntimeException("create Controller:" + controllerClass.getName() + " failed.", e);
        } catch (IOException e) {
            log.error("create Controller:[{}] failed", controllerClass.getName(), e);
            throw new RuntimeException("create Controller:" + controllerClass.getName() + " failed.", e);
        } catch (NotFoundException e) {
            log.error("create Controller:[{}] failed", controllerClass.getName(), e);
            throw new RuntimeException("create Controller:" + controllerClass.getName() + " failed.", e);
        }
    }

    private CtField makeAutowiredField(CtClass declaring, ConstPool constpool) {
        try {
            CtField ctField = new CtField(classPool.get(MapperDispatcher.class.getName()), "mapperDispatcher", declaring);
            ctField.setModifiers(Modifier.PRIVATE);
            FieldInfo fieldInfo = ctField.getFieldInfo();
            // @Autowired
            AnnotationsAttribute fieldAttr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
            Annotation autowired = new Annotation("org.springframework.beans.factory.annotation.Autowired", constpool);
            fieldAttr.addAnnotation(autowired);
            fieldInfo.addAttribute(fieldAttr);
            return ctField;
        } catch (CannotCompileException | NotFoundException e) {
            log.error("make dispatcher field failed.", e);
            throw new RuntimeException("make @Autowired field failed.", e);
        }
    }

    private CtMethod[] makeRequestMapping(CtClass declaring, ConstPool constpool) {
        Set<String> invokers = dispatcher.getMapperInvokers().keySet();
        List<CtMethod> methods = new ArrayList<>(invokers.size());
        for (String id : invokers) {
            CtMethod method = makeMapperMethod(declaring, id);
            methods.add(method);
        }
        return methods.toArray(new CtMethod[0]);
    }

    private CtMethod makeMapperMethod(CtClass declaring, String id) {
        try {
            MappedStatement mappedStatement = sqlSessionFactory.getConfiguration().getMappedStatement(id);
            Class<?> paramType = mappedStatement.getParameterMap().getType();
            Class<?> resultType = mappedStatement.getResultMap().getType();
            return generateMapperMethod(resultType, paramType, id, declaring);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private CtMethod generateMapperMethod(Class<?> resultType, Class<?> paramType, String id, CtClass declaring) throws Exception {
        CtMethod method = new CtMethod(classPool.get(resultType.getName()), id.replace(".", "_"), new CtClass[]{classPool.get(paramType.getName())}, declaring);
        return method;
    }
}
