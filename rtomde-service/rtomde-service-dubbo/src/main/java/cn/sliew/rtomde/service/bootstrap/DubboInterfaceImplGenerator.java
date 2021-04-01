package cn.sliew.rtomde.service.bootstrap;

import cn.sliew.milky.log.Logger;
import cn.sliew.milky.log.LoggerFactory;
import cn.sliew.rtomde.common.bytecode.ClassGenerator;
import cn.sliew.rtomde.common.utils.ClassUtils;
import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.mapping.MappedStatement;
import cn.sliew.rtomde.service.bytecode.dispatcher.MapperDispatcher;
import cn.sliew.rtomde.service.bytecode.dispatcher.NameUtil;
import cn.sliew.rtomde.service.bytecode.dispatcher.ServiceGenerator;
import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;

import java.util.ArrayList;
import java.util.List;

import static cn.sliew.milky.common.check.Ensures.*;

public class DubboInterfaceImplGenerator implements ServiceGenerator {

    private static final Logger log = LoggerFactory.getLogger(DubboInterfaceImplGenerator.class);

    private ClassLoader classLoader = ClassUtils.getClassLoader(DubboServiceBootstrap.class);
    private ClassPool classPool = ClassGenerator.getClassPool(classLoader);

    private final String mapperDispatcherName = "mapperDispatcher";

    private final MybatisApplicationOptions applicationOptions;
    private final CtClass interfaceClazz;
    private final String namespace;
    private final List<MappedStatement> mappedStatements;

    private CtClass interfaceImplClazz;

    public DubboInterfaceImplGenerator(MybatisApplicationOptions applicationOptions, CtClass interfaceClazz, String namespace, List<MappedStatement> mappedStatements) {
        this.applicationOptions = checkNotNull(applicationOptions, "null applicationOptions");
        this.interfaceClazz = checkNotNull(interfaceClazz, "null interfaceClazz");
        this.namespace = notBlank(namespace, "empty namespace");
        this.mappedStatements = notEmpty(mappedStatements, "empty mappedStatements");
    }

    public CtClass getInterfaceImplClazz() {
        if (interfaceImplClazz == null) {
            generate();
        }
        return interfaceImplClazz;
    }

    @Override
    public void generate() {
        interfaceImplClazz = classPool.makeClass(namespace + "Impl");
        interfaceImplClazz.addInterface(interfaceClazz);
        ClassFile ccFile = interfaceImplClazz.getClassFile();

        ConstPool constpool = ccFile.getConstPool();

        // @Service
        AnnotationsAttribute classAttr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
        Annotation service = new Annotation("org.springframework.stereotype.Service", constpool);
        classAttr.addAnnotation(service);

        ccFile.addAttribute(classAttr);
        try {
            CtField autowiredField = generateAutowiredField(interfaceImplClazz, constpool);
            interfaceImplClazz.addField(autowiredField);

            CtMethod[] methods = generateServiceMethodImpls(applicationOptions, interfaceImplClazz, mappedStatements);
            for (CtMethod m : methods) {
                interfaceImplClazz.addMethod(m);
            }
        } catch (CannotCompileException e) {
            log.error("create Service Impl:[{}] failed", interfaceImplClazz.getName(), e);
            throw new RuntimeException("create Service Impl:" + interfaceImplClazz.getName() + " failed.", e);
        }
    }

    private CtField generateAutowiredField(CtClass declaring, ConstPool constpool) {
        try {
            CtField ctField = new CtField(classPool.get(MapperDispatcher.class.getName()), mapperDispatcherName, declaring);
            ctField.setModifiers(Modifier.PRIVATE);
            FieldInfo fieldInfo = ctField.getFieldInfo();
            // @Autowired
            AnnotationsAttribute fieldAttr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
            Annotation autowired = new Annotation("org.springframework.beans.factory.annotation.Autowired", constpool);
            fieldAttr.addAnnotation(autowired);
            // @Qualifier
            Annotation qualifier = new Annotation("org.springframework.beans.factory.annotation.Qualifier", constpool);
            qualifier.addMemberValue("value", new StringMemberValue(MapperDispatcher.BEAN_NAME, constpool));
            fieldAttr.addAnnotation(qualifier);

            fieldInfo.addAttribute(fieldAttr);
            return ctField;
        } catch (CannotCompileException | NotFoundException e) {
            log.error("make dispatcher field failed.", e);
            throw new RuntimeException("make @Autowired, @Qualifier field failed.", e);
        }
    }

    private CtMethod[] generateServiceMethodImpls(MybatisApplicationOptions applicationOptions, CtClass declaring, List<MappedStatement> mappedStatements) {
        List<CtMethod> methods = new ArrayList<>(mappedStatements.size());
        for (MappedStatement ms : mappedStatements) {
            CtMethod method = doGenerateServiceMethodImpl(applicationOptions, declaring, ms);
            methods.add(method);
        }
        return methods.toArray(new CtMethod[0]);
    }

    private CtMethod doGenerateServiceMethodImpl(MybatisApplicationOptions applicationOptions, CtClass declaring, MappedStatement ms) {
        try {
            Class<?> paramType = ms.getParameterMap().getType();
            Class<?> resultType = ms.getResultMap().getType();
            return generateMapperMethodBody(applicationOptions, resultType, paramType, ms.getId(), declaring);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private CtMethod generateMapperMethodBody(MybatisApplicationOptions applicationOptions, Class<?> resultType, Class<?> paramType, String id, CtClass declaring) throws Exception {
        CtMethod method = new CtMethod(classPool.get(resultType.getName()), NameUtil.mappedStatementId(id), new CtClass[]{classPool.get(paramType.getName())}, declaring);
        StringBuilder methodBody = new StringBuilder();
        methodBody.append("{");
        methodBody.append("return " + mapperDispatcherName + ".execute(\"" + id + "\", \"" + applicationOptions.getId() + "\", $args);");
        methodBody.append("}");
        method.setBody(methodBody.toString());
        return method;
    }
}
